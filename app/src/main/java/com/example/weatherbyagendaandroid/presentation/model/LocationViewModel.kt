package com.example.weatherbyagendaandroid.presentation.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbyagendaandroid.config.CityDatabase
import com.example.weatherbyagendaandroid.dao.entites.City
import com.example.weatherbyagendaandroid.dao.repository.MenuOptionsRepository
import com.example.weatherbyagendaandroid.dao.repository.SelectedMenuOptionsRepository
import com.example.weatherbyagendaandroid.enums.LoadingStatusEnum
import com.example.weatherbyagendaandroid.presentation.domain.SavedLocation
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
    private val menuOptionsRepository: MenuOptionsRepository,
    private val selectedMenuOptionsRepository: SelectedMenuOptionsRepository
): ViewModel() {

    val loadingStatus = menuOptionsRepository.savedLocationsLoadingStatus
    val savedLocations = menuOptionsRepository.savedLocations

    private val _selectedSavedLocation = MutableStateFlow<SavedLocation?>(null)
    val selectedSavedLocation = _selectedSavedLocation.asStateFlow()

    private val _cityOptions = MutableStateFlow<List<City>>(listOf())
    val cityOptions = _cityOptions.asStateFlow()

    init {
        viewModelScope.launch {
            if(loadingStatus.value != LoadingStatusEnum.DONE) {
                menuOptionsRepository.loadSavedLocations(context)
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
        viewModelScope.launch {
            val savedLocation = menuOptionsRepository.addSavedLocation(location, context)
            selectLocation(savedLocation.id)
            _cityOptions.value = listOf()
        }
    }

    fun deleteSavedLocation(locationId: Int) {
        viewModelScope.launch {
            menuOptionsRepository.deleteSavedLocation(locationId, context)
            if (_selectedSavedLocation.value?.id == locationId) {
                selectLocation(locationId)
            }
        }
    }

    fun updateLocationName(locationId: Int, newLocationName: String) {
        viewModelScope.launch {
            menuOptionsRepository.updateLocationName(locationId, newLocationName, context)
        }
    }
}