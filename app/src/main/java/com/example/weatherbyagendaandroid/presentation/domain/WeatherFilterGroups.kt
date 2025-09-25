package com.example.weatherbyagendaandroid.presentation.domain

import android.util.Log
import com.google.android.gms.common.util.CollectionUtils
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherFilterGroups(val filterGroups: MutableMap<String, WeatherFilterGroup> = mutableMapOf()) {

    private companion object {
        const val LOG_TAG = "WeatherFilterGroups"
    }

    fun deleteWeatherFilterGroup(filterGroupName: String): Boolean {
        if(filterGroupName.isNotBlank()) {
            if(filterGroups.remove(filterGroupName) != null) {

                return true
            } else {
                Log.e(LOG_TAG, "$filterGroupName does not exist. This should not happen")
            }
        } else {
            Log.e(LOG_TAG, "No filter group Name passed in. This should not happen")
        }

        return false
    }

    fun runWeatherDisplayBlockThroughFilters(weatherPeriodDisplayBlock: WeatherPeriodDisplayBlock,
                                             selectedWeatherFilterGroupNames: Set<String>,
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
