package com.example.weatherbyagendaandroid.presentation.domain

import android.util.Log
import kotlin.random.Random

data class SavedLocations(private val _locations: MutableMap<Int, SavedLocation> = mutableMapOf()) {

    private companion object {
        val LOG_TAG = "SavedLocations"
    }

    // Return an immutable map since we don't want it updated outside of this class.
    val locations: Map<Int, SavedLocation>
        get() = _locations.toMap()

    fun hasSaveLocations() = _locations.isNotEmpty()

    // Since a new id will be created for this, returned the newly added saved location
    // so the id can be retrieved and selected.
    fun addLocation(location: SavedLocation): SavedLocation {
        var locationId = Random.nextInt(0, 10000)

        while(_locations.containsKey(locationId)) {
            locationId = Random.nextInt(0, 10000)
        }

        val locationWithId = location.copy(id = locationId)
        _locations[locationId] = locationWithId

        return locationWithId
    }

    fun updateLocationName(locationId: Int, newLocationName:String): SavedLocations {
        if(_locations.containsKey(locationId)) {
            val locationToUpdateName = _locations[locationId]
            _locations[locationId] = locationToUpdateName!!.copy(name = newLocationName)

            return this.copy(_locations = _locations)
        }

        Log.e(LOG_TAG, "Unable to find location with name id $locationId")

        return this
    }

    fun deleteLocation(locationId: Int): SavedLocations {
        if(_locations.containsKey(locationId)) {
            val copyOfLocations = _locations.toMutableMap()
            copyOfLocations.remove(locationId)
            return this.copy(_locations = copyOfLocations)
        }

        Log.e(LOG_TAG, "Unable to find location with name $locationId")

        return this
    }

    fun retrieveLocation(locationId: Int): SavedLocation {
        return locations[locationId]!!
    }
}