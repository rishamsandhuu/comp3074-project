package com.example.huntquest.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AddressDao {
    @Insert
    suspend fun insert(address: Address): Long

    @Update
    suspend fun update(address: Address)

    @Query("SELECT * FROM addresses WHERE id = :id")
    suspend fun getById(id: Long): Address?
}
