package com.example.huntquest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TeamFragment : Fragment(R.layout.fragment_team) {

    // Data + UI
    private lateinit var repo: TeamPrefsRepository
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: TeamMemberAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repo = TeamPrefsRepository(requireContext().applicationContext)

        // --- RecyclerView setup ---
        recycler = view.findViewById(R.id.teamRecycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = TeamMemberAdapter(
            onCall = { member -> openDialer(member.phone) },
            onEmail = { member -> composeEmail(member.email, member.name) },
            onEdit = { member ->
                findNavController().navigate(
                    R.id.editMemberFragment,
                    bundleOf("memberId" to member.id)
                )
            },
            onRemove = { member ->
                val list = repo.load().toMutableList()
                if (list.removeAll { it.id == member.id }) {
                    repo.save(list)
                    adapter.replaceAll(list)
                }
            }
        )
        recycler.adapter = adapter
        adapter.replaceAll(repo.load())

        // --- FAB: Add Member ---
        view.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            findNavController().navigate(R.id.addMemberFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh after add/edit
        adapter.replaceAll(repo.load())
    }

    // --- Helpers ---

    private fun openDialer(raw: String?) {
        val phone = (raw ?: "").filterNot { it.isWhitespace() }
        if (phone.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            }
            startActivity(intent)
        }
    }

    private fun composeEmail(email: String?, name: String?) {
        val addr = (email ?: "").trim()
        if (addr.isEmpty()) return

        val subject = "Hello ${name ?: ""}".trim()
        val body = "Hi ${name ?: ""},\n\n".trim()

        // Put recipient into the path so Gmail fills "To"
        val uri = Uri.parse("mailto:").buildUpon()
            .appendEncodedPath(addr)
            .appendQueryParameter("subject", subject)
            .appendQueryParameter("body", body)
            .build()

        val sendTo = Intent(Intent.ACTION_SENDTO, uri)
        val pm = requireContext().packageManager
        val gmail = Intent(sendTo).apply { `package` = "com.google.android.gm" }

        when {
            gmail.resolveActivity(pm) != null -> startActivity(gmail)
            sendTo.resolveActivity(pm) != null -> startActivity(sendTo)
            else -> {
                val fallback = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(addr))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                startActivity(Intent.createChooser(fallback, "Send email"))
            }
        }
    }
}
