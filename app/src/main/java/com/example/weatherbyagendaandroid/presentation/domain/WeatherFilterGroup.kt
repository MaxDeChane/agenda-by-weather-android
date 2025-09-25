package com.example.weatherbyagendaandroid.presentation.domain

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherFilterGroup(var name: String, val filtersByName: MutableMap<String, WeatherFilter> = mutableMapOf()
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
}
