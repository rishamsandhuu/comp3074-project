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

// NEW: add VM imports
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.huntquest.team.TeamViewModel


class EditMemberFragment : Fragment() {

    // NEW: Team ViewModel (Room)
    private val teamVm: TeamViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

    // FIX: keep old email for Room lookup
    private var originalEmail: String? = null

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
            originalEmail = it.email   // <-- remember old email
        }

        btnSave.setOnClickListener {
            val newName  = etName.text.toString().trim()
            val newPhone = etPhone.text.toString().trim()
            val newEmail = etEmail.text.toString().trim()

            // Your existing Prefs update (unchanged)
            if (index >= 0 && existing != null) {
                members[index] = existing.copy(
                    name = newName,
                    phone = newPhone,
                    email = newEmail
                )
                repo.save(members)
            }

            // NEW: also update the Room table.
            // We look up by the *old* email (unique enough in this app) and
            // update to the new values. If not found, we insert.
            val lookupEmail = originalEmail ?: newEmail
            teamVm.updateByEmail(
                oldEmail = lookupEmail,
                fullName = newName,
                phone    = newPhone,
                email    = newEmail
            )

            findNavController().popBackStack()
        }
    }
}
