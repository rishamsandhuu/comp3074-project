package com.example.huntquest

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ShareBottomSheet : BottomSheetDialogFragment() {

    private lateinit var shareText: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_share, container, false)

        // --- RECEIVE DATA FROM ActivityDetailsActivity ---
        val name = arguments?.getString("poi_name") ?: "Unknown Location"
        val address = arguments?.getString("poi_address") ?: "Address unavailable"
        val task = arguments?.getString("poi_task") ?: "No task available"
        val tags = arguments?.getString("poi_tags") ?: "#huntquest"

        // --- SET UI TEXTS ---
        view.findViewById<TextView>(R.id.tvName).text = "Name: $name"
        view.findViewById<TextView>(R.id.tvAddress).text = "Address: $address"
        view.findViewById<TextView>(R.id.tvTask).text = "Task: $task"
        view.findViewById<TextView>(R.id.tvTags).text = tags

        // --- SHARE TEXT ---
        shareText = """
            Check out this HuntQuest challenge!

            📍 $name
            $address

            📝 Task:
            $task

            🏷 Tags: $tags

            Download HuntQuest:
            https://example.com
        """.trimIndent()

        // --- SOCIAL SHARE BUTTONS ---
        view.findViewById<Button>(R.id.btnFacebook).setOnClickListener {
            shareTo("com.facebook.katana")
        }

        view.findViewById<Button>(R.id.btnInstagram).setOnClickListener {
            shareTo("com.instagram.android")
        }

        view.findViewById<Button>(R.id.btnTwitter).setOnClickListener {
            shareTo("com.twitter.android")
        }

        view.findViewById<Button>(R.id.btnEmail).setOnClickListener {
            shareEmail()
        }

        // --- CLOSE BUTTON ---
        view.findViewById<Button>(R.id.back_button).setOnClickListener {
            dismiss()
        }

        return view
    }


    // ---------------------------------------------------------
    // SHARE HELPERS
    // ---------------------------------------------------------

    private fun shareTo(packageName: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            setPackage(packageName)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            // App not installed → fallback
            startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    },
                    "Share using"
                )
            )
        }
    }

    private fun shareEmail() {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_SUBJECT, "HuntQuest Location")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(emailIntent, "Send email"))
    }
}
