package com.example.huntquest.team

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.huntquest.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TeamViewModel(app: Application) : AndroidViewModel(app) {
    // Use your DB accessor `get(...)` and call the DAO exposed by AppDatabase
    private val repo: TeamRepository by lazy {
        val db = AppDatabase.get(getApplication())
        TeamRepository(db.teamMemberDao())   // <-- requires AppDatabase to expose this
    }

    // Live list for RecyclerView screens
    val members: LiveData<List<TeamMember>> = repo.all.asLiveData()

    fun add(fullName: String, phone: String, email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.add(TeamMember(fullName = fullName.trim(), phone = phone.trim(), email = email.trim()))
        }
    }

    fun update(id: Long, fullName: String, phone: String, email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.update(TeamMember(id = id, fullName = fullName.trim(), phone = phone.trim(), email = email.trim()))
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch(Dispatchers.IO) { repo.deleteById(id) }
    }

    fun migrateFromPrefs(prefList: List<com.example.huntquest.TeamMember>) {
        viewModelScope.launch(Dispatchers.IO) {
            for (old in prefList) {
                // Check if this member already exists by email
                val existing = repo.findByEmail(old.email)
                if (existing == null) {
                    repo.add(
                        TeamMember(
                            fullName = old.name.trim(),
                            phone = old.phone.trim(),
                            email = old.email.trim()
                        )
                    )
                }
            }
        }
    }

    fun updateByEmail(oldEmail: String, fullName: String, phone: String, email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repo.findByEmail(oldEmail)
            if (current == null) {
                // not found → insert
                repo.add(com.example.huntquest.team.TeamMember(
                    fullName = fullName, phone = phone, email = email
                ))
            } else {
                // found → keep id, update fields
                repo.update(current.copy(
                    fullName = fullName, phone = phone, email = email
                ))
            }
        }
    }

}