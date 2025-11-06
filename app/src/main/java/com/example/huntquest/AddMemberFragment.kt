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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.huntquest.team.TeamViewModel

class AddMemberFragment : Fragment() {

    private val teamVm: TeamViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_member, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val repo = TeamPrefsRepository(requireContext().applicationContext)

        // IDs must match fragment_add_member.xml (etName/etPhone/etEmail)
        val etName  = view.findViewById<EditText>(R.id.etName)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)

        btnSave.setOnClickListener {
            val name  = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()

            // Existing: save to your screen list (SharedPreferences)
            val list = repo.load().toMutableList()
            list.add(
                TeamMember(
                    name = name,
                    role = "", // role not used
                    email = email,
                    phone = phone
                )
            )
            repo.save(list)

            // NEW (minimal): also persist to Room (TeamMember table) for live updates
            teamVm.add(fullName = name, phone = phone, email = email)

            findNavController().popBackStack()
        }
    }
}

