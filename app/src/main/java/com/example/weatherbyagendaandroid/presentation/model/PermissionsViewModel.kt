package com.example.weatherbyagendaandroid.presentation.model

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbyagendaandroid.helpers.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val locationHelper: LocationHelper
) : ViewModel() {
    private val _systemLocationGranted = MutableStateFlow<LocationHelper.LocationPermissionStatus>(
        LocationHelper.LocationPermissionStatus.PermissionCheckInProgress
    )
    val systemLocationGranted = _systemLocationGranted.asStateFlow()

    init {
        viewModelScope.launch {
            locationHelper.checkLocationPermissions {
                _systemLocationGranted.value = it
            }
        }
    }

    fun recheckLocationPermissions() {
        _systemLocationGranted.value =
            LocationHelper.LocationPermissionStatus.PermissionCheckInProgress

        viewModelScope.launch {
            locationHelper.checkLocationPermissions {
                _systemLocationGranted.value = it
            }
        }
    }

    fun checkNotificationPermissions(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        return true
    }
}