package com.example.huntquest.team

import androidx.room.*

@Entity(tableName = "TeamMember", indices = [Index("email", unique = false)])
data class TeamMember(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "fullName") val fullName: String,
    @ColumnInfo(name = "phone")    val phone: String,
    @ColumnInfo(name = "email")    val email: String,
    @ColumnInfo(name = "createdAt") val createdAt: Long = System.currentTimeMillis()
)
