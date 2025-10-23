package com.example.huntquest

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class AboutFragment : Fragment(R.layout.fragment_about) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // No logic needed; toolbar title/back is handled in MainActivity
    }
}
