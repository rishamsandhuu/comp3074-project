package com.example.huntquest

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ShareBottomSheet : BottomSheetDialogFragment() {

    private lateinit var shareText: String

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_share, container, false)

        //RECEIVE DATA
        val name = arguments?.getString("poi_name") ?: "Unknown Location"
        val address = arguments?.getString("poi_address") ?: "Address unavailable"
        val task = arguments?.getString("poi_task") ?: "No task available"
        val tags = arguments?.getString("poi_tags") ?: "#huntquest"

        //UI TEXT
        view.findViewById<TextView>(R.id.tvName).text = "Name: $name"
        view.findViewById<TextView>(R.id.tvAddress).text = "Address: $address"
        view.findViewById<TextView>(R.id.tvTask).text = "Task: $task"
        view.findViewById<TextView>(R.id.tvTags).text = tags

        //SHARE TEXT
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

        val fb  = view.findViewById<ImageButton>(R.id.btnFacebook)
        val ig  = view.findViewById<ImageButton>(R.id.btnInstagram)
        val tw  = view.findViewById<ImageButton>(R.id.btnTwitter)
        val em  = view.findViewById<ImageButton>(R.id.btnEmail)

        fb.setOnClickListener { shareTo("com.facebook.katana") }
        ig.setOnClickListener { shareTo("com.instagram.android") }
        tw.setOnClickListener { shareTo("com.twitter.android") }
        em.setOnClickListener { shareEmail() }

        // Close
        view.findViewById<Button>(R.id.back_button).setOnClickListener {
            dismiss()
        }

        return view
    }

    private fun shareTo(packageName: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            setPackage(packageName)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
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
