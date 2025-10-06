package com.example.huntquest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MapFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_map, container, false)

        // --- Buttons from fragment_map.xml ---
        val btnDirections: View = v.findViewById(R.id.btnDirections)
        val btnFullscreen: View = v.findViewById(R.id.btnFullscreen)

        // Open the Directions screen (UI-only screen we added in nav_graph.xml)
        btnDirections.setOnClickListener {
            findNavController().navigate(R.id.directionsFragment)
        }

        // Placeholder for Full Screen View (wire later to another destination)
        btnFullscreen.setOnClickListener {
            Toast.makeText(requireContext(), "Full screen view coming soon", Toast.LENGTH_SHORT)
                .show()
            // Example (when you add it):
            // findNavController().navigate(R.id.fullScreenMapFragment)
        }

        return v
    }

    companion object
}
