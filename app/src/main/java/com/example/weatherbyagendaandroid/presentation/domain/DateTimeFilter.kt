package com.example.weatherbyagendaandroid.presentation.domain

import com.example.weatherbyagendaandroid.dao.domain.WeatherPeriod
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime
import java.time.OffsetDateTime

data class DateTimeFilter(var startDateTime: LocalDateTime = LocalDateTime.now(),
                          var endDateTime: LocalDateTime = startDateTime) : WeatherFilter {

    @Transient private var currentWeatherPeriodsZoneOffset = OffsetDateTime.now().offset
    @Transient private var startDateTimeOffset = OffsetDateTime.of(startDateTime, currentWeatherPeriodsZoneOffset)
    @Transient private var endDateTimeOffset = OffsetDateTime.of(endDateTime, currentWeatherPeriodsZoneOffset)

    override fun filter(elementToFilter: WeatherPeriod): Boolean {

        // If these are equal, then just return since nothing to filter
        if(startDateTime == endDateTime) {
            return false
        }

        if(currentWeatherPeriodsZoneOffset != elementToFilter.startTime.offset) {
            currentWeatherPeriodsZoneOffset = elementToFilter.startTime.offset
            startDateTimeOffset = OffsetDateTime.of(startDateTime, currentWeatherPeriodsZoneOffset)
            endDateTimeOffset = OffsetDateTime.of(endDateTime, currentWeatherPeriodsZoneOffset)
        }
//
//        val startDateTime = OffsetDateTime.of(startDate, startTime, elementToFilter.startTime.offset)
//        val endDateTime = OffsetDateTime.of(endDate, endTime, elementToFilter.startTime.offset)

        return elementToFilter.endTime < startDateTimeOffset || endDateTimeOffset <= elementToFilter.startTime
    }

}
