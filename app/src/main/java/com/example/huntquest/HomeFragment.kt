//package com.example.huntquest
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//
//class HomeFragment : Fragment() {
//
//    private lateinit var rv: RecyclerView
//    private lateinit var adapter: PoiAdapter
//    private val pois = mutableListOf<Poi>()
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val v = inflater.inflate(R.layout.fragment_home, container, false)
//
//        // Dummy POIs
//        pois.clear()
//        pois.addAll(
//            listOf(
//                Poi("CN Tower", 2.3, "Open until 10:00 pm"),
//                Poi("St. Lawrence Market", 1.5, "Open until 5:00 pm"),
//                Poi("High Park", 4.8, "Open until 11:00 pm"),
//                Poi("Old Mill", 4.8, "Open until 11:00 pm")
//            )
//        )
//
//        rv = v.findViewById(R.id.rvPois)
//        rv.layoutManager = LinearLayoutManager(requireContext())
//        adapter = PoiAdapter(
//            pois,
//            onEdit = { poi, _ ->
//                Toast.makeText(requireContext(), "Edit ${poi.name}", Toast.LENGTH_SHORT).show()
//            },
//            onRemove = { poi, pos ->
//                Toast.makeText(requireContext(), "Removed ${poi.name}", Toast.LENGTH_SHORT).show()
//                pois.removeAt(pos)
//                adapter.notifyItemRemoved(pos)
//            },
//            onItemClick = {
//                // Open the details activity directly
//                val intent = Intent(requireContext(), ActivityDetailsActivity::class.java)
//                startActivity(intent)
//            }
//        )
//        rv.adapter = adapter
//
//        // Search button
//        v.findViewById<View>(R.id.btnSearch).setOnClickListener {
//            Toast.makeText(requireContext(), "Search tapped", Toast.LENGTH_SHORT).show()
//        }
//
//        // Floating action button for adding new POI
//        v.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
//            findNavController().navigate(R.id.addPoiFragment)
//        }
//
//        return v
//    }
//}

package com.example.huntquest

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.huntquest.ui.home.HomeViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// Use the Room entity with an alias so we can still use your UI model named Poi.
import com.example.huntquest.data.Poi as PoiEntity

class HomeFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: PoiAdapter

    // The list your adapter already expects (UI model)
    private val pois = mutableListOf<Poi>()

    // Keep the latest DB entities so we can delete by position
    private var lastEntities: List<PoiEntity> = emptyList()

    // ViewModel that exposes DB data
    private val viewModel: HomeViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_home, container, false)

        rv = v.findViewById(R.id.rvPois)
        rv.layoutManager = LinearLayoutManager(requireContext())

        adapter = PoiAdapter(
            items = pois,
            onEdit = { _: Poi, pos: Int ->
                lastEntities.getOrNull(pos)?.let { entity ->
                    val b = Bundle().apply { putLong("poiId", entity.id) }
                    findNavController().navigate(R.id.editPoiFragment, b)
                }
            },
            onRemove = { _: Poi, pos: Int ->
                lastEntities.getOrNull(pos)?.let { entity -> viewModel.remove(entity) }
            },
            onItemClick = { /* unchanged */ }
        )
        rv.adapter = adapter

        // Observe DB -> map to your UI model -> update adapter
        viewModel.pois.observe(viewLifecycleOwner) { entities: List<PoiEntity> ->
            lastEntities = entities

            val uiList: List<Poi> = entities.map { e: PoiEntity ->
                Poi(
                    e.name,                                               // title
                    computeDistanceFromDowntown(e.latitude, e.longitude), // right-side "X km"
                    e.openUntil                                           // subtitle
                )
            }

            pois.clear()
            pois.addAll(uiList)
            adapter.notifyDataSetChanged()
            // Optional quick check:
            // Toast.makeText(requireContext(), "Loaded ${entities.size} POIs", Toast.LENGTH_SHORT).show()
        }

        // Search button (unchanged)
        v.findViewById<View>(R.id.btnSearch).setOnClickListener {
            Toast.makeText(requireContext(), "Search tapped", Toast.LENGTH_SHORT).show()
        }

        // Floating action button for adding new POI (unchanged navigation)
        v.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            findNavController().navigate(R.id.addPoiFragment)
        }

        return v
    }

    /**
     * Computes distance (km) from a fixed downtown Toronto coordinate to the given lat/lon.
     * If lat/lon are null, returns 0.0. Rounded to 1 decimal to match your UI style.
     */
    private fun computeDistanceFromDowntown(lat: Double?, lon: Double?): Double {
        if (lat == null || lon == null) return 0.0
        val downtownLat = 43.6532
        val downtownLon = -79.3832
        val km = haversineKm(downtownLat, downtownLon, lat, lon)
        return ((km * 10.0).roundToInt()) / 10.0
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}
