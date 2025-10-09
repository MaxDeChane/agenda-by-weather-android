package com.example.weatherbyagendaandroid.presentation.model

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(): ViewModel() {
    private val _logTag = "MenuViewModel"

    private val _showMenu =  MutableStateFlow(false)
    val showMenu = _showMenu.asStateFlow()

    // These are in the viewModel here so that if the MenuView is closed and
    // re-opened the state of them is perserved.
    private val _showAgendaItemsExpanded =  MutableStateFlow(true)
    val showAgendaItemsExpanded = _showAgendaItemsExpanded.asStateFlow()
    private val _showWeatherFiltersExpanded =  MutableStateFlow(false)
    val showWeatherFiltersExpanded = _showWeatherFiltersExpanded.asStateFlow()
    private val _showWeatherFilterGroupsExpanded =  MutableStateFlow(false)
    val showWeatherFilterGroupsExpanded = _showWeatherFilterGroupsExpanded.asStateFlow()
    private val _showUpdateLocationExpanded =  MutableStateFlow(false)
    val showUpdateLocationExpanded = _showUpdateLocationExpanded.asStateFlow()

    fun menuButtonClicked() {
        _showMenu.value = !showMenu.value
        Log.i(_logTag, "Menu button has been clicked and show menu is now ${_showMenu.value}")
    }

    fun showAgendaItemsClick() {
        _showAgendaItemsExpanded.value = !showAgendaItemsExpanded.value
        Log.i(_logTag, "Menu button has been clicked and show menu is now ${_showAgendaItemsExpanded.value}")
    }

    fun showWeatherFiltersClick() {
        _showWeatherFiltersExpanded.value = !showWeatherFiltersExpanded.value
        Log.i(_logTag, "Menu button has been clicked and show menu is now ${_showUpdateLocationExpanded.value}")
    }

    fun showWeatherFilterGroupsExpansionClick() {
        _showWeatherFilterGroupsExpanded.value = !showWeatherFilterGroupsExpanded.value
        Log.i(_logTag, "Menu button has been clicked and show menu is now ${_showWeatherFilterGroupsExpanded.value}")
    }

    fun showUpdateLocationExpansionClick() {
        _showUpdateLocationExpanded.value = !showUpdateLocationExpanded.value
        Log.i(_logTag, "Menu button has been clicked and show menu is now ${_showUpdateLocationExpanded.value}")
    }
}