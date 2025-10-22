//package com.example.huntquest
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.RatingBar
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//
//class AddPoiFragment : Fragment() {
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        val v = inflater.inflate(R.layout.fragment_add_poi, container, false)
//
//        val rating = v.findViewById<RatingBar>(R.id.ratingBar)
//        v.findViewById<View>(R.id.btnSave).setOnClickListener {
//            // TODO: read text fields and add to list; for now, just a toast
//            Toast.makeText(requireContext(),
//                "Saved with rating ${rating.rating.toInt()}/5", Toast.LENGTH_SHORT).show()
//
//            // Go back to Home (or pop):
//            findNavController().popBackStack()
//        }
//
//        v.findViewById<View>(R.id.btnCancel).setOnClickListener {
//            findNavController().popBackStack()
//        }
//
//        return v
//    }
//}

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

class AddPoiFragment : Fragment() {

    private val vm: HomeViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_add_poi, container, false)

        val etName: EditText = v.findViewById(R.id.etName)
        val etAddress: EditText = v.findViewById(R.id.etAddress)
        val ratingBar: RatingBar = v.findViewById(R.id.ratingBar)

        v.findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            val name = etName.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val rating = ratingBar.rating

            if (name.isNotEmpty()) {
                vm.addPoiWithAddress(name, address, rating)
                findNavController().popBackStack() // back to Home -> list updates automatically
            }
        }

        v.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            findNavController().popBackStack()
        }

        return v
    }
}

