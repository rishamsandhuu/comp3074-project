package com.example.huntquest.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PoiDao {
    @Query("SELECT * FROM pois ORDER BY name")
    fun getAll(): Flow<List<Poi>>

    @Query("SELECT * FROM pois WHERE id = :id")
    suspend fun getById(id: Long): Poi?

    // Observe a single POI (for the Edit screen)
    @Query("SELECT * FROM pois WHERE id = :id")
    fun observeById(id: Long): Flow<Poi?>

    @Upsert
    suspend fun upsert(poi: Poi)

    @Delete
    suspend fun delete(poi: Poi)

    @Query("DELETE FROM pois")
    suspend fun clear()
}
