package com.example.weatherbyagendaandroid.presentation.domain

import com.example.weatherbyagendaandroid.dao.domain.WeatherPeriod
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherKeywordFilter(val defaultSelectedKeywords: Set<String> = setOf(), val customSelectedKeywords: Set<String> = setOf()) : WeatherFilter {

    override fun filter(elementToFilter: WeatherPeriod): Boolean {
        val allKeywords = defaultSelectedKeywords + customSelectedKeywords
        return allKeywords.isNotEmpty() && allKeywords.firstOrNull { elementToFilter.shortForecast.contains(it) } == null
    }
}
