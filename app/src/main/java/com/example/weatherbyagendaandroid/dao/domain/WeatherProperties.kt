package com.example.weatherbyagendaandroid.dao.domain

import java.time.OffsetDateTime

data class WeatherProperties(val units: String, val generatedAt: OffsetDateTime, val updateTime: OffsetDateTime,
                             val periods: List<WeatherPeriod>) {

    fun needsUpdating(updatedGeneratedTime: OffsetDateTime, updatedUpdateTime: OffsetDateTime): Boolean {
        return updatedGeneratedTime != generatedAt || updatedUpdateTime != updateTime
    }
}
