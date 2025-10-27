package com.example.weatherbyagendaandroid.dao.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelectedMenuOptionsRepository @Inject constructor() {

    sealed class LocationLatLon {
        object GpsLatLon: LocationLatLon()
        class SavedLocationLatLon(val latitude: Double, val longitude: Double): LocationLatLon()
    }

    // Used to update what location to get the weather info from.
    private val _selectedLocationLatLon = MutableStateFlow<LocationLatLon>(LocationLatLon.GpsLatLon)
    val selectedLocationLatLon = _selectedLocationLatLon.asStateFlow()

    fun setSelectedLocationLatLon(locationLatLong: LocationLatLon) {
        _selectedLocationLatLon.value = locationLatLong
    }
}