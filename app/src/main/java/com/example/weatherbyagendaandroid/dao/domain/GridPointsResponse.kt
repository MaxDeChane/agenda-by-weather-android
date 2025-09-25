package com.example.weatherbyagendaandroid.dao.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GridPointsResponse(
    val properties: GridPointProperties
)

@JsonClass(generateAdapter = true)
data class GridPointProperties(
    @Json(name = "forecast") val forecastUrl: String,
    @Json(name = "forecastHourly") val forecastHourlyUrl: String,
    @Json(name = "forecastGridData") val gridDataUrl: String?,
    @Json(name = "observationStations") val observationStationsUrl: String?,
    val gridId: String,
    val gridX: Int,
    val gridY: Int,
    val cwa: String,
    val forecastOffice: String,
    val county: String?,
    val timeZone: String? = null,

    @Json(name = "relativeLocation")
    val relativeLocation: RelativeLocation
)

@JsonClass(generateAdapter = true)
data class RelativeLocation(
    val properties: RelativeLocationProperties
)

@JsonClass(generateAdapter = true)
data class RelativeLocationProperties(
    val city: String,
    val state: String
)
