package com.example.weatherbyagendaandroid.dao.repository

import android.content.Context
import com.example.weatherbyagendaandroid.dao.SavedAgendaItemsDao
import com.example.weatherbyagendaandroid.dao.SavedLocationsDao
import com.example.weatherbyagendaandroid.dao.WeatherFilterGroupsDao
import com.example.weatherbyagendaandroid.domain.agenda.AgendaItem
import com.example.weatherbyagendaandroid.domain.agenda.AgendaItems
import com.example.weatherbyagendaandroid.enums.LoadingStatusEnum
import com.example.weatherbyagendaandroid.presentation.domain.SavedLocation
import com.example.weatherbyagendaandroid.presentation.domain.SavedLocations
import com.example.weatherbyagendaandroid.presentation.domain.WeatherFilterGroup
import com.example.weatherbyagendaandroid.presentation.domain.WeatherFilterGroups
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuOptionsRepository @Inject constructor(
    private val savedAgendaItemsDao: SavedAgendaItemsDao,
    private val savedLocationsDao: SavedLocationsDao,
    private val weatherFilterGroupsDao: WeatherFilterGroupsDao,
    private val selectedMenuOptionsRepository: SelectedMenuOptionsRepository
) {

    private val _agendaItemsLoadingStatus = MutableStateFlow(LoadingStatusEnum.LOADING)
    val agendaItemsLoadingStatus = _agendaItemsLoadingStatus.asStateFlow()
    private val _agendaItems = MutableStateFlow(AgendaItems())
    val agendaItems = _agendaItems.asStateFlow()
    private val _savedLocationsLoadingStatus = MutableStateFlow(LoadingStatusEnum.LOADING)
    val savedLocationsLoadingStatus = _savedLocationsLoadingStatus.asStateFlow()
    private val _savedLocations = MutableStateFlow(SavedLocations())
    val savedLocations = _savedLocations.asStateFlow()
    private val _weatherFilterGroupsLoadingStatus = MutableStateFlow(LoadingStatusEnum.LOADING)
    val weatherFilterGroupsLoadingStatus = _weatherFilterGroupsLoadingStatus.asStateFlow()
    private val _weatherFilterGroups = MutableStateFlow(WeatherFilterGroups())
    val weatherFilterGroups = _weatherFilterGroups.asStateFlow()

    suspend fun loadAgendaItems(context: Context) {
        // If this has already been loaded then just return.
        if(_agendaItemsLoadingStatus.value == LoadingStatusEnum.DONE) {
            return
        }

        val agendaItems =
            savedAgendaItemsDao.retrieveAgendaItems(context)

        if(agendaItems != null && agendaItems.hasItems()) {
            _agendaItems.value = agendaItems
        }

        _agendaItemsLoadingStatus.value = LoadingStatusEnum.DONE
    }

    suspend fun addAgendaItem(agendaItem: AgendaItem, context: Context) {
        _agendaItems.value = agendaItems.value.addAgendaItem(agendaItem)
        savedAgendaItemsDao.saveAgendaItems(context, _agendaItems.value)
    }

    suspend fun deleteAgendaItem(id: Int, context: Context) {
        _agendaItems.value = agendaItems.value.deleteAgendaItem(id)
        savedAgendaItemsDao.saveAgendaItems(context, _agendaItems.value)
    }

    suspend fun loadSavedLocations(context: Context) {
        // If this has already been loaded then just return.
        if(_savedLocationsLoadingStatus.value == LoadingStatusEnum.DONE) {
            return
        }

        val loadedLocations =
            savedLocationsDao.retrieveLocations(context)

        if(loadedLocations != null && loadedLocations.hasSaveLocations()) {
            _savedLocations.value = loadedLocations
        }

        _savedLocationsLoadingStatus.value = LoadingStatusEnum.DONE
    }

    suspend fun addSavedLocation(location: SavedLocation, context: Context): SavedLocation {
        val savedLocation = savedLocations.value.addLocation(location)
        _savedLocations.value = _savedLocations.value.copy(_locations = _savedLocations.value.locations.toMutableMap())
        savedLocationsDao.saveLocations(context, _savedLocations.value)

        return savedLocation
    }

    suspend fun deleteSavedLocation(locationId: Int, context: Context) {
        _savedLocations.value = _savedLocations.value.deleteLocation(locationId)
        savedLocationsDao.saveLocations(context, _savedLocations.value)
    }

    suspend fun updateLocationName(locationId: Int, newLocationName: String, context: Context) {
        _savedLocations.value = _savedLocations.value.updateLocationName(locationId, newLocationName)
        savedLocationsDao.saveLocations(context, _savedLocations.value)
    }

    suspend fun loadWeatherFilterGroups(context: Context) {
        // If this has already been loaded then just return.
        if(_weatherFilterGroupsLoadingStatus.value == LoadingStatusEnum.DONE) {
            return
        }

        val loadedWeatherFilterGroups = weatherFilterGroupsDao.retrieveWeatherFilterGroups(context)

        if(loadedWeatherFilterGroups != null && loadedWeatherFilterGroups.filterGroups.isNotEmpty()) {
            _weatherFilterGroups.value = loadedWeatherFilterGroups
        }

        _weatherFilterGroupsLoadingStatus.value = LoadingStatusEnum.DONE
    }

    suspend fun saveNewWeatherFilterGroup(filterGroupName: String, adhocWeatherFilterGroup: WeatherFilterGroup,
                                          context: Context): Int {
        val updateWeatherFilterGroups = weatherFilterGroups.value.saveNewWeatherFilterGroup(filterGroupName, adhocWeatherFilterGroup)
        _weatherFilterGroups.value = updateWeatherFilterGroups
        weatherFilterGroupsDao.saveWeatherFilterGroups(context, updateWeatherFilterGroups)

        return updateWeatherFilterGroups.filterGroups.values.first { it.name == filterGroupName }.id
    }

    suspend fun updateWeatherFilterGroup(filterGroupId: Int, updatedFilterGroup: WeatherFilterGroup, context: Context) {
        val updatedFilterGroups = weatherFilterGroups.value.updateWeatherFilterGroup(filterGroupId, updatedFilterGroup)

        _weatherFilterGroups.value = updatedFilterGroups
        weatherFilterGroupsDao.saveWeatherFilterGroups(context, updatedFilterGroups)
    }

    suspend fun deleteWeatherFilterGroup(locationId: Int, context: Context) {
        _weatherFilterGroups.value = weatherFilterGroups.value.deleteWeatherFilterGroup(locationId)
        weatherFilterGroupsDao.saveWeatherFilterGroups(context, _weatherFilterGroups.value)
    }

    suspend fun loadMenuItemsIntoMemory(context: Context) {
        // If everything is loaded than no need to retry to load them.
        if(_agendaItemsLoadingStatus.value != LoadingStatusEnum.DONE ||
            _savedLocationsLoadingStatus.value != LoadingStatusEnum.DONE ||
            _weatherFilterGroupsLoadingStatus.value != LoadingStatusEnum.DONE) {
            coroutineScope {
                launch {
                    loadAgendaItems(context)
                }
                launch {
                    loadSavedLocations(context)
                }
                launch {
                    loadWeatherFilterGroups(context)
                }
            }
        }
    }

    suspend fun loadMenuSelectionsFromAgendaItemId(agendaItemId: Int, context: Context) {
        loadMenuItemsIntoMemory(context)

        val agendaItem = _agendaItems.value.items[agendaItemId]
        val location = _savedLocations.value.locations[agendaItem?.locationId]
        val weatherFilterGroup = _weatherFilterGroups.value.filterGroups[agendaItem?.weatherFilterGroupId]
        selectedMenuOptionsRepository.setSelectedAgendaItem(agendaItem)
        selectedMenuOptionsRepository.setSelectedLocationLatLon(
            SelectedMenuOptionsRepository.LocationLatLon.SavedLocationLatLon(
                location!!.latitude, location.longitude))
        selectedMenuOptionsRepository.setCurrentWeatherFilterGroup(weatherFilterGroup?: WeatherFilterGroup())
    }
}