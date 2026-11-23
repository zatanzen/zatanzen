package com.zatanzen.actividades.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zatanzen.actividades.data.ActivityRepository
import com.zatanzen.actividades.data.DemoData
import com.zatanzen.actividades.model.Activity
import com.zatanzen.actividades.model.Category
import com.zatanzen.actividades.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class AppViewModel : ViewModel() {
    val repo = ActivityRepository()

    /** Current time, refreshed every 30 s so expired activities disappear from the map. */
    val now: StateFlow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(30_000)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, System.currentTimeMillis())

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()

    private val _categoryFilter = MutableStateFlow<Category?>(null)
    val categoryFilter: StateFlow<Category?> = _categoryFilter.asStateFlow()

    val visibleActivities: StateFlow<List<Activity>> =
        combine(repo.activities, now, _categoryFilter) { list, t, cat ->
            list.filter { it.isActive(t) && (cat == null || it.category == cat) }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun onLocationResolved(location: LatLng?) {
        if (_userLocation.value != null) return
        val point = location ?: DemoData.DEFAULT_LOCATION
        _userLocation.value = point
        repo.seedAround(point, System.currentTimeMillis())
    }

    fun setCategoryFilter(category: Category?) {
        _categoryFilter.value = category
    }

    fun currentTime() = System.currentTimeMillis()
}
