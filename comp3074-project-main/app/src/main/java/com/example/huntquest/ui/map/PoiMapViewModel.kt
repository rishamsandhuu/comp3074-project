
package com.example.huntquest.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.huntquest.data.AppDatabase
import com.example.huntquest.data.Poi
import com.example.huntquest.data.PoiRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PoiMapViewModel(app: Application) : AndroidViewModel(app) {

    // Was: PoiRepository(AppDatabase.get(app).poiDao())
    private val repo = PoiRepository(app)  // Application is a Context

    val pois = repo.allWithCoordinates()
        .map { it.sortedBy { p -> p.name } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
