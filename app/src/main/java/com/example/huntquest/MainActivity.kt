package com.example.huntquest

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_HuntQuest)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Views
        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)

        // NavController
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        // Bottom nav <-> navController
        NavigationUI.setupWithNavController(bottomNav, navController)

        // Top-right menu click (info)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_about -> {
                    val current = navController.currentDestination?.id
                    if (current == R.id.homeFragment) {
                        navController.navigate(R.id.action_homeFragment_to_aboutFragment)
                    }
                    true
                }
                else -> false
            }
        }

        // Update toolbar and menu per destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    toolbar.title = "Home"
                    toolbar.navigationIcon = null
                    toolbar.menu.clear()
                    toolbar.inflateMenu(R.menu.menu_home)      // show the i icon only on Home
                    bottomNav.visibility = View.VISIBLE
                }
                R.id.mapFragment -> {
                    toolbar.title = "Old Mill"
                    toolbar.menu.clear()
                    toolbar.setNavigationIcon(R.drawable.huntquest_arrow_back)
                    toolbar.setNavigationOnClickListener {
                        navController.navigate(R.id.homeFragment)
                    }
                    bottomNav.visibility = View.VISIBLE
                }
                R.id.teamFragment -> {
                    toolbar.title = "Team"
                    toolbar.menu.clear()
                    toolbar.setNavigationIcon(R.drawable.huntquest_arrow_back)
                    toolbar.setNavigationOnClickListener {
                        navController.navigate(R.id.homeFragment)
                    }
                    bottomNav.visibility = View.VISIBLE
                }
                R.id.directionsFragment -> {
                    toolbar.title = "Directions"
                    toolbar.menu.clear()
                    toolbar.setNavigationIcon(R.drawable.huntquest_arrow_back)
                    toolbar.setNavigationOnClickListener {
                        navController.navigate(R.id.mapFragment)
                    }
                    bottomNav.visibility = View.VISIBLE   // use View.VISIBLE (not BottomNavigationView.VISIBLE)
                }
                R.id.aboutFragment -> {
                    toolbar.title = "About"
                    toolbar.menu.clear()                         // hide the i icon on About
                    toolbar.setNavigationIcon(R.drawable.huntquest_arrow_back)
                    toolbar.setNavigationOnClickListener {
                        navController.popBackStack()
                    }
                    bottomNav.visibility = View.VISIBLE
                }
                else -> {
                    toolbar.menu.clear()
                    toolbar.navigationIcon = null
                    bottomNav.visibility = View.VISIBLE
                }
            }
        }

        // System back: if not on Home, go to Home; else exit
        onBackPressedDispatcher.addCallback(this) {
            if (navController.currentDestination?.id == R.id.homeFragment) {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            } else {
                navController.navigate(R.id.homeFragment)
            }
        }
    }
}
