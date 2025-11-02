package com.example.weatherbyagendaandroid.service

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.weatherbyagendaandroid.R
import com.example.weatherbyagendaandroid.dao.WeatherRepository
import com.example.weatherbyagendaandroid.dao.domain.GridPointProperties
import com.example.weatherbyagendaandroid.dao.domain.WeatherPeriod
import com.example.weatherbyagendaandroid.dao.domain.WeatherProperties
import com.example.weatherbyagendaandroid.domain.weather.WeatherInfo
import com.example.weatherbyagendaandroid.presentation.domain.WeatherPeriodBlock
import com.example.weatherbyagendaandroid.presentation.model.WeatherViewModel.Companion.LOG_TAG
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.withTimeoutOrNull
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherService @Inject constructor(private val weatherRepository: WeatherRepository) {

    suspend fun updateWeatherInfo(latitude: Double, longitude: Double, existingWeatherInfo: WeatherInfo?): WeatherInfo? {
        val gridPoints = weatherRepository.retrieveGridPoints(latitude, longitude)

        // Check if grid ids are different. If they are that means this is a different location
        // so the weather info will need to be reloaded.
        val isDifferentLocation = gridPoints.properties.gridId != existingWeatherInfo?.gridId

        return updateWeatherForecasts(gridPoints.properties, existingWeatherInfo, isDifferentLocation)
    }

    private suspend fun updateWeatherForecasts(gridPointProperties: GridPointProperties, existingWeatherInfo: WeatherInfo?, isDifferentLocation: Boolean): WeatherInfo? {
        var updatedGeneralForecast: WeatherProperties? = null
        var updatedHourlyForecast: WeatherProperties? = null
        var currentTimeWeatherPeriod: WeatherPeriod? = null;

        coroutineScope {
            launch {
                updatedGeneralForecast = withTimeoutOrNull(Duration.ofSeconds(10)) {
                    updateForecastIfNeeded(gridPointProperties.forecastUrl, existingWeatherInfo?.generalForecast,
                        true, isDifferentLocation)
                }
            }

            launch {
                updatedHourlyForecast = withTimeoutOrNull(Duration.ofSeconds(10)) {
                    updateForecastIfNeeded(gridPointProperties.forecastHourlyUrl, existingWeatherInfo?.hourlyForecast,
                        false, isDifferentLocation)
                }

                // If hourly periods were found then set it and get the current
                // weather period from it.
                if(updatedHourlyForecast != null) {
                    currentTimeWeatherPeriod = updateCurrentTimeWeatherPeriod(updatedHourlyForecast!!)
                }
            }
        }

        if(updatedGeneralForecast != null || updatedHourlyForecast != null) {
            val relativeLocation = gridPointProperties.relativeLocation.properties
            val cityStateForDisplay = "${relativeLocation.city}, ${relativeLocation.state}"

            var weatherPeriodDisplayBlocks = emptyList<WeatherPeriodBlock>()
            if(updatedGeneralForecast != null && updatedHourlyForecast != null) {
                weatherPeriodDisplayBlocks =
                    groupWeatherPeriodsInBlocks(updatedGeneralForecast!!.periods, updatedHourlyForecast!!.periods)
            }

            return WeatherInfo(cityStateForDisplay, gridPointProperties.gridId,
                currentTimeWeatherPeriod, updatedGeneralForecast, updatedHourlyForecast,
                weatherPeriodDisplayBlocks)
        }

        return null
    }

    private suspend fun updateForecastIfNeeded(url: String, oldWeatherProperties: WeatherProperties?,
                                               isGeneralForecast: Boolean, isDifferentLocation: Boolean): WeatherProperties? {

        val updatedWeatherProperties = weatherRepository.retrieveWeatherProperties(url)

        if (oldWeatherProperties == null || isDifferentLocation ||
            oldWeatherProperties.needsUpdating(
                updatedWeatherProperties.generatedAt,
                updatedWeatherProperties.updateTime
            )
        ) {

            var previousPeriodTemperature: Int? = null
            var previousPeriodTemperatureLineHeight: Dp = 50.dp
            for (period in updatedWeatherProperties.periods) {
                val periodTemperatureCompareResult =
                    previousPeriodTemperature?.compareTo(period.temperature) ?: 0

                period.windSpeedNumber = retrieveNumericalWindSpeed(period.windSpeed)
                period.icon = determineWeatherIcon(period.shortForecast)
                period.isGeneralForecast = isGeneralForecast
                period.startDisplayTime = createDisplayTime(period.startTime)
                period.backgroundColor =
                    determineBackgroundForWeather(period.shortForecast, period.isDaytime)
                period.textColor = if (period.isDaytime) Color.Black else Color.White
                period.temperatureTrendColor =
                    determineTemperatureLineColor(periodTemperatureCompareResult)
                period.temperatureTrendLineHeight = determineTemperatureLineHeight(
                    periodTemperatureCompareResult,
                    previousPeriodTemperatureLineHeight
                )
                previousPeriodTemperature = period.temperature
                previousPeriodTemperatureLineHeight = period.temperatureTrendLineHeight
            }
            return updatedWeatherProperties
        }

        return oldWeatherProperties
    }

    private fun retrieveNumericalWindSpeed(windSpeedText: String): Int {
        return windSpeedText.split(" ")[0].toInt()
    }

    private fun determineWeatherIcon(shortDescription: String): Int {
        val shortDescriptionLowercased = shortDescription.lowercase()
        return when {
            shortDescriptionLowercased == "cloudy" -> R.drawable.ic_cloudy
            shortDescriptionLowercased.contains("cloudy") -> R.drawable.ic_partly_cloudy_day
            shortDescriptionLowercased.contains("clear") -> R.drawable.ic_clear
            shortDescriptionLowercased.contains("rain") || shortDescription.contains("drizzle") -> R.drawable.ic_rainy
            shortDescriptionLowercased.contains("smoke") -> R.drawable.ic_smoke
            shortDescriptionLowercased.contains("sunny") -> R.drawable.ic_sunny
            shortDescriptionLowercased.contains("thunderstorm") -> R.drawable.ic_thunderstorm
            else -> {
                Log.e(LOG_TAG, "Unable to match $shortDescription with a icon. Using broken image icon as default.")
                return R.drawable.ic_broken_image
            }
        }
    }

    private fun determineTemperatureLineColor(compareToResult: Int): Color {
        return if(compareToResult < 0) {
            Color.Red
        } else if(compareToResult > 0) {
            Color.Blue
        } else {
            Color.Gray
        }
    }

    private fun determineTemperatureLineHeight(compareToResult: Int, previousPeriodTemperatureLineHeight: Dp): Dp {
        return if(compareToResult < 0) {
            previousPeriodTemperatureLineHeight.plus(  2.dp)
        } else if(compareToResult > 0) {
            previousPeriodTemperatureLineHeight.minus(2.dp)
        } else {
            previousPeriodTemperatureLineHeight
        }
    }

    private fun createDisplayTime(timeToFormat: OffsetDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("h a", Locale.US)

        return timeToFormat.format(formatter)
    }

    private fun determineBackgroundForWeather(shortForecast: String, isDaytime: Boolean): Color {
        val forecast = shortForecast.lowercase()

        return when {
            "clear" in forecast || "sunny" in forecast ->
                if (isDaytime) Color(0xFFFFF9C4) else Color(0xFF0D47A1)

            "cloudy" in forecast ->
                if (isDaytime) Color(0xFFE0E0E0) else Color(0xFF455A64)

            "rain" in forecast || "showers" in forecast || "drizzle" in forecast ->
                if (isDaytime) Color(0xFFB3E5FC) else Color(0xFF37474F)

            "snow" in forecast || "flurries" in forecast ->
                if (isDaytime) Color(0xFFFFFFFF) else Color(0xFF90A4AE)

            "thunder" in forecast || "storm" in forecast ->
                if (isDaytime) Color(0xFFFFCC80) else Color(0xFF212121)

            "fog" in forecast || "mist" in forecast || "haze" in forecast || "smoke" in forecast ->
                if (isDaytime) Color(0xFFCFD8DC) else Color(0xFF546E7A)

            "wind" in forecast || "breezy" in forecast ->
                if (isDaytime) Color(0xFFB2EBF2) else Color(0xFF607D8B)

            else -> Color.Red
        }
    }

    private fun updateCurrentTimeWeatherPeriod(hourlyForecast: WeatherProperties): WeatherPeriod? {
        val currentTime = OffsetDateTime.now()
        for(period in hourlyForecast.periods) {
            if(currentTime >= period.startTime && currentTime <= period.endTime) {
                return period
            }
        }

        return null
    }

    private fun groupWeatherPeriodsInBlocks(generalPeriods: List<WeatherPeriod>, hourlyPeriods: List<WeatherPeriod>): List<WeatherPeriodBlock> {
        val blocks = mutableListOf<WeatherPeriodBlock>()
        var currentBlockEndTime: OffsetDateTime
        var currentHourlyPeriodIndex = 0

        if (generalPeriods.isNotEmpty() && hourlyPeriods.isNotEmpty()) {
            for (generalPeriod in generalPeriods) {
                currentBlockEndTime = generalPeriod.endTime

                val blockHourlyPeriods = mutableListOf<WeatherPeriod>()
                for (j in currentHourlyPeriodIndex..<hourlyPeriods.size) {
                    val currentHourlyPeriod = hourlyPeriods[j]

                    if (currentHourlyPeriod.startTime < currentBlockEndTime) {
                        blockHourlyPeriods.add(currentHourlyPeriod)
                    } else {
                        blocks.add(WeatherPeriodBlock(generalPeriod, blockHourlyPeriods))
                        currentHourlyPeriodIndex = j
                        break
                    }
                }
            }
        }

        return blocks
    }
}