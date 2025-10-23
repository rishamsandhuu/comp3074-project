
package com.example.huntquest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.huntquest.ui.home.HomeViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar

class AddPoiFragment : Fragment() {

    private val vm: HomeViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_add_poi, container, false)

        val etName: EditText = v.findViewById(R.id.etName)
        val etAddress: EditText = v.findViewById(R.id.etAddress)
        val ratingBar: RatingBar = v.findViewById(R.id.ratingBar)
        val chipGroup: ChipGroup = v.findViewById(R.id.chipGroupTags)
        val btnSave: MaterialButton = v.findViewById(R.id.btnSave)
        val btnCancel: MaterialButton = v.findViewById(R.id.btnCancel)

        ratingBar.rating = 0f

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val address: String? = etAddress.text.toString().trim().ifEmpty { null }
            val rating = ratingBar.rating

            val selectedTags = mutableListOf<String>()
            for (i in 0 until chipGroup.childCount){
                val chip = chipGroup.getChildAt(i) as Chip
                if (chip.isChecked) selectedTags += chip.text.toString()
            }

            val tagsCsv = selectedTags.joinToString(", ")

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            vm.addPoi(
                name = name,
                rating = rating,
                address = address,
                tagCsv = tagsCsv
            )

            Snackbar.make(v, "POI saved successfully", Snackbar.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        return v
    }
}


