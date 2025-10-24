package com.example.huntquest
import com.google.android.gms.maps.model.LatLng
data class Poi(
    val id: String,
    val name: String,
    val hours: String,
    val distanceKm: Double,
    val address: String? = null,
    val latLng: LatLng? = null

    )
