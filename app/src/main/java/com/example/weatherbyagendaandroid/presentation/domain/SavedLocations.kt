package com.example.weatherbyagendaandroid.presentation.domain

import android.util.Log

data class SavedLocations(val locationsByName: Map<String, SavedLocation> = emptyMap()) {

    private companion object {
        val LOG_TAG = "SavedLocations"
    }

    fun hasSaveLocations() = locationsByName.isNotEmpty()

    fun addLocation(locationName: String, location: SavedLocation): SavedLocations {
        val locationsByNameCopy = locationsByName.toMutableMap()
        locationsByNameCopy[locationName] = location
        return this.copy(locationsByName = locationsByNameCopy)
    }

    fun removeLocation(locationName: String): SavedLocations {
        if(locationsByName.contains(locationName)) {
            val locationsByNameCopy = locationsByName.toMutableMap()
            locationsByNameCopy.remove(locationName)
            return this.copy(locationsByName = locationsByNameCopy)
        }

        Log.e(LOG_TAG, "Unable to find location with name $locationName")

        return this
    }

    fun updateLocationName(oldLocationName: String, newLocationName:String): SavedLocations {
        if(locationsByName.contains(oldLocationName)) {
            val locationsByNameCopy = locationsByName.toMutableMap()
            val locationCopy = locationsByNameCopy[oldLocationName]
            locationsByNameCopy[oldLocationName] = locationCopy!!.copy(name = newLocationName)

            return this.copy(locationsByName = locationsByNameCopy)
        }

        Log.e(LOG_TAG, "Unable to find location with name $oldLocationName")

        return this
    }

    fun retrieveLocation(locationName: String): SavedLocation {
        return locationsByName[locationName]!!
    }

    fun retrieveCityStateOfLocation(locationName: String): String? {
        val location = locationsByName[locationName]

        if(location != null) {
            return location.cityState
        }

        return null
    }
}