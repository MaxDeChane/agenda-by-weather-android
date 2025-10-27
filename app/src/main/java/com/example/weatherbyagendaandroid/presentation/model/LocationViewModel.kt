package com.example.weatherbyagendaandroid.presentation.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbyagendaandroid.config.CityDatabase
import com.example.weatherbyagendaandroid.dao.SavedLocationsDao
import com.example.weatherbyagendaandroid.dao.entites.City
import com.example.weatherbyagendaandroid.dao.repository.SelectedMenuOptionsRepository
import com.example.weatherbyagendaandroid.enums.LoadingStatusEnum
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
    private val cityDatabase: CityDatabase,
    private val savedLocationsDao: SavedLocationsDao,
    private val selectedMenuOptionsRepository: SelectedMenuOptionsRepository
): ViewModel() {

    private val _loadingStatus = MutableStateFlow(LoadingStatusEnum.LOADING)
    val loadingStatus = _loadingStatus.asStateFlow()

    private val _selectedSavedLocation = MutableStateFlow<SavedLocation?>(null)
    val selectedSavedLocation = _selectedSavedLocation.asStateFlow()

    private val _cityOptions = MutableStateFlow<List<City>>(listOf())
    val cityOptions = _cityOptions.asStateFlow()

    private val _savedLocations = MutableStateFlow(SavedLocations())
    val savedLocations = _savedLocations.asStateFlow()

    init {
        viewModelScope.launch {
            val loadedLocations =
                savedLocationsDao.retrieveLocations(context)

            if(loadedLocations != null && loadedLocations.hasSaveLocations()) {
                _savedLocations.value = loadedLocations
            }

            _loadingStatus.value = LoadingStatusEnum.DONE
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
        if (selectedSavedLocation.value == null ||
            selectedSavedLocation.value!!.id != locationId
        ) {
            val selectedLocation = savedLocations.value.retrieveLocation(locationId)
            _selectedSavedLocation.value = selectedLocation
            // Set the selected lat lon to be used to retrieve and display weather info
            selectedMenuOptionsRepository.setSelectedLocationLatLon(
                SelectedMenuOptionsRepository.LocationLatLon.SavedLocationLatLon(
                    selectedLocation.latitude,
                    selectedLocation.longitude
                )
            )
        } else {
            _selectedSavedLocation.value = null
            selectedMenuOptionsRepository.setSelectedLocationLatLon(SelectedMenuOptionsRepository.LocationLatLon.GpsLatLon)
        }
    }

    fun addSavedLocation(location: SavedLocation) {
        val savedLocation = savedLocations.value.addLocation(location)
        _savedLocations.value = _savedLocations.value.copy(_locations = _savedLocations.value.locations.toMutableMap())
        selectLocation(savedLocation.id)
        _cityOptions.value = listOf()

        viewModelScope.launch {
            savedLocationsDao.saveLocations(context, _savedLocations.value)
        }
    }

    fun deleteSavedLocation(locationId: Int) {
        _savedLocations.value = savedLocations.value.deleteLocation(locationId)
        if(_selectedSavedLocation.value?.id == locationId) {
            selectLocation(locationId)
        }

        viewModelScope.launch {
            savedLocationsDao.saveLocations(context, _savedLocations.value)
        }
    }

    fun updateLocationName(locationId: Int, newLocationName: String) {
        _savedLocations.value = savedLocations.value.updateLocationName(locationId, newLocationName)
    }
}