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
        vm.observePoi(poiId).observe(viewLifecycleOwner) { poi ->
            poi?.let {
                etName.setText(it.name)
                ratingBar.rating = it.rating
                etAddress.setText(it.address.orEmpty())   // ✅ prefill address
            }
        }

        v.findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            val name = etName.text.toString().trim()
            val address = etAddress.text.toString().trim().ifEmpty { null }
            val rating = ratingBar.rating

            if (poiId > 0 && name.isNotEmpty()) {
                vm.updatePoi(poiId, name, rating, address)  // ✅ update address too
                findNavController().popBackStack()
            }
        }

        v.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            findNavController().popBackStack()
        }

        return v
    }
}
