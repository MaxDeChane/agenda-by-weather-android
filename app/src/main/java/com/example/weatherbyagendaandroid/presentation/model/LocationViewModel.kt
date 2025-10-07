package com.example.weatherbyagendaandroid.presentation.model

import android.app.Activity
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbyagendaandroid.config.CityDatabase
import com.example.weatherbyagendaandroid.dao.SavedLocationsDao
import com.example.weatherbyagendaandroid.dao.entites.City
import com.example.weatherbyagendaandroid.helpers.LocationHelper
import com.example.weatherbyagendaandroid.presentation.domain.SavedLocation
import com.example.weatherbyagendaandroid.presentation.domain.SavedLocations
import com.google.android.gms.common.util.CollectionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    locationHelper: LocationHelper,
    private val cityDatabase: CityDatabase,
    private val savedLocationsDao: SavedLocationsDao
): ViewModel() {

    private val _gpsLocation = MutableStateFlow<Location?>(null)
    val gpsLocation = _gpsLocation.asStateFlow()

    private val _selectedSavedLocation = MutableStateFlow<SavedLocation?>(null)
    val selectedSavedLocation = _selectedSavedLocation.asStateFlow()

    private val _cityOptions = MutableStateFlow<List<City>>(listOf())
    val cityOptions = _cityOptions.asStateFlow()

    private val _savedLocations = MutableStateFlow(SavedLocations())
    val savedLocations = _savedLocations.asStateFlow()

    init {
        locationHelper.retrieveCurrentLocation({location ->
            _gpsLocation.value = location
        }){
            // Reload activity. This will kick off the prompting for access.
            (context as Activity).recreate()
        }

        viewModelScope.launch {
            val loadedLocations =
                savedLocationsDao.retrieveLocations(context)

            if(loadedLocations != null && loadedLocations.hasSaveLocations()) {
                _savedLocations.value = loadedLocations
            }
        }
    }

    suspend fun findCityOptionsByName(name: String) {
        val foundCityOptions = mutableListOf<City>()
        if(name.contains(",")) {
            val cityState = name.split(",")
            if(cityState[1] != "") {
                val foundCity = cityDatabase.cityDao().searchCityState(cityState[0].trim(), cityState[1].trim())
                if(foundCity != null) {
                    foundCityOptions.add(foundCity)
                }
            }
        } else {
            foundCityOptions.addAll(cityDatabase.cityDao().searchCity(name))
        }

        if(CollectionUtils.isEmpty(foundCityOptions)) {
            _cityOptions.value = listOf()
        } else {
            _cityOptions.value = foundCityOptions
        }
    }

    fun selectLocation(locationId: Int) {
        if(selectedSavedLocation.value == null ||
            selectedSavedLocation.value!!.id != locationId) {
            _selectedSavedLocation.value = savedLocations.value.retrieveLocation(locationId)
        } else {
            _selectedSavedLocation.value = null
        }
    }

    fun addSavedLocation(location: SavedLocation) {
        _selectedSavedLocation.value = location
        _savedLocations.value = savedLocations.value.addLocation(location)
        _cityOptions.value = listOf()

        viewModelScope.launch {
            savedLocationsDao.saveLocations(context, _savedLocations.value)
        }
    }

    fun deleteSavedLocation(locationId: Int) {
        _savedLocations.value = savedLocations.value.deleteLocation(locationId)
        if(_selectedSavedLocation.value?.id == locationId) {
            _selectedSavedLocation.value = null
        }

        viewModelScope.launch {
            savedLocationsDao.saveLocations(context, _savedLocations.value)
        }
    }

    fun updateLocationName(locationId: Int, newLocationName: String) {
        _savedLocations.value = savedLocations.value.updateLocationName(locationId, newLocationName)
    }
}