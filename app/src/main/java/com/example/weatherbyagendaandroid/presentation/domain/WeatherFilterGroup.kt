package com.example.weatherbyagendaandroid.presentation.domain

import com.example.weatherbyagendaandroid.dao.domain.WeatherPeriod
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherFilterGroup(val id: Int = -1, var name: String = "",
                              val filtersByName: MutableMap<String, WeatherFilter> = mutableMapOf()
) {

    private companion object {
        const val LOG_TAG = "WeatherFilterGroup"
    }

    fun retrieveWeatherFilter(filterClassName: String): WeatherFilter {
        return if(filtersByName.contains(filterClassName)) {
            filtersByName[filterClassName]!!
        } else {
            // If adding a new filter class here, make sure it is added
            // to the Moshi so it is json-ed correctly
            when(filterClassName) {
                DateTimeFilter::class.simpleName -> DateTimeFilter()
                TemperatureFilter::class.simpleName -> TemperatureFilter()
                WindFilter::class.simpleName -> WindFilter()
                WeatherKeywordFilter::class.simpleName -> WeatherKeywordFilter()
                else -> {
                    throw RuntimeException("Unknown weather filter name $filterClassName passed in. " +
                            "Add new branch for it above.")
                }
            }
        }
    }

    fun addWeatherFilter(filterClassName: String, weatherFilter: WeatherFilter): WeatherFilterGroup {
        val copyOfFilters = filtersByName.toMutableMap()
        copyOfFilters[filterClassName] = weatherFilter

        return this.copy(filtersByName = copyOfFilters)
    }

    fun removeWeatherFilter(filterClassName: String): WeatherFilterGroup {
        val copyOfFilters = filtersByName.toMutableMap()
        copyOfFilters.remove(filterClassName)

        return this.copy(filtersByName = copyOfFilters)
    }

    fun hasFilter(filterClassName: String): Boolean {
        return filtersByName.contains(filterClassName)
    }

    fun hasFilters(): Boolean {
        return filtersByName.isNotEmpty()
    }

    fun runWeatherDisplayBlockThroughFilters(weatherPeriodDisplayBlock: WeatherPeriodDisplayBlock) {
        // Reset the filtered periods in block to make sure
        // they get rechecked against current filters.
        weatherPeriodDisplayBlock.resetFiltered()

        if(this.hasFilters()) {
            var anyHourlyPeriodsFiltered = false
            var allHourlyPeriodsFiltered = true
            for(hourlyWeatherPeriod in weatherPeriodDisplayBlock.hourlyWeatherPeriods) {
                // check and see if period already filtered since no need to keep
                // going if already checked
                if(hourlyWeatherPeriod.filtered) {
                    break
                }

                for(currentFilter in filtersByName.values) {
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

            if(allHourlyPeriodsFiltered) {
                weatherPeriodDisplayBlock.isWholeBlockFiltered = true
            } else if(anyHourlyPeriodsFiltered) {
                weatherPeriodDisplayBlock.isPartialBlockFiltered = true
            }
        }
    }

    fun findFirstMatchingWeatherPeriod(weatherPeriods: List<WeatherPeriod>): WeatherPeriod? {
        return weatherPeriods.find { weatherPeriod ->
            // Find the first one that doesn't match the filter
            filtersByName.values.any { !it.filter(weatherPeriod) }
        }
    }
}
