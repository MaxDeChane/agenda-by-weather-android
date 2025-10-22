package com.example.weatherbyagendaandroid.dao.domain

import java.time.LocalDateTime
import java.time.OffsetDateTime

data class WeatherProperties(val units: String, val generatedAt: OffsetDateTime, val updateTime: OffsetDateTime,
                             val periods: List<WeatherPeriod>) {

    fun needsUpdating(updatedGeneratedTime: OffsetDateTime, updatedUpdateTime: OffsetDateTime): Boolean {
        return updatedGeneratedTime != generatedAt || updatedUpdateTime != updateTime
    }

    fun retrievePeriodsInTimeRange(startDateTime: LocalDateTime, endDateTime: LocalDateTime): List<WeatherPeriod> {
        val periodsOffset = periods[0].startTime.offset
        val offSetStartDateTime = OffsetDateTime.of(startDateTime, periodsOffset)
        val offSetEndDateTime = OffsetDateTime.of(endDateTime, periodsOffset)

        return periods.filter {
            offSetStartDateTime <= it.startTime && it.startTime < offSetEndDateTime
        }
    }
}
