package com.example.huntquest.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.huntquest.data.PoiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PoiRepository.get(app)

    private val userLat = MutableStateFlow<Double?>(null)
    private val userLng = MutableStateFlow<Double?>(null)

    val items: LiveData<List<HomePoi>> =
        combine(repo.allPois, userLat, userLng) { list, lat, lng ->
            list.map { it.toHomePoi(lat, lng) }.sortedBy { it.name }
        }.asLiveData()

    fun setUserLocation(lat: Double?, lng: Double?) {
        userLat.value = lat
        userLng.value = lng
    }

    fun addPoi(
        name: String,
        rating: Float,
        address: String?,
        tagCsv: String,
        task: String
    ) {
        viewModelScope.launch {
            repo.addPoi(name, rating, address, tagCsv, task)
        }
    }

    fun updatePoi(
        poiId: Long,
        name: String,
        rating: Float,
        address: String?,
        tagCsv: String,
        latitude: Double?,
        longitude: Double?,
        task: String
    ) {
        viewModelScope.launch {
            repo.updatePoi(
                poiId = poiId,
                name = name,
                rating = rating,
                address = address,
                tagsCsv = tagCsv,
                latitude = latitude,
                longitude = longitude,
                task = task
            )
        }
    }

    fun observePoi(id: Long) = repo.observeById(id).asLiveData()

    fun removeById(id: Long) {
        viewModelScope.launch {
            repo.getById(id)?.let { repo.delete(it) }
        }
    }
}
