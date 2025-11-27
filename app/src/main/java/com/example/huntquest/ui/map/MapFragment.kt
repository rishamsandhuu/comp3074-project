package com.example.huntquest.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.huntquest.R
import com.example.huntquest.data.Poi
import com.example.huntquest.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private var _vb: FragmentMapBinding? = null
    private val vb get() = _vb!!

    private var map: GoogleMap? = null
    private val markerToPoi = mutableMapOf<Marker, Poi>()

    private val vm: PoiMapViewModel by viewModels()
    
    private val fused by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private val reqLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) enableMyLocationIfPermitted()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _vb = FragmentMapBinding.bind(view)

        // Map init
        (childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment)
            .getMapAsync(this)

        // Start hidden
        vb.cardPoi.visibility = View.GONE

        // Observe POIs from DB → draw markers
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.pois.collect { list -> drawMarkers(list) }
            }
        }

        // Card actions
        vb.btnDetails.setOnClickListener {
            val poi = vb.cardPoi.tag as? Poi ?: return@setOnClickListener
            // TODO: navigate to your POI details screen (SafeArgs)
            // findNavController().navigate(MapFragmentDirections.actionMapToPoiDetail(poi.id))
        }
        vb.btnDirections.setOnClickListener {
            val poi = vb.cardPoi.tag as? Poi ?: return@setOnClickListener
            val destLat = poi.latitude ?: return@setOnClickListener
            val destLng = poi.longitude ?: return@setOnClickListener

            fun navigateWith(origin: com.google.android.gms.maps.model.LatLng) {
                val action = MapFragmentDirections.actionMapToDirections(
                    originLat = origin.latitude.toFloat(),
                    originLng = origin.longitude.toFloat(),
                    destLat   = destLat.toFloat(),
                    destLng   = destLng.toFloat()
                )
                findNavController().navigate(action)
            }

            // 1) Explicit permission check
            val fineGranted = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val coarseGranted = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!fineGranted && !coarseGranted) {
                // If you already have an Activity Result launcher, use it; otherwise request directly:
                // If you have: reqLocationPermissions.launch(arrayOf(...))
                reqLocationPermissions.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
                return@setOnClickListener
            }

            // 2) We have permission → try lastLocation, handle SecurityException defensively
            try {
                fused.lastLocation
                    .addOnSuccessListener { loc ->
                        val origin = if (loc != null) {
                            com.google.android.gms.maps.model.LatLng(loc.latitude, loc.longitude)
                        } else {
                            // Fallback: camera center or destination if camera is null
                            map?.cameraPosition?.target
                                ?: com.google.android.gms.maps.model.LatLng(destLat, destLng)
                        }
                        navigateWith(origin)
                    }
                    .addOnFailureListener {
                        // Fallback: camera center or destination
                        val origin = map?.cameraPosition?.target
                            ?: com.google.android.gms.maps.model.LatLng(destLat, destLng)
                        navigateWith(origin)
                    }
            } catch (se: SecurityException) {
                // Shouldn't happen after our check, but guard anyway
                val origin = map?.cameraPosition?.target
                    ?: com.google.android.gms.maps.model.LatLng(destLat, destLng)
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

        // Style: if you have dark mode support, you can load a raw style here; or leave default.
        map?.uiSettings?.isMapToolbarEnabled = false
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(43.6532, -79.3832), 11f)) // Toronto

        map?.setOnMarkerClickListener { marker ->
            markerToPoi[marker]?.let { showCard(it) }
            true // consume (we're using our own card, not InfoWindow)
        }

        map?.setOnMapClickListener { vb.cardPoi.visibility = View.GONE }

        enableMyLocationIfPermitted()
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
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
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

    private val reqLocationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (fine || coarse) enableMyLocationIfPermitted()

        }

    private fun hasLocationPermission(): Boolean {
        val ctx = requireContext()
        val fine = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun enableMyLocationIfPermitted() {
        val gmap = map ?: return
        if (hasLocationPermission()) {
            try {
                gmap.isMyLocationEnabled = true
            } catch (se: SecurityException) {

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
