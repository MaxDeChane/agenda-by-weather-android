package com.example.weatherbyagendaandroid.presentation.domain

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.util.CollectionUtils
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.launch
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

    fun updateWeatherFilterGroup(filterGroupId: Int, filterGroupName: String, updatedFilterGroup: WeatherFilterGroup): WeatherFilterGroups {
        val mutableFilterGroups = filterGroups.toMutableMap()

        mutableFilterGroups[filterGroupId] = updatedFilterGroup.copy(name = filterGroupName)

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

    fun runWeatherDisplayBlockThroughFilters(weatherPeriodDisplayBlock: WeatherPeriodDisplayBlock,
                                             selectedWeatherFilterGroupNames: Set<Int>,
                                             inProgressFilterGroup: WeatherFilterGroup) {
        val allFilterGroups = selectedWeatherFilterGroupNames.map { filterGroups[it] }.toMutableList()

        if(inProgressFilterGroup.hasFilters()) {
            allFilterGroups.add(inProgressFilterGroup)
        }

        // Reset the filtered periods in block to make sure
        // they get rechecked against current filters.
        weatherPeriodDisplayBlock.resetFiltered()

        if(!CollectionUtils.isEmpty(allFilterGroups)) {
            var anyHourlyPeriodsFiltered = false
            var allHourlyPeriodsFiltered = true
            for(hourlyWeatherPeriod in weatherPeriodDisplayBlock.hourlyWeatherPeriods) {
                for (currentFilterGroup in allFilterGroups) {
                    // check and see if period already filtered since no need to keep
                    // going if already checked
                    if(hourlyWeatherPeriod.filtered) {
                        break
                    }

                    for(currentFilter in currentFilterGroup!!.filtersByName.values) {
                        if (currentFilter.filter(hourlyWeatherPeriod)) {
                            hourlyWeatherPeriod.filtered = true
                            anyHourlyPeriodsFiltered = true
                            // If already filtered then break out since no sense in running through anymore
                            break
                        } else {
                            hourlyWeatherPeriod.filtered = false
                            allHourlyPeriodsFiltered = false
                        }
                    }
                }
            }

            if(allHourlyPeriodsFiltered) {
                weatherPeriodDisplayBlock.isWholeBlockFiltered = true
            } else if(anyHourlyPeriodsFiltered) {
                weatherPeriodDisplayBlock.isPartialBlockFiltered = true
            }
        }
    }
}
