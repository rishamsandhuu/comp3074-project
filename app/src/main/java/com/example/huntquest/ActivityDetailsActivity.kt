package com.example.huntquest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController

class ActivityDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // Share Button
        val btnShare: Button = findViewById(R.id.btnShare)
        btnShare.setOnClickListener {
            val shareSheet = ShareBottomSheet()
            shareSheet.show(supportFragmentManager, "ShareBottomSheet")
        }

        //Back Button
        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


    }

}