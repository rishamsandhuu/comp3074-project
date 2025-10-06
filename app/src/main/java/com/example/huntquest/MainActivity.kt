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

        // NavController from the NavHost in the layout
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        // Hook BottomNavigationView to Navigation component
        NavigationUI.setupWithNavController(bottomNav, navController)

        // Update the top bar title and show a back arrow on Map/Team
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    toolbar.title = "Home"
                    toolbar.navigationIcon = null // no back on Home
                }
                R.id.mapFragment -> {
                    toolbar.title = "Old Mill"
                    toolbar.setNavigationIcon(R.drawable.huntquest_arrow_back)
                    toolbar.setNavigationOnClickListener {
                        navController.navigate(R.id.homeFragment)
                    }
                }
                R.id.teamFragment -> {
                    toolbar.title = "Team"
                    toolbar.setNavigationIcon(R.drawable.huntquest_arrow_back)
                    toolbar.setNavigationOnClickListener {
                        navController.navigate(R.id.homeFragment)
                    }
                }
                R.id.directionsFragment -> {
                        toolbar.title = "Directions"
                        toolbar.setNavigationIcon(R.drawable.huntquest_arrow_back)
                        toolbar.setNavigationOnClickListener {
                            // go back to the Map screen
                            navController.navigate(R.id.mapFragment)
                        }
                        bottomNav.visibility = BottomNavigationView.VISIBLE
                    }

            }
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
