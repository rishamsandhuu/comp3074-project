package com.example.huntquest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.huntquest.TeamPrefsRepository
import com.example.huntquest.TeamMember
import com.google.android.material.button.MaterialButton

class EditMemberFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edit_member, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val repo = TeamPrefsRepository(requireContext().applicationContext)

        // IDs must match fragment_edit_member.xml (etName/etPhone/etEmail)
        val etName  = view.findViewById<EditText>(R.id.etName)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)

        // Expect a "memberId" argument when navigating from TeamFragment
        val memberId = arguments?.getString("memberId")

        val members = repo.load().toMutableList()
        val index = members.indexOfFirst { it.id == memberId }
        val existing: TeamMember? = members.getOrNull(index)

        // Pre-fill fields
        existing?.let {
            etName.setText(it.name)
            etPhone.setText(it.phone)
            etEmail.setText(it.email)
        }

        btnSave.setOnClickListener {
            if (index >= 0 && existing != null) {
                members[index] = existing.copy(
                    name = etName.text.toString().trim(),
                    phone = etPhone.text.toString().trim(),
                    email = etEmail.text.toString().trim()
                )
                repo.save(members)
            }
            findNavController().popBackStack()
        }
    }
}
