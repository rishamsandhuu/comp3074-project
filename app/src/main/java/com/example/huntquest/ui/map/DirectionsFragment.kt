package com.example.huntquest.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.huntquest.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.*
import org.json.JSONObject
import android.util.Log
import android.widget.Toast
import kotlin.math.abs

private const val REQ_ORIGIN_LOCATION = 2000

class DirectionsFragment : Fragment(), OnMapReadyCallback {

    private val args: DirectionsFragmentArgs by navArgs()

    private var map: GoogleMap? = null
    private lateinit var btnStart: MaterialButton
    private lateinit var tvEta: TextView
    private lateinit var tvDistance: TextView
    private lateinit var toggle: MaterialButtonToggleGroup
    private lateinit var btnDriving: MaterialButton
    private lateinit var btnWalking: MaterialButton

    // Directions polyline + markers
    private var routePolyline: Polyline? = null
    private var originMarker: Marker? = null
    private var destMarker: Marker? = null

    private var directionsJob: Job? = null

    // Trip mode (breadcrumb while moving)
    private var tripActive = false
    private var tripStartMillis = 0L
    private var breadcrumb: Polyline? = null

    // Route duration for countdown
    private var routeDurationSeconds = 0

    private var fusedClient: FusedLocationProviderClient? = null
    private val locationRequest by lazy {
        LocationRequest.Builder(2000L)
            .setMinUpdateIntervalMillis(1000L)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            val pt = LatLng(loc.latitude, loc.longitude)
            val poly = breadcrumb ?: run {
                breadcrumb = map?.addPolyline(PolylineOptions().width(10f))
                breadcrumb!!
            }
            val pts = poly.points
            pts.add(pt)
            poly.points = pts
        }
    }

    private val uiScope = MainScope()
    private val http = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_directions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI refs
        btnStart = view.findViewById(R.id.btnStart)
        tvEta = view.findViewById(R.id.tvEta)
        tvDistance = view.findViewById(R.id.tvDistance)
        toggle = view.findViewById(R.id.toggleMode)
        btnDriving = view.findViewById(R.id.btnDriving)
        btnWalking = view.findViewById(R.id.btnWalking)

        // Map in card container
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer)
                as? SupportMapFragment ?: SupportMapFragment.newInstance().also {
            childFragmentManager.beginTransaction()
                .replace(R.id.mapContainer, it)
                .commitNow()
        }
        mapFragment.getMapAsync(this)

        fusedClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Refetch route when switching Driving/Walking
        toggle.addOnButtonCheckedListener(
            MaterialButtonToggleGroup.OnButtonCheckedListener { _, _, _ ->
                if (map != null) fetchAndRenderRoute()
            }
        )

        // Start / Stop trip
        btnStart.setOnClickListener {
            if (!tripActive) startTrip() else stopTrip()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMapToolbarEnabled = true
        }

        // Shows Google’s blue dot if permission is granted
        val fine = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            try {
                map?.isMyLocationEnabled = true
            } catch (_: SecurityException) {
            }
        }

        fetchAndRenderRoute()
    }

    // --- Helpers ---

    private fun currentMode(): String {
        // map Driving/Walking buttons → directions mode
        return if (toggle.checkedButtonId == btnWalking.id) "walking" else "driving"
    }

    private fun toastShort(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    private fun hasLocationPermission(): Boolean {
        val ctx = requireContext()
        val fine = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    // --- Route fetching / drawing ---


    private fun fetchAndRenderRoute() {
        val dest = LatLng(args.destLat.toDouble(), args.destLng.toDouble())

        // If origin was explicitly provided (from MapFragment), use it directly
        val hasExplicitOrigin =
            args.originLat.toDouble() != 0.0 || args.originLng.toDouble() != 0.0

        if (hasExplicitOrigin) {
            val origin = LatLng(args.originLat.toDouble(), args.originLng.toDouble())
            drawRoute(origin, dest)
            return
        }

        // Otherwise we want current device location
        if (!hasLocationPermission()) {
            // This SHOULD trigger the Android "allow location" dialog
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQ_ORIGIN_LOCATION
            )
            return
        }

        // We already have permission → ask FusedLocation for a *fresh* fix first
        try {
            fusedClient?.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                ?.addOnSuccessListener { loc ->
                    if (loc != null) {
                        val origin = LatLng(loc.latitude, loc.longitude)
                        drawRoute(origin, dest)
                    } else {
                        // If getCurrentLocation returns null, fall back to lastLocation
                        fusedClient?.lastLocation
                            ?.addOnSuccessListener { last ->
                                val origin = if (last != null) {
                                    LatLng(last.latitude, last.longitude)
                                } else {
                                    // Absolute last resort: Toronto centre
                                    LatLng(43.6532, -79.3832)
                                }
                                drawRoute(origin, dest)
                            }
                            ?.addOnFailureListener {
                                val origin = LatLng(43.6532, -79.3832)
                                drawRoute(origin, dest)
                            }
                    }
                }
                ?.addOnFailureListener {
                    // getCurrentLocation failed → fall back to Toronto
                    val origin = LatLng(43.6532, -79.3832)
                    drawRoute(origin, dest)
                }
        } catch (se: SecurityException) {
            // If somehow called without permission, don’t crash – just use fallback
            val origin = LatLng(43.6532, -79.3832)
            drawRoute(origin, dest)
        }
    }



    private fun drawRoute(origin: LatLng, dest: LatLng) {
        val m = map ?: return

        // Cancel any previous directions request
        directionsJob?.cancel()

        // Clear overlays
        routePolyline?.remove(); routePolyline = null
        originMarker?.remove(); originMarker = null
        destMarker?.remove(); destMarker = null

        originMarker = m.addMarker(
            MarkerOptions()
                .position(origin)
                .title("Origin")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )
        destMarker = m.addMarker(
            MarkerOptions()
                .position(dest)
                .title("Destination")
        )

        // Fit both markers while route loads
        val bounds = LatLngBounds.builder()
            .include(origin)
            .include(dest)
            .build()
        m.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

        // Reset ETA / distance while loading
        tvEta.text = getString(R.string.eta_placeholder, "—")
        tvDistance.text = "—"

        directionsJob = uiScope.launch {
            val json = runCatching {
                withContext(Dispatchers.IO) {
                    fetchDirectionsJson(origin, dest, currentMode())
                }
            }.getOrNull()

            if (!isActive) return@launch

            if (json == null) {
                toastShort("Directions request failed (null). Check API key / network.")
                return@launch
            }

            val status = json.optString("status")
            if (status != "OK") {
                val msg = json.optString("error_message", status)
                toastShort("Directions status: $status")
                return@launch
            }

            val (points, distanceText, durationText, durationSeconds) = parseDirections(json)
            routeDurationSeconds = durationSeconds

            if (!isActive) return@launch

            tvEta.text = "Eta: $durationText"     // initial display before starting trip
            tvDistance.text = distanceText

            if (points.isNotEmpty()) {
                routePolyline = m.addPolyline(
                    PolylineOptions()
                        .addAll(points)
                        .width(12f)
                )

                val rb = LatLngBounds.builder().apply {
                    points.forEach { include(it) }
                }.build()

                m.animateCamera(CameraUpdateFactory.newLatLngBounds(rb, 100))
            } else {
                toastShort("No route points returned.")
            }
        }
    }

    private fun fetchDirectionsJson(origin: LatLng, dest: LatLng, mode: String): JSONObject? {
        val key = requireContext().getString(R.string.directions_key).trim()
        val tag = "DirectionsDebug"

        fun mask(s: String) =
            if (s.length < 8) "(len=${s.length})" else s.take(4) + "…" + s.takeLast(4)

        if (key.isEmpty()) {
            Log.e(tag, "Missing key! directions_key is empty")
            uiScope.launch { toastShort("Missing DIRECTIONS_API_KEY (res/values).") }
            return null
        } else {
            Log.d(tag, "Using key: ${mask(key)}")
        }

        val url = Uri.Builder()
            .scheme("https")
            .authority("maps.googleapis.com")
            .path("maps/api/directions/json")
            .appendQueryParameter("origin", "${origin.latitude},${origin.longitude}")
            .appendQueryParameter("destination", "${dest.latitude},${dest.longitude}")
            .appendQueryParameter("mode", mode) // driving | walking
            .appendQueryParameter("key", key)
            .build()
            .toString()

        Log.d(tag, "GET ${url.substringBefore("&key=")}&key=***")

        val req = Request.Builder().url(url).get().build()
        return try {
            http.newCall(req).execute().use { resp ->
                Log.d(tag, "HTTP ${resp.code}")
                val bodyStr = resp.body?.string()
                if (!resp.isSuccessful) {
                    Log.e(tag, "Unsuccessful HTTP. Body: $bodyStr")
                    uiScope.launch { toastShort("HTTP ${resp.code}") }
                    null
                } else if (bodyStr.isNullOrEmpty()) {
                    Log.e(tag, "Empty body")
                    null
                } else {
                    val json = JSONObject(bodyStr)
                    val status = json.optString("status")
                    val errMsg = json.optString("error_message")
                    Log.d(tag, "Status: $status  Error: $errMsg")
                    json
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Network error", e)
            uiScope.launch { toastShort("Network err: ${e.javaClass.simpleName}") }
            null
        }
    }

    private data class RouteParseResult(
        val points: List<LatLng>,
        val distanceText: String,
        val durationText: String,
        val durationSeconds: Int
    )

    private fun parseDirections(json: JSONObject): RouteParseResult {
        val routes = json.optJSONArray("routes")
        if (routes == null || routes.length() == 0) {
            return RouteParseResult(emptyList(), "—", "—", 0)
        }

        val first = routes.getJSONObject(0)
        val legs = first.optJSONArray("legs")
        val leg0 = if (legs != null && legs.length() > 0) legs.getJSONObject(0) else null

        val distanceText = leg0
            ?.optJSONObject("distance")
            ?.optString("text") ?: "—"

        val durationObj = leg0?.optJSONObject("duration")
        val durationText = durationObj?.optString("text") ?: "—"
        val durationSeconds = durationObj?.optInt("value", 0) ?: 0

        val encoded = first.optJSONObject("overview_polyline")?.optString("points") ?: ""
        val decoded = decodePolyline(encoded)

        return RouteParseResult(decoded, distanceText, durationText, durationSeconds)
    }

    // --- Trip start/stop ---

    private fun startTrip() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2001)
            return
        }

        tripActive = true
        tripStartMillis = System.currentTimeMillis()
        btnStart.text = getString(R.string.stop_)
        map?.isMyLocationEnabled = true

        // Start live location breadcrumb
        fusedClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            requireActivity().mainLooper
        )

        // Countdown from routeDurationSeconds
        tvEta.removeCallbacks(null)
        tvEta.post(object : Runnable {
            override fun run() {
                if (!tripActive) return

                if (routeDurationSeconds <= 0) {
                    // If no ETA from API → show elapsed time
                    val mins = (System.currentTimeMillis() - tripStartMillis) / 60000.0
                    tvEta.text = String.format("Trip time: %.1f min", mins)
                    tvEta.postDelayed(this, 1000L)
                    return
                }

                val elapsedSec =
                    ((System.currentTimeMillis() - tripStartMillis) / 1000L).toInt()
                val remainingSec = routeDurationSeconds - elapsedSec

                if (remainingSec <= 0) {
                    tvEta.text = "Eta: 0.0 min"
                    stopTrip()
                    return
                }

                val remainingMin = remainingSec / 60.0
                tvEta.text = String.format("Eta: %.1f min", remainingMin)
                tvEta.postDelayed(this, 1000L)
            }
        })
    }

    private fun stopTrip() {
        tripActive = false
        btnStart.text = getString(R.string.start_)
        fusedClient?.removeLocationUpdates(locationCallback)
        fetchAndRenderRoute()
    }

    // --- Polyline decoder ---

    private fun decodePolyline(encoded: String): List<LatLng> {
        if (encoded.isEmpty()) return emptyList()
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            val latD = lat / 1E5
            val lngD = lng / 1E5
            poly.add(LatLng(latD, lngD))
        }
        return poly
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_ORIGIN_LOCATION) {
            val granted = grantResults.any { it == PackageManager.PERMISSION_GRANTED }
            if (granted) {
                // Now that we have permission, build route from real location
                fetchAndRenderRoute()
            } else {
                toastShort("Location permission denied, using default start point.")
                val dest = LatLng(args.destLat.toDouble(), args.destLng.toDouble())
                val defaultOrigin = LatLng(43.6532, -79.3832)
                drawRoute(defaultOrigin, dest)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        uiScope.cancel()
        fusedClient?.removeLocationUpdates(locationCallback)
    }
}
