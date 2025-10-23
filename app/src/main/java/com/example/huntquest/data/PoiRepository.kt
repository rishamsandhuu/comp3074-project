package com.example.huntquest.data

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Locale

class PoiRepository private constructor(private val context: Context) {
    private val dao = AppDatabase.get(context).poiDao()
    val allPois: Flow<List<Poi>> = dao.getAll()
    suspend fun upsert(poi: Poi) = dao.upsert(poi)
    suspend fun delete(poi: Poi) = dao.delete(poi)
    suspend fun getById(id: Long) = dao.getById(id)
    fun observeById(id: Long): Flow<Poi?> = dao.observeById(id)

    //geocoding an address: Pair<lat, lon>
    @Suppress("DEPRECATION")
    private suspend fun geocode(address: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val res = geocoder.getFromLocationName(address, 1)
            if(!res.isNullOrEmpty()) {
                val a = res.first()
                return@withContext a.latitude to a.longitude
            }
            null
        } catch (_: Exception){
            null
        }
    }

    // Add with address string
    suspend fun addPoi(name: String, rating: Float, address: String?, tagsCsv: String) {
        val latLng: Pair<Double, Double>? =
            if(!address.isNullOrBlank()) geocode(address) else null

        dao.upsert(
            Poi(
                name = name,
                openUntil = "Open until 10:00 pm",
                rating = rating,
                address = address,
                latitude = latLng?.first,
                longitude = latLng?.second,
                tagsCsv = tagsCsv
            )
        )
    }

    // Update name/rating/address of an existing poi
    suspend fun updatePoi(poiId: Long, name: String, rating: Float, address: String?, tagsCsv: String) {
        val existing = dao.getById(poiId) ?: return

        val addressChanged = existing.address != address
        val latLonIfChanged: Pair<Double, Double>? =
            if (addressChanged && !address.isNullOrBlank()) geocode(address) else null

        dao.upsert(existing.copy(
            name = name,
            rating = rating,
            address = address,
            tagsCsv = tagsCsv,
            latitude = if (addressChanged) latLonIfChanged?.first else existing.latitude,
            longitude = if (addressChanged) latLonIfChanged?.second else existing.longitude

        ))
    }

    companion object {
        @Volatile private var INSTANCE: PoiRepository? = null
        fun get(context: Context): PoiRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PoiRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
