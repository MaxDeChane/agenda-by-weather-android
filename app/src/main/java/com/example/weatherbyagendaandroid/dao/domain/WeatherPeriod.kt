package com.example.weatherbyagendaandroid.dao.domain

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.weatherbyagendaandroid.R
import java.time.OffsetDateTime

data class WeatherPeriod(val number: Int,
                         val name: String,
                         val startTime: OffsetDateTime,
                         val endTime: OffsetDateTime,
                         val isDaytime: Boolean,
                         val temperature: Int,
                         val temperatureUnit: String,
                         val temperatureTrend: String?,
                         val windSpeed: String,
                         val windDirection: String,
                         val shortForecast: String,
                         val longForecast: String?,
                         val probabilityOfPrecipitation: QuantitativeValue,
                         @Transient var windSpeedNumber: Int = 0,
                         @Transient var startDisplayTime: String = "",
                         @Transient var icon: Int = R.drawable.ic_broken_image,
                         @Transient var iconTint: Color = Color.White,
                         @Transient var isGeneralForecast: Boolean = false,
                         @Transient var textColor: Color = Color.Green,
                         @Transient var temperatureTrendColor: Color = Color.Gray,
                         @Transient var temperatureTrendLineHeight: Dp = 50.dp,
                         @Transient var filtered: Boolean = false,
                         @Transient var backgroundColor: Color = Color.Red)

data class QuantitativeValue(val value: Float, val unitCode: String)
