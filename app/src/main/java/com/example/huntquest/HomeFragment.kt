package com.example.huntquest

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyLog.v
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class HomeFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: PoiAdapter
    private lateinit var etSearch: TextInputEditText
    private val pois = mutableListOf<Poi>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
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
        etSearch = v.findViewById(R.id.etSearch)


        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = PoiAdapter(
            pois,
            onEdit = { _, position ->
                val args = bundleOf("poiId" to position.toLong()) // temp ID = list index
                findNavController().navigate(R.id.editPoiFragment, args)
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

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Search button
        v.findViewById<View>(R.id.btnSearch).setOnClickListener {
            etSearch.requestFocus()
        }

        // Floating action button for adding new POI
        v.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            findNavController().navigate(R.id.addPoiFragment)
        }

        return v
    }
}
