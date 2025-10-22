package com.example.huntquest.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class PoiRepository private constructor(context: Context) {
    private val db = AppDatabase.get(context)
    private val poiDao = db.poiDao()
    private val addrDao = db.addressDao()

    val allPois: Flow<List<Poi>> = poiDao.getAll()

    // Basic ops
    suspend fun upsert(poi: Poi) = poiDao.upsert(poi)
    suspend fun delete(poi: Poi) = poiDao.delete(poi)
    suspend fun getById(id: Long) = poiDao.getById(id)

    // Observe one with address
    fun observeWithAddress(id: Long): Flow<PoiWithAddress?> = poiDao.observeWithAddress(id)

    // Add new POI along with a new address
    suspend fun addPoiWithAddress(
        name: String,
        openUntil: String,
        addressLine: String,
        rating: Float,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        val addressId = addrDao.insert(Address(line = addressLine))
        poiDao.upsert(
            Poi(
                name = name,
                openUntil = openUntil,
                latitude = latitude,
                longitude = longitude,
                rating = rating,
                addressId = addressId
            )
        )
    }

    // Update both poi and its address (creates a new address if it was null)
    suspend fun updatePoiAndAddress(
        poiId: Long,
        name: String,
        openUntil: String,
        addressLine: String,
        rating: Float
    ) {
        val existing = poiDao.getById(poiId) ?: return
        val addrId = existing.addressId ?: addrDao.insert(Address(line = addressLine))
        if (existing.addressId != null) {
            addrDao.update(Address(id = addrId, line = addressLine))
        }
        poiDao.upsert(
            existing.copy(
                name = name,
                openUntil = openUntil,
                rating = rating,
                addressId = addrId
            )
        )
    }

    companion object {
        @Volatile private var INSTANCE: PoiRepository? = null
        fun get(context: Context): PoiRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PoiRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
