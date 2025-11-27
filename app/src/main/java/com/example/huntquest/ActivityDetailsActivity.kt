package com.example.huntquest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.huntquest.data.Poi
import com.example.huntquest.data.PoiRepository
import kotlinx.coroutines.launch

class ActivityDetailsActivity : AppCompatActivity() {

    private var loadedPoi: Poi? = null   // store POI safely

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // --- UI elements ---
        val backButton = findViewById<ImageButton>(R.id.back_button)
        val menuButton = findViewById<ImageButton>(R.id.menu_button)
        val headerTitle = findViewById<TextView>(R.id.title)
        val headerAddress = findViewById<TextView>(R.id.address)
        val txtTask = findViewById<TextView>(R.id.txtTask)
        val txtTags = findViewById<TextView>(R.id.txtTags)

        val btnComplete = findViewById<Button>(R.id.complete_task)
        val btnDirections = findViewById<Button>(R.id.btnDirections)
        val btnMap = findViewById<Button>(R.id.btnMap)
        val btnShare = findViewById<Button>(R.id.btnShare)

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val btnSubmitRating = findViewById<Button>(R.id.btnSubmitRating)

        // Back button
        backButton.setOnClickListener { finish() }

        // Menu button (placeholder)
        menuButton.setOnClickListener {
            Toast.makeText(this, "Options not implemented", Toast.LENGTH_SHORT).show()
        }

        // --- Extract POI id ---
        val poiId = intent.getLongExtra("poi_id", -1)
        if (poiId == -1L) {
            Toast.makeText(this, "Invalid POI ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // --- Load POI details ---
        lifecycleScope.launch {
            val repo = PoiRepository.get(this@ActivityDetailsActivity)
            val poi = repo.getById(poiId)

            if (poi == null) {
                Toast.makeText(this@ActivityDetailsActivity, "POI not found", Toast.LENGTH_SHORT)
                    .show()
                finish()
                return@launch
            }

            loadedPoi = poi

            // Fill UI
            headerTitle.text = poi.name
            headerAddress.text = poi.address ?: "No address available"
            txtTask.text = poi.task ?: "No description available"
            txtTags.text = if (!poi.tagsCsv.isNullOrBlank()) poi.tagsCsv else "No tags"

            ratingBar.rating = poi.rating

            // If already completed, update UI
            if (poi.completed) {
                btnComplete.text = "Completed"
                btnComplete.isEnabled = false
                btnComplete.alpha = 0.6f
            }
        }

        // --- Directions (Google Maps Navigation inside app) ---
        btnDirections.setOnClickListener {
            val poi = loadedPoi ?: return@setOnClickListener
            val lat = poi.latitude ?: return@setOnClickListener
            val lng = poi.longitude ?: return@setOnClickListener

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("navigate_to_directions_lat", lat)
            intent.putExtra("navigate_to_directions_lng", lng)
            startActivity(intent)
        }

        // --- Map button (open MapFragment focusing POI) ---
        btnMap.setOnClickListener {
            val poi = loadedPoi ?: return@setOnClickListener
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("open_map_single", poi.id)
            startActivity(intent)
        }

        // --- Complete Task ---
        btnComplete.setOnClickListener {
            val poi = loadedPoi ?: return@setOnClickListener

            lifecycleScope.launch {
                val repo = PoiRepository.get(this@ActivityDetailsActivity)

                poi.completed = true
                repo.upsert(poi)

                btnComplete.text = "Completed"
                btnComplete.isEnabled = false
                btnComplete.alpha = 0.6f

                Toast.makeText(
                    this@ActivityDetailsActivity,
                    "Task marked as complete!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        // --- Submit Rating ---
        btnSubmitRating.setOnClickListener {
            val poi = loadedPoi ?: return@setOnClickListener
            val newRating = ratingBar.rating

            lifecycleScope.launch {
                val repo = PoiRepository.get(this@ActivityDetailsActivity)
                poi.rating = newRating
                repo.upsert(poi)

                Toast.makeText(
                    this@ActivityDetailsActivity,
                    "Rating submitted!",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
}