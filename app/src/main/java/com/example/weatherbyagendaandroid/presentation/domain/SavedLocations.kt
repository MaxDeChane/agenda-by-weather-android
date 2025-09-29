package com.example.weatherbyagendaandroid.presentation.domain

import android.util.Log
import kotlin.random.Random

data class SavedLocations(val locations: Map<Int, SavedLocation> = emptyMap()) {

    private companion object {
        val LOG_TAG = "SavedLocations"
    }

    fun hasSaveLocations() = locations.isNotEmpty()

    fun addLocation(location: SavedLocation): SavedLocations {
        val mutableLocations = locations.toMutableMap()

        var locationId = Random.nextInt(0, 10000)

        while(mutableLocations.containsKey(locationId)) {
            locationId = Random.nextInt(0, 10000)
        }

        mutableLocations[locationId] = location.copy(id = locationId)
        return this.copy(locations = mutableLocations)
    }

    fun updateLocationName(locationId: Int, newLocationName:String): SavedLocations {
        if(locations.containsKey(locationId)) {
            val locationsByNameCopy = locations.toMutableMap()
            val locationToUpdateName = locationsByNameCopy[locationId]
            locationsByNameCopy[locationId] = locationToUpdateName!!.copy(name = newLocationName)

            return this.copy(locations = locationsByNameCopy)
        }

        Log.e(LOG_TAG, "Unable to find location with name id $locationId")

        return this
    }

    fun deleteLocation(locationId: Int): SavedLocations {
        if(locations.containsKey(locationId)) {
            val locationsByNameCopy = locations.toMutableMap()
            locationsByNameCopy.remove(locationId)
            return this.copy(locations = locationsByNameCopy)
        }

        Log.e(LOG_TAG, "Unable to find location with name $locationId")

        return this
    }

    fun retrieveLocation(locationId: Int): SavedLocation {
        return locations[locationId]!!
    }
}