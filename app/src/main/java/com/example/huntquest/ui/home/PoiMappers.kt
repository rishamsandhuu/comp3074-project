package com.example.huntquest.ui.home

import android.location.Location
import com.example.huntquest.data.Poi

/**
 * Map the Room entity (data layer) to the Home screen UI model.
 * If user or POI coords are missing, distance = 0.0 (we hide it in the adapter).
 */
fun Poi.toHomePoi(userLat: Double?, userLng: Double?): HomePoi {

    val lat = latitude
    val lng = longitude

    val km = if (userLat != null && userLng != null && lat != null && lng != null) {
        val out = FloatArray(1)
        Location.distanceBetween(userLat, userLng, lat, lng, out)
        out[0] / 1000.0
    } else 0.0

    return HomePoi(
        id = id,
        name = name,
        distanceKm = km,
        hours = openUntil,
        rating = rating
    )
}

