package com.example.huntquest.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.huntquest.ActivityDetailsActivity
import com.example.huntquest.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: PoiAdapter
    private lateinit var etSearch: TextInputEditText

    // If HomeViewModel extends AndroidViewModel and needs Application, keep this:
    private val vm: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_home, container, false)

        rv = v.findViewById(R.id.rvPois)
        etSearch = v.findViewById(R.id.etSearch)

        // RecyclerView setup
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = PoiAdapter(
            items = mutableListOf(),
            onEdit = { poi, _ ->
                findNavController().navigate(
                    R.id.editPoiFragment,
                    bundleOf("poiId" to poi.id)
                )
            },
            onRemove = { poi, pos ->
                // Optimistic UI remove
                adapter.removeAt(pos)
                Toast.makeText(requireContext(), "Removed ${poi.name}", Toast.LENGTH_SHORT).show()
                // Persist removal
                viewLifecycleOwner.lifecycleScope.launch {
                    vm.removeById(poi.id)
                }
            },
            onItemClick = { poi ->
                startActivity(
                    Intent(requireContext(), ActivityDetailsActivity::class.java)
                        .putExtra("poi_id", poi.id)
                )
            }
        )
        rv.setHasFixedSize(true)
        rv.adapter = adapter   // <-- important: attach the adapter

        // Search box live filter
        etSearch.doOnTextChanged { text, _, _, _ ->
            adapter.filter(text?.toString().orEmpty())
        }

        // Search button just focuses the field for now
        v.findViewById<View>(R.id.btnSearch).setOnClickListener {
            etSearch.requestFocus()
        }

        // FAB: add new POI
        v.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            findNavController().navigate(R.id.addPoiFragment)
        }

        // Observe DB → Adapter
        vm.items.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
        vm.items.observe(viewLifecycleOwner) { list ->
            Log.d("HomeFragment", "Loaded ${list.size} POIs")
            list.forEach { Log.d("HomeFragment", "POI: id=${it.id}, name=${it.name}") }
            adapter.submitList(list)
        }

        return v
    }
}
