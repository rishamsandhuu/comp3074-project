package com.example.huntquest.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class PoiRepository private constructor(context: Context) {
    private val dao = AppDatabase.get(context).poiDao()

    val allPois: Flow<List<Poi>> = dao.getAll()

    suspend fun upsert(poi: Poi) = dao.upsert(poi)
    suspend fun delete(poi: Poi) = dao.delete(poi)
    suspend fun getById(id: Long) = dao.getById(id)
    fun observeById(id: Long): Flow<Poi?> = dao.observeById(id)

    // ✅ Add with address string
    suspend fun addPoi(name: String, rating: Float, address: String?) {
        dao.upsert(
            Poi(
                name = name,
                openUntil = "Open until 10:00 pm",
                rating = rating,
                address = address
            )
        )
    }

    // ✅ Update name/rating/address of an existing poi
    suspend fun updatePoi(poiId: Long, name: String, rating: Float, address: String?) {
        val existing = dao.getById(poiId) ?: return
        dao.upsert(existing.copy(name = name, rating = rating, address = address))
    }

    companion object {
        @Volatile private var INSTANCE: PoiRepository? = null
        fun get(context: Context): PoiRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PoiRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
