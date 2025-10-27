package com.example.huntquest

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.huntquest.data.PoiRepository
import kotlinx.coroutines.launch

class ActivityDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        val menuButton = findViewById<ImageButton>(R.id.menu_button)
        val headerTitle = findViewById<TextView>(R.id.title)
        val headerAddress = findViewById<TextView>(R.id.address)
        val txtTask = findViewById<TextView>(R.id.txtTask)
        val txtTags = findViewById<TextView>(R.id.txtTags)

        backButton.setOnClickListener { finish() }
        menuButton.setOnClickListener {
            Toast.makeText(this, "Options not implemented", Toast.LENGTH_SHORT).show()
        }

        val poiId = intent.getLongExtra("poi_id", -1)
        if (poiId == -1L) {
            Toast.makeText(this, "Invalid POI ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            val repo = PoiRepository.get(this@ActivityDetailsActivity)
            val poi = repo.getById(poiId)
            if (poi == null) {
                Toast.makeText(this@ActivityDetailsActivity, "POI not found", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            // Dynamic header update
            headerTitle.text = poi.name
            headerAddress.text = poi.address ?: "No address available"

            // Task description
            txtTask.text = poi.task ?: "No description available"
            txtTags.text = if (!poi.tagsCsv.isNullOrBlank()) poi.tagsCsv else "No tags"
        }
    }
}