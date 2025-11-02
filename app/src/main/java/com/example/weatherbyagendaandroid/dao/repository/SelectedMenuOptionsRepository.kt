package com.example.weatherbyagendaandroid.dao.repository

import com.example.weatherbyagendaandroid.domain.agenda.AgendaItem
import com.example.weatherbyagendaandroid.presentation.domain.WeatherFilterGroup
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

    // Holds the current filter group being created.
    private val _adhocFilterGroup = MutableStateFlow(WeatherFilterGroup())
    val adhocFilterGroup = _adhocFilterGroup.asStateFlow()

    private val _currentWeatherFilterGroup = MutableStateFlow(WeatherFilterGroup())
    val currentWeatherFilterGroup = _currentWeatherFilterGroup.asStateFlow()

    // Used to update what location to get the weather info from.
    private val _selectedAgendaItem = MutableStateFlow<AgendaItem?>(null)
    val selectedAgendaItem = _selectedAgendaItem.asStateFlow()

    fun setSelectedLocationLatLon(locationLatLong: LocationLatLon) {
        _selectedLocationLatLon.value = locationLatLong
    }

    fun setAdhocWeatherFilterGroup(weatherFilterGroup: WeatherFilterGroup) {
        _adhocFilterGroup.value = weatherFilterGroup
    }

    fun setCurrentWeatherFilterGroup(weatherFilterGroup: WeatherFilterGroup) {
        _currentWeatherFilterGroup.value = weatherFilterGroup
    }

    fun setSelectedAgendaItem(agendaItem: AgendaItem?) {
        _selectedAgendaItem.value = agendaItem
    }
}