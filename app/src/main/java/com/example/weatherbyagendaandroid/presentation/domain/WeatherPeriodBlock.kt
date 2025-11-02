package com.example.weatherbyagendaandroid.presentation.domain

import com.example.weatherbyagendaandroid.dao.domain.WeatherPeriod

data class WeatherPeriodBlock(val generalWeatherPeriod: WeatherPeriod,
                              val hourlyWeatherPeriods: List<WeatherPeriod> = mutableListOf(),
                              var isWholeBlockFiltered:Boolean = false,
                              var isPartialBlockFiltered:Boolean = false,
) {

    fun resetFiltered() {
        if(isWholeBlockFiltered || isPartialBlockFiltered) {
            generalWeatherPeriod.filtered = false
            hourlyWeatherPeriods.forEach { it.filtered = false }
            isWholeBlockFiltered = false
            isPartialBlockFiltered = false
        }
    }
}
