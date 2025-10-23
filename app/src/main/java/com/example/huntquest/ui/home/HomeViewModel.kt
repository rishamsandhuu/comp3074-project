package com.example.huntquest.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.huntquest.data.Poi
import com.example.huntquest.data.PoiRepository
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PoiRepository.get(app)

    val pois = repo.allPois.asLiveData()

    fun save(poi: Poi) = viewModelScope.launch { repo.upsert(poi) }
    fun remove(poi: Poi) = viewModelScope.launch { repo.delete(poi) }

    fun addPoi(name: String, rating: Float, address: String?) = viewModelScope.launch {
        repo.addPoi(name, rating, address)
    }

    fun updatePoi(poiId: Long, name: String, rating: Float, address: String?) = viewModelScope.launch {
        repo.updatePoi(poiId, name, rating, address)
    }

    fun observePoi(poiId: Long) = repo.observeById(poiId).asLiveData()
}
