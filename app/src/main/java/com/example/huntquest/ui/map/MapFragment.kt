package com.example.huntquest.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.huntquest.ActivityDetailsActivity
import com.example.huntquest.MainActivity   // 👈 NEW
import com.example.huntquest.R
import com.example.huntquest.data.Poi
import com.example.huntquest.databinding.FragmentMapBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private var _vb: FragmentMapBinding? = null
    private val vb get() = _vb!!

    private var map: GoogleMap? = null
    private val markerToPoi = mutableMapOf<Marker, Poi>()
    private val vm: PoiMapViewModel by viewModels()

    private var latestPois: List<Poi> = emptyList()

    // If launched from details screen, shows single POI
    private var focusedPoiId: Long? = null

    private val fused by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    // Request both fine + coarse location permissions
    private val reqLocationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (fine || coarse) {
                enableMyLocationIfPermitted()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _vb = FragmentMapBinding.bind(view)

        // Read optional POI id passed from MainActivity (when coming from details screen)
        focusedPoiId = arguments?.getLong("poiId", -1L)?.takeIf { it != -1L }

        // Map init
        (childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment)
            .getMapAsync(this)

        vb.cardPoi.visibility = View.GONE

        // FULLSCREEN BUTTON: hide bottom nav via MainActivity
        vb.btnFullscreen.setOnClickListener {
            (requireActivity() as? MainActivity)?.setMapFullscreen(true)
        }

        // Observe POIs from DB → draw markers when map is ready
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.pois.collect { list ->
                    latestPois = list
                    map?.let { drawMarkers(list) }
                    focusPoiIfNeeded()
                }
            }
        }

        // Card: Details → open ActivityDetailsActivity for this POI
        vb.btnDetails.setOnClickListener {
            val poi = vb.cardPoi.tag as? Poi ?: return@setOnClickListener
            startActivity(
                Intent(requireContext(), ActivityDetailsActivity::class.java)
                    .putExtra("poi_id", poi.id)
            )
        }

        // Card: Directions → open DirectionsFragment with origin and dest
        vb.btnDirections.setOnClickListener {
            val poi = vb.cardPoi.tag as? Poi ?: return@setOnClickListener
            val destLat = poi.latitude ?: return@setOnClickListener
            val destLng = poi.longitude ?: return@setOnClickListener

            fun navigateWith(origin: LatLng) {
                val action = MapFragmentDirections.actionMapToDirections(
                    originLat = origin.latitude.toFloat(),
                    originLng = origin.longitude.toFloat(),
                    destLat = destLat.toFloat(),
                    destLng = destLng.toFloat()
                )
                findNavController().navigate(action)
            }

            // Permission check
            val fineGranted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val coarseGranted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!fineGranted && !coarseGranted) {
                reqLocationPermissions.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
                return@setOnClickListener
            }

            // If permission granted → try lastLocation
            try {
                fused.lastLocation
                    .addOnSuccessListener { loc ->
                        val origin = if (loc != null) {
                            LatLng(loc.latitude, loc.longitude)
                        } else {
                            // Fallback: camera center or destination
                            map?.cameraPosition?.target ?: LatLng(destLat, destLng)
                        }
                        navigateWith(origin)
                    }
                    .addOnFailureListener {
                        val origin = map?.cameraPosition?.target ?: LatLng(destLat, destLng)
                        navigateWith(origin)
                    }
            } catch (se: SecurityException) {
                val origin = map?.cameraPosition?.target ?: LatLng(destLat, destLng)
                navigateWith(origin)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vb = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map?.uiSettings?.isMapToolbarEnabled = false
        map?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(43.6532, -79.3832), // Toronto
                11f
            )
        )

        map?.setOnMarkerClickListener { marker ->
            markerToPoi[marker]?.let { showCard(it) }
            true
        }

        map?.setOnMapClickListener {
            vb.cardPoi.visibility = View.GONE
        }

        enableMyLocationIfPermitted()

        if (latestPois.isNotEmpty()) {
            drawMarkers(latestPois)
            focusPoiIfNeeded()
        }
    }

    private fun drawMarkers(pois: List<Poi>) {
        val gmap = map ?: return
        markerToPoi.clear()
        gmap.clear()

        for (p in pois) {
            val lat = p.latitude ?: continue
            val lng = p.longitude ?: continue
            val marker = gmap.addMarker(
                MarkerOptions()
                    .position(LatLng(lat, lng))
                    .title(p.name)
                    .icon(
                        BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE
                        )
                    )
            )
            if (marker != null) {
                marker.tag = p.id
                markerToPoi[marker] = p
            }
        }
    }

    private fun showCard(poi: Poi) {
        vb.tvPoiName.text = poi.name
        vb.tvPoiAddress.text = poi.address ?: "Address unavailable"
        vb.tvOpenUntil.text = poi.openUntil
        vb.ratingBar.rating = poi.rating
        vb.cardPoi.tag = poi
        vb.cardPoi.visibility = View.VISIBLE
    }

    private fun focusPoiIfNeeded() {
        val id = focusedPoiId ?: return
        val gmap = map ?: return

        val target = latestPois.firstOrNull { it.id == id } ?: return
        val lat = target.latitude ?: return
        val lng = target.longitude ?: return

        val pos = LatLng(lat, lng)
        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f))
        showCard(target)

        focusedPoiId = null
    }

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

    private fun enableMyLocationIfPermitted() {
        val gmap = map ?: return
        if (hasLocationPermission()) {
            try {
                gmap.isMyLocationEnabled = true
            } catch (_: SecurityException) {
            }
        } else {
            reqLocationPermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}
