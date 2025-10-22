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

    // New helpers
    fun addPoiWithAddress(name: String, address: String, rating: Float) = viewModelScope.launch {
        // set a sensible default for openUntil if you don't collect it on the form
        repo.addPoiWithAddress(name, openUntil = "Open until 10:00 pm", addressLine = address, rating = rating)
    }

    fun updatePoiAndAddress(poiId: Long, name: String, address: String, rating: Float) = viewModelScope.launch {
        repo.updatePoiAndAddress(poiId, name, openUntil = "Open until 10:00 pm", addressLine = address, rating = rating)
    }

    fun observePoiWithAddress(poiId: Long) = repo.observeWithAddress(poiId).asLiveData()
}
