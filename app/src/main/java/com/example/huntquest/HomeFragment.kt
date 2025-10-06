package com.example.huntquest

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: PoiAdapter
    private val pois = mutableListOf<Poi>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_home, container, false)

        // Dummy POIs
        pois.clear()
        pois.addAll(
            listOf(
                Poi("CN Tower", 2.3, "Open until 10:00 pm"),
                Poi("St. Lawrence Market", 1.5, "Open until 5:00 pm"),
                Poi("High Park", 4.8, "Open until 11:00 pm"),
                Poi("Old Mill", 4.8, "Open until 11:00 pm")
            )
        )

        rv = v.findViewById(R.id.rvPois)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = PoiAdapter(
            pois,
            onEdit = { poi, _ ->
                Toast.makeText(requireContext(), "Edit ${poi.name}", Toast.LENGTH_SHORT).show()
            },
            onRemove = { poi, pos ->
                Toast.makeText(requireContext(), "Removed ${poi.name}", Toast.LENGTH_SHORT).show()
                pois.removeAt(pos)
                adapter.notifyItemRemoved(pos)
            },
            onItemClick = {
                // Open the details activity directly
                val intent = Intent(requireContext(), ActivityDetailsActivity::class.java)
                startActivity(intent)
            }
        )
        rv.adapter = adapter

        // Search button
        v.findViewById<View>(R.id.btnSearch).setOnClickListener {
            Toast.makeText(requireContext(), "Search tapped", Toast.LENGTH_SHORT).show()
        }

        // Floating action button for adding new POI
        v.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            findNavController().navigate(R.id.addPoiFragment)
        }

        return v
    }
}
