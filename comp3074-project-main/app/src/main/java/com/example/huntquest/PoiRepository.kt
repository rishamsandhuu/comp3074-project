package com.example.huntquest

import com.example.huntquest.ui.home.HomePoi

/**
 * Legacy demo repository (kept only so old references compile).
 * If nothing uses this anymore, feel free to delete the file.
 */
object DemoPoiRepository {

    private val demo = listOf(
        HomePoi(
            id = 1L,
            name = "CN Tower",
            distanceKm = 0.0,                 // no user location -> show blank in adapter
            hours = "Open until 10:00 pm"
        ),
        HomePoi(
            id = 2L,
            name = "Royal Ontario Museum",
            distanceKm = 0.0,
            hours = "Open until 9:00 pm"
        ),
        HomePoi(
            id = 3L,
            name = "Harbourfront Centre",
            distanceKm = 0.0,
            hours = "Open until 11:00 pm"
        )
    )

    fun getAll(): List<HomePoi> = demo
}
