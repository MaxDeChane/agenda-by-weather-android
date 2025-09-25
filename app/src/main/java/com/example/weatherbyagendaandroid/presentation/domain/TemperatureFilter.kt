package com.example.weatherbyagendaandroid.presentation.domain

import com.example.weatherbyagendaandroid.dao.domain.WeatherPeriod
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TemperatureFilter(var lowerTemperature: Int = Int.MIN_VALUE, var higherTemperature: Int = Int.MAX_VALUE) : WeatherFilter {

    override fun filter(elementToFilter: WeatherPeriod): Boolean {
        return elementToFilter.temperature !in lowerTemperature..higherTemperature
    }
}
