package com.example.huntquest

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_HuntQuest)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)

        val lightGreen = ContextCompat.getColor(this, R.color.hq_teal)
        val solidTeal = android.content.res.ColorStateList.valueOf(lightGreen)
        bottomNav.itemIconTintList = solidTeal
        bottomNav.itemTextColor = solidTeal
        bottomNav.itemRippleColor =
            android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
        bottomNav.isItemActiveIndicatorEnabled = false
        com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED.let {
            bottomNav.labelVisibilityMode = it
        }

        // NavController from the NavHost in the layout
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        // Handle Intents coming from ActivityDetailsActivity (Directions / Map)
        handleIncomingIntent(intent)

        // Top app bar menu (About)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_about -> {
                    navController.navigate(R.id.aboutFragment)
                    true
                }

                else -> false
            }
        }

        NavigationUI.setupWithNavController(bottomNav, navController)

        // Update the top bar title + back arrow based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    toolbar.title = "Home"
                    toolbar.navigationIcon = null
                    toolbar.menu.clear()
                    toolbar.inflateMenu(R.menu.menu_home)
                }

                R.id.mapFragment -> {
                    toolbar.title = "MAPS"
                    toolbar.setNavigationIcon(R.drawable.huntquest_arrow_back)
                    toolbar.setNavigationOnClickListener {
                        navController.navigate(R.id.homeFragment)
                    }
                    toolbar.menu.clear()
                }

                R.id.teamFragment -> {
                    toolbar.title = "Team"
                    toolbar.setNavigationIcon(R.drawable.huntquest_arrow_back)
                    toolbar.setNavigationOnClickListener {
                        navController.navigate(R.id.homeFragment)
                    }
                    toolbar.menu.clear()
                }

                R.id.directionsFragment -> {
                    toolbar.title = "Directions"
                    toolbar.setNavigationIcon(R.drawable.huntquest_arrow_back)
                    toolbar.setNavigationOnClickListener {
                        navController.navigate(R.id.mapFragment)
                    }
                    toolbar.menu.clear()
                    bottomNav.visibility = BottomNavigationView.VISIBLE
                }

                R.id.aboutFragment -> {
                    toolbar.title = "About"
                    toolbar.setNavigationIcon(R.drawable.huntquest_arrow_back)
                    toolbar.setNavigationOnClickListener {
                        navController.navigate(R.id.homeFragment)
                    }
                    toolbar.menu.clear()
                }
            }
        }


        onBackPressedDispatcher.addCallback(this) {
            if (navController.currentDestination?.id == R.id.homeFragment) {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            } else {
                navController.navigate(R.id.homeFragment)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }


    private fun handleIncomingIntent(intent: Intent?) {
        intent ?: return

        // --- Open MapFragment focusing a specific POI ---
        var mapPoiId = intent.getLongExtra("navigate_to_map_poi", -1L)
        if (mapPoiId == -1L) {
            mapPoiId = intent.getLongExtra("open_map_single", -1L)
        }

        if (mapPoiId != -1L) {
            navController.navigate(
                R.id.mapFragment,
                bundleOf("poiId" to mapPoiId)
            )
            intent.removeExtra("navigate_to_map_poi")
            intent.removeExtra("open_map_single")
            return
        }

        // --- Open DirectionsFragment with coordinates ---
        val destLat = intent.getDoubleExtra("navigate_to_directions_lat", 0.0)
        val destLng = intent.getDoubleExtra("navigate_to_directions_lng", 0.0)

        if (destLat != 0.0 && destLng != 0.0) {
            navController.navigate(
                R.id.directionsFragment,
                bundleOf(
                    "originLat" to 0f,  // DirectionsFragment can replace this with GPS later
                    "originLng" to 0f,
                    "destLat" to destLat.toFloat(),
                    "destLng" to destLng.toFloat()
                )
            )
            intent.removeExtra("navigate_to_directions_lat")
            intent.removeExtra("navigate_to_directions_lng")
        }
    }
}
