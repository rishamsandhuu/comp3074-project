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

class EditPoiFragment : Fragment() {

    private val vm: HomeViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

    private var poiId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        poiId = requireArguments().getLong("poiId", -1L)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_edit_poi, container, false)

        val etName: EditText = v.findViewById(R.id.etName)
        val etAddress: EditText = v.findViewById(R.id.etAddress)
        val ratingBar: RatingBar = v.findViewById(R.id.ratingBar)

        // Prefill existing values
        vm.observePoiWithAddress(poiId).observe(viewLifecycleOwner) { data ->
            data?.let {
                etName.setText(it.poi.name)
                etAddress.setText(it.address?.line.orEmpty())
                ratingBar.rating = it.poi.rating
            }
        }

        v.findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            val name = etName.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val rating = ratingBar.rating

            if (poiId > 0 && name.isNotEmpty()) {
                vm.updatePoiAndAddress(poiId, name, address, rating)
                findNavController().popBackStack()
            }
        }

        v.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            findNavController().popBackStack()
        }

        return v
    }
}
