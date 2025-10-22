package com.example.huntquest.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import androidx.room.Transaction

@Dao
interface PoiDao {
    @Query("SELECT * FROM pois ORDER BY name")
    fun getAll(): Flow<List<Poi>>

    @Query("SELECT * FROM pois WHERE id = :id")
    suspend fun getById(id: Long): Poi?

    @Upsert
    suspend fun upsert(poi: Poi)

    @Delete
    suspend fun delete(poi: Poi)

    @Query("DELETE FROM pois")
    suspend fun clear()

    // Observe a POI plus address in one go
    @Transaction
    @Query("SELECT * FROM pois WHERE id = :id")
    fun observeWithAddress(id: Long): Flow<PoiWithAddress?>
}
