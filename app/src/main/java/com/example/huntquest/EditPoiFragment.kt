package com.example.huntquest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.huntquest.ui.home.HomeViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar

class EditPoiFragment : Fragment() {

    private val vm: HomeViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

    private var poiId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        poiId = requireArguments().getLong("poiId", -1L)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_edit_poi, container, false)

        val etName: EditText = v.findViewById(R.id.etName)
        val etAddress: EditText = v.findViewById(R.id.etAddress)
        val ratingBar: RatingBar = v.findViewById(R.id.ratingBar)
        val chipGroup: ChipGroup = v.findViewById(R.id.chipGroupTags)
        val btnSave: MaterialButton = v.findViewById(R.id.btnSave)
        val btnCancel: MaterialButton = v.findViewById(R.id.btnCancel)

        vm.observePoi(poiId).observe(viewLifecycleOwner) { poi ->
            poi ?: return@observe
            etName.setText(poi.name)
            etAddress.setText(poi.address.orEmpty())
            ratingBar.rating = poi.rating

            val selected = poi.tagsCsv
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()

            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as Chip
                chip.isChecked = chip.text.toString() in selected
            }
        }

        btnSave.setOnClickListener {
            Snackbar.make(v, "Saving…", Snackbar.LENGTH_SHORT).show()

            val name = etName.text.toString().trim()
            val address = etAddress.text.toString().trim().ifEmpty { null }
            val rating = ratingBar.rating

            val tagCsv = buildList {
                for (i in 0 until chipGroup.childCount) {
                    val chip = chipGroup.getChildAt(i) as Chip
                    if (chip.isChecked) add(chip.text.toString())
                }
            }.joinToString(",")

            if (poiId <= 0L) {
                Snackbar.make(v, "Oops: invalid POI id", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (name.isEmpty()) {
                Snackbar.make(v, "Please enter a name", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            vm.updatePoi(
                poiId = poiId,
                name = name,
                rating = rating,
                address = address,
                tagCsv = tagCsv
            )

            Snackbar.make(v, "POI updated", Snackbar.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        btnCancel.setOnClickListener { findNavController().popBackStack() }

        return v
    }
}
