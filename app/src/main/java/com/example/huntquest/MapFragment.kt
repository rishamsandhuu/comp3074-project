package com.example.huntquest.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.huntquest.Poi
import com.example.huntquest.R
import com.example.huntquest.InMemoryPoiRepo
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private val markerToPoi = mutableMapOf<Marker, Poi>()

    private val requestLocPerm = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val ok = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (ok) enableMyLocationIfPermitted()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_map, container, false)

        // Bottom row buttons
        v.findViewById<View>(R.id.btnDirections).setOnClickListener {
            // use destination id directly; replace with Safe Args later if needed
            findNavController().navigate(R.id.directionsFragment)
        }
        v.findViewById<View>(R.id.btnFullscreen).setOnClickListener {
            Toast.makeText(requireContext(), "Full screen view coming soon", Toast.LENGTH_SHORT).show()
        }

        // Card buttons
        v.findViewById<View>(R.id.btnDetailsCard)?.setOnClickListener {
            findNavController().navigate(R.id.poiDetailFragment)
        }
        v.findViewById<View>(R.id.btnDirectionsCard)?.setOnClickListener {
            findNavController().navigate(R.id.directionsFragment)
        }

        // Tap background to hide card
        v.setOnClickListener { hidePoiCard() }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFrag = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFrag.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map.apply {
            uiSettings.isZoomControlsEnabled = true
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        maybeRequestLocation()

        map.setOnMarkerClickListener { marker ->
            val poi = markerToPoi[marker]
            if (poi != null) showPoiCard(poi, marker.position)
            true
        }
        map.setOnMapClickListener { hidePoiCard() }

        // Render ALL POIs (hardcoded repo for now)
        renderAllPois()
    }

    private fun renderAllPois() {
        val m = googleMap ?: return
        val pois = InMemoryPoiRepo.getAll()

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            val latLngs = mutableListOf<LatLng>()
            for (poi in pois) {
                val ll = poi.latLng ?: geocodeAddressAsync(poi.address)
                if (ll != null) {
                    val marker = m.addMarker(
                        MarkerOptions()
                            .position(ll)
                            .title(poi.name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    )
                    if (marker != null) markerToPoi[marker] = poi
                    latLngs.add(ll)
                }
            }
            if (latLngs.isNotEmpty()) {
                val b = LatLngBounds.builder().apply { latLngs.forEach { include(it) } }.build()
                m.animateCamera(CameraUpdateFactory.newLatLngBounds(b, 80))
            }
        }
    }

    private suspend fun geocodeAddressAsync(address: String?): LatLng? = withContext(Dispatchers.IO) {
        if (address.isNullOrBlank()) return@withContext null
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocationName(address, 1)
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(address, 1)
            }
            val first = list?.firstOrNull() ?: return@withContext null
            LatLng(first.latitude, first.longitude)
        } catch (e: Exception) {
            null
        }
    }

    private fun showPoiCard(poi: Poi, at: LatLng) {
        val root = requireView()
        val card = root.findViewById<View>(R.id.poiCard)
        root.findViewById<TextView>(R.id.tvPoiName).text = poi.name
        root.findViewById<TextView>(R.id.tvPoiAddress).text = poi.address ?: ""
        card.tag = poi
        if (card.visibility != View.VISIBLE) {
            card.alpha = 0f
            card.visibility = View.VISIBLE
            card.animate().alpha(1f).setDuration(150).start()
        }
        googleMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                at,
                googleMap?.cameraPosition?.zoom?.coerceAtLeast(13f) ?: 14f
            )
        )
    }

    private fun hidePoiCard() {
        val card = view?.findViewById<View>(R.id.poiCard) ?: return
        if (card.visibility == View.VISIBLE) {
            card.animate().alpha(0f).setDuration(120).withEndAction {
                card.visibility = View.GONE
                card.alpha = 1f
                card.tag = null
            }.start()
        }
    }

    private fun hasLocationPermission(): Boolean {
        val ctx = requireContext()
        val fine = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    private fun maybeRequestLocation() {
        if (hasLocationPermission()) {
            enableMyLocationIfPermitted()
        } else {
            requestLocPerm.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocationIfPermitted() {
        if (!hasLocationPermission()) return
        try { googleMap?.isMyLocationEnabled = true } catch (_: SecurityException) {}
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
