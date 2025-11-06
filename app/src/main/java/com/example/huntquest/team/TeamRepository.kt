package com.example.huntquest.team

import kotlinx.coroutines.flow.Flow

class TeamRepository(private val dao: TeamMemberDao) {
    val all: Flow<List<TeamMember>> = dao.observeAll()
    suspend fun add(m: TeamMember) = dao.insert(m)
    suspend fun update(m: TeamMember) = dao.update(m)
    suspend fun delete(m: TeamMember) = dao.delete(m)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun findByEmail(email: String) = dao.getByEmail(email)

}