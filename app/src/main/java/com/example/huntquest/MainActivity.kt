package com.example.huntquest

import com.example.huntquest.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Make sure your normal app theme is applied after the splash
        setTheme(R.style.Theme_HuntQuest)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Grab views
        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)

        /* >>> MAKE BOTTOM NAV ALWAYS LIGHT-GREEN <<< */
        val lightGreen = androidx.core.content.ContextCompat.getColor(this, R.color.hq_teal)
// use a solid ColorStateList so it never changes with state
        val solidTeal = android.content.res.ColorStateList.valueOf(lightGreen)
        bottomNav.itemIconTintList = solidTeal
        bottomNav.itemTextColor   = solidTeal
        bottomNav.itemRippleColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
        bottomNav.isItemActiveIndicatorEnabled = false
        com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED.let {
            bottomNav.labelVisibilityMode = it
        }

        // NavController from the NavHost in the layout
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        // Handle Intents from ActivityDetailsActivity
        intent?.let {
            when {
                it.hasExtra("open_map_single") -> {
                    val poiId = it.getLongExtra("open_map_single", -1)
                    if (poiId != -1L) {
                        val bundle = Bundle().apply { putLong("poiId", poiId) }
                        navController.navigate(R.id.mapFragment, bundle)
                    }
                }

                it.hasExtra("open_directions_single") -> {
                    val poiId = it.getLongExtra("open_directions_single", -1)
                    if (poiId != -1L) {
                        val bundle = Bundle().apply { putLong("destPoiId", poiId) }
                        navController.navigate(R.id.directionsFragment, bundle)
                    }
                }
            }
        }

        // Add this once, near where toolbar is initialized
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_about -> {
                    navController.navigate(R.id.aboutFragment)
                    true
                }
                else -> false
            }
        }

        // Hook BottomNavigationView to Navigation component
        NavigationUI.setupWithNavController(bottomNav, navController)

        // Update the top bar title and show a back arrow on Map/Team
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    toolbar.title = "Home"
                    toolbar.navigationIcon = null // no back on Home

                    // Show the About menu ONLY on Home
                    toolbar.menu.clear()
                    toolbar.inflateMenu(R.menu.menu_home)
                }

                R.id.mapFragment -> {
                    toolbar.title = "MAPS"
                    toolbar.setNavigationIcon(R.drawable.huntquest_arrow_back)
                    toolbar.setNavigationOnClickListener {
                        navController.navigate(R.id.homeFragment)
                    }

                    // Hide menu on non-Home screens
                    toolbar.menu.clear()
                }

                R.id.teamFragment -> {
                    toolbar.title = "Team"
                    toolbar.setNavigationIcon(R.drawable.huntquest_arrow_back)
                    toolbar.setNavigationOnClickListener {
                        navController.navigate(R.id.homeFragment)
                    }

                    // Hide menu on non-Home screens
                    toolbar.menu.clear()
                }

                R.id.directionsFragment -> {
                    toolbar.title = "Directions"
                    toolbar.setNavigationIcon(R.drawable.huntquest_arrow_back)
                    toolbar.setNavigationOnClickListener {
                        // go back to the Map screen
                        navController.navigate(R.id.mapFragment)
                    }
                    // Hide menu on non-Home screens
                    toolbar.menu.clear()
                    bottomNav.visibility = BottomNavigationView.VISIBLE
                }

                R.id.aboutFragment -> {
                    toolbar.title = "About"
                    toolbar.setNavigationIcon(R.drawable.huntquest_arrow_back)
                    toolbar.setNavigationOnClickListener {
                        // Always return to Home from About
                        navController.navigate(R.id.homeFragment)
                    }
                    toolbar.menu.clear() // hide the "i" on About screen
                }
            }
        }

        // --- Open MapFragment focusing a specific POI ---
        val mapPoiId = intent.getLongExtra("navigate_to_map_poi", -1)
        if (mapPoiId != -1L) {
            navController.navigate(
                R.id.mapFragment,
                Bundle().apply { putLong("poiId", mapPoiId) }
            )
        }

        // --- Open DirectionsFragment with coordinates ---
        val destLat = intent.getDoubleExtra("navigate_to_directions_lat", 0.0)
        val destLng = intent.getDoubleExtra("navigate_to_directions_lng", 0.0)

        if (destLat != 0.0 && destLng != 0.0) {
            navController.navigate(
                R.id.directionsFragment,
                Bundle().apply {
                    putFloat("originLat", 0f)   // optional, can update later using GPS
                    putFloat("originLng", 0f)
                    putFloat("destLat", destLat.toFloat())
                    putFloat("destLng", destLng.toFloat())
                }
            )
        }

        // System back: if not on Home, go to Home; else default back (exit)
        onBackPressedDispatcher.addCallback(this) {
            if (navController.currentDestination?.id == R.id.homeFragment) {
                // default back behavior (finish)
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            } else {
                navController.navigate(R.id.homeFragment)
            }
        }
    }
}
