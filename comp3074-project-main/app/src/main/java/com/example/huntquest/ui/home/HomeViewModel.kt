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

    // For distance in the Home list (optional; leave null if you don't use it yet)
    private val userLat = MutableStateFlow<Double?>(null)
    private val userLng = MutableStateFlow<Double?>(null)

    // Home screen list (entity -> HomePoi). If you don't use HomePoi, you can expose repo.allPois directly.
    val items: LiveData<List<HomePoi>> =
        combine(repo.allPois, userLat, userLng) { list, lat, lng ->
            list.map { it.toHomePoi(lat, lng) }.sortedBy { it.name }
        }.asLiveData()

    fun setUserLocation(lat: Double?, lng: Double?) {
        userLat.value = lat
        userLng.value = lng
    }

    // ---- API that your teammate's fragments call (names/signatures match) ----

    // AddPoiFragment calls: vm.addPoi(name = ..., rating = ..., address = ..., tagCsv = ...)
    fun addPoi(
        name: String,
        rating: Float,
        address: String?,
        tagCsv: String,
        task: String? = null       // adding new optional parameter for task
    ) {
        viewModelScope.launch {
            repo.addPoi(name, rating, address, tagCsv, task)
        }
    }


    // EditPoiFragment calls: vm.updatePoi(poiId=..., name=..., rating=..., address=..., tagCsv=..., latitude=..., longitude=...)
    fun updatePoi(
        poiId: Long,
        name: String,
        rating: Float,
        address: String?,
        tagCsv: String,
        latitude: Double?,
        longitude: Double?
    ) {
        viewModelScope.launch {
            // Preserve existing lat/lng if nulls are passed; your repo already handles this.
            repo.updatePoi(
                poiId = poiId,
                name = name,
                rating = rating,
                address = address,
                tagsCsv = tagCsv,     // map VM param name -> repo param name
                latitude = latitude,
                longitude = longitude
            )
        }
    }

    // EditPoiFragment uses this to prefill the form
    fun observePoi(id: Long) = repo.observeById(id).asLiveData()

    // Optional helper if you delete from Home list
    fun removeById(id: Long) {
        viewModelScope.launch {
            repo.getById(id)?.let { repo.delete(it) }
        }
    }
}
