package com.example.weatherbyagendaandroid.presentation.domain

import com.example.weatherbyagendaandroid.dao.domain.WeatherPeriod
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WindFilter(var lowerWindSpeed: Int = -1, var higherWindSpeed: Int = Int.MAX_VALUE) : WeatherFilter {

    override fun filter(elementToFilter: WeatherPeriod): Boolean {
        return if(lowerWindSpeed <= -1 && higherWindSpeed == Int.MAX_VALUE) {
            return false
        } else {
            elementToFilter.windSpeedNumber !in lowerWindSpeed..higherWindSpeed
        }
    }
}
