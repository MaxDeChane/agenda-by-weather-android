package com.example.weatherbyagendaandroid.presentation.model

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbyagendaandroid.helpers.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class PermissionsViewModel @Inject constructor(private val userPreferencesDataStore: DataStore<Preferences>,
                                               private val locationHelper: LocationHelper) : ViewModel() {
    private val _systemLocationGranted =  MutableStateFlow<LocationHelper.LocationPermissionStatus>(
        LocationHelper.LocationPermissionStatus.PermissionCheckInProgress
    )
    val systemLocationGranted = _systemLocationGranted.asStateFlow()

    private val _DONT_SHOW_RATIONAL = booleanPreferencesKey("dont_show_notification_rational")
    val dontShowRationalFlow: Flow<Boolean> = userPreferencesDataStore.data
        .map { preferences ->
            // No type safety.
            preferences[_DONT_SHOW_RATIONAL] ?: false
        }

    init {
        viewModelScope.launch {
            locationHelper.checkLocationPermissions{
                _systemLocationGranted.value = it
            }
        }
    }

    fun recheckLocationPermissions() {
        _systemLocationGranted.value = LocationHelper.LocationPermissionStatus.PermissionCheckInProgress

        viewModelScope.launch {
            locationHelper.checkLocationPermissions{
                _systemLocationGranted.value = it
            }
        }
    }

    fun updateDontShowRationalFlow(dontShowRational: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.edit { preferences ->
                preferences[_DONT_SHOW_RATIONAL] = dontShowRational
            }
        }
    }
}