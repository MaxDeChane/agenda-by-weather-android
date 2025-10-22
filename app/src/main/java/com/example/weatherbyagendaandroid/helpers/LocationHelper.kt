package com.example.weatherbyagendaandroid.helpers

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LocationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {
    private companion object {
        val LOG_TAG = LocationHelper::javaClass.name
    }

    sealed class LocationPermissionStatus {
        object PermissionCheckInProgress: LocationPermissionStatus()
        object AllPermissionsGranted: LocationPermissionStatus()
        data class ResolvableError(val intentSender: IntentSender): LocationPermissionStatus()
        data class CoarseLocationDenied(val locationPermission: String): LocationPermissionStatus()
        data class BackgroundLocationDenied(val locationPermission: String): LocationPermissionStatus()
    }

    private val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)

    fun retrieveCurrentLocation(callBack: (location: Location) -> Unit,
                                coarseLocationDeniedCallback: () -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            coarseLocationDeniedCallback()
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { lastKnowLocation ->
                if (lastKnowLocation != null && !isLocationTooOld(lastKnowLocation)) {
                    // Use cached location
                    callBack(lastKnowLocation)
                } else {
                    // Request fresh location
                    val locationRequest = CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                        .build()

                    val cancellationToken = CancellationTokenSource()
                    val locationTask = fusedLocationClient.getCurrentLocation(locationRequest, cancellationToken.token)
                    locationTask.addOnSuccessListener {
                        callBack(it)
                    }
                }
            }
    }

    // Set old time to be 5 minutes.
    private fun isLocationTooOld(location: Location, maxAgeMillis: Long = 300_000): Boolean {
        val locationTime = location.time
        val currentTime = System.currentTimeMillis()
        return (currentTime - locationTime) > maxAgeMillis
    }

    suspend fun checkLocationPermissions(setLocationPermissionStatus: (locationPermissionStatus: LocationPermissionStatus) -> Unit) =
        withContext(Dispatchers.Default) {
            val locationRequest = LocationRequest
                .Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 300_000)
                .build()

            val locationSettingsRequest = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()

            // Check if the user has allowed the app to use location services.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                setLocationPermissionStatus(LocationPermissionStatus.CoarseLocationDenied(
                    Manifest.permission.ACCESS_COARSE_LOCATION))
            } else {
                try {
                    // Check if the device has location settings on
                    val response =
                        settingsClient.checkLocationSettings(locationSettingsRequest).await()

                    setLocationPermissionStatus(LocationPermissionStatus.AllPermissionsGranted)
                } catch (exception: ApiException) {
                    when (exception.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            Log.e(
                                LOG_TAG,
                                "Unable to get location services exception ${exception.cause}"
                            )

                            val resolvableApiException = exception as ResolvableApiException
                            setLocationPermissionStatus(
                                LocationPermissionStatus.ResolvableError(
                                    resolvableApiException.resolution.intentSender
                                )
                            )
                        }
                    }
                }
            }
        }
}
