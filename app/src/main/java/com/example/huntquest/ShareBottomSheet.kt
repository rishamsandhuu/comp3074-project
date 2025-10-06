// ShareBottomSheet.kt
package com.example.huntquest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ShareBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_share, container, false)

        val backButton = view.findViewById<Button>(R.id.back_button)

        // Close the bottom sheet when clicked
        backButton.setOnClickListener {
            dismiss()
        }

        return view
    }
}
