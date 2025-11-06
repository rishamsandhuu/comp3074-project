package com.example.huntquest.team

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamMemberDao {
    @Query("SELECT * FROM TeamMember ORDER BY id DESC")
    fun observeAll(): Flow<List<TeamMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(m: TeamMember): Long

    @Update
    suspend fun update(m: TeamMember)

    @Delete
    suspend fun delete(m: TeamMember)

    @Query("DELETE FROM TeamMember WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM TeamMember WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TeamMember?

    @Query("SELECT * FROM TeamMember WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): TeamMember?

}