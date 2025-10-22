package com.example.huntquest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment

class DirectionsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_directions, container, false)

        v.findViewById<View>(R.id.btnStart).setOnClickListener {
            Toast.makeText(requireContext(), "Starting route (dummy)", Toast.LENGTH_SHORT).show()
        }

        // optional: respond to Driving/Walking taps (UI only)
        v.findViewById<View>(R.id.btnDriving).setOnClickListener { /* highlight handled by selector */ }
        v.findViewById<View>(R.id.btnWalking).setOnClickListener { /* highlight handled by selector */ }

        return v
    }
}
