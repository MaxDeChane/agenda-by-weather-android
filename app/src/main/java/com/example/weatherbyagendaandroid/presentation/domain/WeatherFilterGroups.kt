package com.example.weatherbyagendaandroid.presentation.domain

import android.util.Log
import com.google.android.gms.common.util.CollectionUtils
import com.squareup.moshi.JsonClass
import kotlin.random.Random

@JsonClass(generateAdapter = true)
data class WeatherFilterGroups(val filterGroups: Map<Int, WeatherFilterGroup> = mapOf()) {

    private companion object {
        const val LOG_TAG = "WeatherFilterGroups"
    }

    fun saveNewWeatherFilterGroup(filterGroupName: String, newFilterGroup: WeatherFilterGroup): WeatherFilterGroups {

        val mutableFilterGroups = filterGroups.toMutableMap()

        var filterGroupId = Random.nextInt(0, 10000)

        while(mutableFilterGroups.contains(filterGroupId)) {
            filterGroupId = Random.nextInt(0, 10000)
        }
        mutableFilterGroups[filterGroupId] = newFilterGroup.copy(id = filterGroupId, name = filterGroupName)

        return this.copy(filterGroups = mutableFilterGroups.toMap())
    }

    fun updateWeatherFilterGroup(filterGroupId: Int, updatedFilterGroup: WeatherFilterGroup): WeatherFilterGroups {
        val mutableFilterGroups = filterGroups.toMutableMap()

        mutableFilterGroups[filterGroupId] = updatedFilterGroup.copy()

        return this.copy(filterGroups = mutableFilterGroups.toMap())
    }

    fun deleteWeatherFilterGroup(filterGroupId: Int): WeatherFilterGroups {
        if(filterGroups.containsKey(filterGroupId)) {
            val mutableFilterGroups = filterGroups.toMutableMap()
            mutableFilterGroups.remove(filterGroupId)
            return this.copy(filterGroups = mutableFilterGroups.toMap())
        }

        Log.e(LOG_TAG, "$filterGroupId does not exist. This should not happen")
        return this
    }

    fun retrieveWeatherFilterGroup(filterGroupId: Int): WeatherFilterGroup {
        if(filterGroups.containsKey(filterGroupId)) {
            return filterGroups[filterGroupId]!!
        }

        Log.e(LOG_TAG, "$filterGroupId does not exist. This should not happen")
        return WeatherFilterGroup()
    }
}
