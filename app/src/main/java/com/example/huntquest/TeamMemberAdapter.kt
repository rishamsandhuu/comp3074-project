package com.example.huntquest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TeamMemberAdapter(
    private val onCall: (TeamMember) -> Unit,
    private val onEmail: (TeamMember) -> Unit,
    private val onEdit: (TeamMember) -> Unit,
    private val onRemove: (TeamMember) -> Unit
) : RecyclerView.Adapter<TeamMemberAdapter.ViewHolder>() {

    private val members = mutableListOf<TeamMember>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team_member, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(members[position])
    }

    override fun getItemCount(): Int = members.size

    fun replaceAll(newMembers: List<TeamMember>) {
        members.clear()
        members.addAll(newMembers)
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtAvatar: TextView = view.findViewById(R.id.txtAvatar)
        private val txtName: TextView = view.findViewById(R.id.txtName)
        private val btnPhone: ImageButton = view.findViewById(R.id.btnPhone)
        private val btnEmail: ImageButton = view.findViewById(R.id.btnEmail)
        private val btnEdit: Button = view.findViewById(R.id.btnEdit)
        private val btnRemove: Button = view.findViewById(R.id.btnRemove)

        fun bind(member: TeamMember) {
            txtName.text = member.name

            // Up to 2 initials (fallback "?")
            val initials = member.name
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }
                .map { it.first().uppercaseChar() }
                .take(2)
                .joinToString("")
                .ifEmpty { "?" }
            txtAvatar.text = initials

            btnPhone.setOnClickListener { onCall(member) }
            btnEmail.setOnClickListener { onEmail(member) }
            btnEdit.setOnClickListener { onEdit(member) }
            btnRemove.setOnClickListener { onRemove(member) }
        }
    }
}
