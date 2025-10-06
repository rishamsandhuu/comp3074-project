package com.example.huntquest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class AddPoiFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_add_poi, container, false)

        val rating = v.findViewById<RatingBar>(R.id.ratingBar)
        v.findViewById<View>(R.id.btnSave).setOnClickListener {
            // TODO: read text fields and add to list; for now, just a toast
            Toast.makeText(requireContext(),
                "Saved with rating ${rating.rating.toInt()}/5", Toast.LENGTH_SHORT).show()

            // Go back to Home (or pop):
            findNavController().popBackStack()
        }

        v.findViewById<View>(R.id.btnCancel).setOnClickListener {
            findNavController().popBackStack()
        }

        return v
    }
}
