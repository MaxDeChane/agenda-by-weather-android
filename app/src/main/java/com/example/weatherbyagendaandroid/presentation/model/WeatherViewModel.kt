package com.example.weatherbyagendaandroid.presentation.model

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbyagendaandroid.R
import com.example.weatherbyagendaandroid.dao.WeatherRepository
import com.example.weatherbyagendaandroid.dao.domain.GridPointsResponse
import com.example.weatherbyagendaandroid.dao.domain.WeatherPeriod
import com.example.weatherbyagendaandroid.dao.domain.WeatherProperties
import com.example.weatherbyagendaandroid.helpers.LocationHelper
import com.example.weatherbyagendaandroid.presentation.domain.WeatherPeriodDisplayBlock
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.withTimeoutOrNull
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(@ApplicationContext private val context: Context,
                                           private val locationHelper: LocationHelper,
                                           private val weatherRepository: WeatherRepository): ViewModel() {

   companion object {
       val LOG_TAG = "WeatherViewModel"
   }

    private val _weatherGridPoints = MutableStateFlow<GridPointsResponse?>(null)
    val weatherGridPoints = _weatherGridPoints.asStateFlow()

    private val _currentTimeWeatherPeriod =  MutableStateFlow<WeatherPeriod?>(null)
    val currentTimeWeatherPeriod = _currentTimeWeatherPeriod.asStateFlow()

    private val _generalForecast = MutableStateFlow<WeatherProperties?>(null)
    val generalForecast = _generalForecast.asStateFlow()

    private val _hourlyForecast = MutableStateFlow<WeatherProperties?>(null)
    val hourlyForecast = _hourlyForecast.asStateFlow()

    private val _weatherPeriodDisplayBlocks = MutableStateFlow<List<WeatherPeriodDisplayBlock>?>(null)
    val weatherPeriodDisplayBlocks = _weatherPeriodDisplayBlocks.asStateFlow()

//    init {
//        viewModelScope.launch {
//            locationHelper.retrieveCurrentLocation({ location ->
//                updateWeatherInfo(location.latitude, location.longitude)
//            }) {
//                // Reload activity. This will kick off the prompting for access.
//                (context as Activity).recreate()
//            }
//        }
//    }

    fun updateWeatherInfoUsingCurrentLocation() {
        viewModelScope.launch {
            locationHelper.retrieveCurrentLocation({ location ->
                updateWeatherInfo(location.latitude, location.longitude)
            }) {
                // Reload activity. This will kick off the prompting for access.
                (context as Activity).recreate()
            }
        }
    }

    fun updateWeatherInfo(latitude: Double, longitude: Double): Job {
        return viewModelScope.launch(Dispatchers.Default) {
            // Retrieve the oldGridId if it exists to compare against
            // the new one since if they are different it is known that
            // the forecast will need to be updated since a new location
            // is being searched.
            val oldGridId = weatherGridPoints.value?.properties?.gridId
            updateGridPoints(latitude, longitude)
            updateWeatherForecasts(oldGridId)
        }
    }

    private suspend fun updateGridPoints(latitude: Double, longitude: Double) {
        _weatherGridPoints.value =
                weatherRepository.retrieveGridPoints(latitude, longitude)
    }

    private suspend fun updateWeatherForecasts(oldGridId: String?) {
        var updatedGeneralForecast: WeatherProperties? = null
        var updatedHourlyForecast: WeatherProperties? = null

        coroutineScope {
            launch {
                updatedGeneralForecast = withTimeoutOrNull(Duration.ofSeconds(10)) {
                    updateForecastIfNeeded(true, oldGridId)
                }
            }

            launch {
                updatedHourlyForecast = withTimeoutOrNull(Duration.ofSeconds(10)) {
                    updateForecastIfNeeded(false, oldGridId)
                }

                // If hourly periods were found then set it and get the current
                // weather period from it.
                if(updatedHourlyForecast != null) {
                    _currentTimeWeatherPeriod.value = updateCurrentTimeWeatherPeriod(updatedHourlyForecast!!)
                }
            }
        }

        if(updatedGeneralForecast != null) {
            _generalForecast.value = updatedGeneralForecast

            if(updatedHourlyForecast != null) {
                _hourlyForecast.value = updatedHourlyForecast
                _weatherPeriodDisplayBlocks.value =
                    groupWeatherPeriodsInBlocks(
                        updatedGeneralForecast!!.periods,
                        updatedHourlyForecast!!.periods
                    )
            }
        }
    }

    private fun updateCurrentTimeWeatherPeriod(hourlyForecast: WeatherProperties): WeatherPeriod? {
        val currentTime = OffsetDateTime.now()
        for(period in hourlyForecast.periods) {
            if(currentTime >= period.startTime && currentTime <= period.endTime) {
                return period
            }
        }

        return currentTimeWeatherPeriod.value
    }

    private fun groupWeatherPeriodsInBlocks(generalPeriods: List<WeatherPeriod>, hourlyPeriods: List<WeatherPeriod>): List<WeatherPeriodDisplayBlock> {
        val blocks = mutableListOf<WeatherPeriodDisplayBlock>()
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
                        blocks.add(WeatherPeriodDisplayBlock(generalPeriod, blockHourlyPeriods))
                        currentHourlyPeriodIndex = j
                        break
                    }
                }
            }
        }

        return blocks
    }

    private suspend fun updateForecastIfNeeded(isGeneralForecast: Boolean, oldGridId: String?): WeatherProperties? {
        val oldWeatherProperties = if(isGeneralForecast) generalForecast.value else hourlyForecast.value

        if(weatherGridPoints.value != null) {
            val url = if(isGeneralForecast) weatherGridPoints.value!!.properties.forecastUrl else
                weatherGridPoints.value!!.properties.forecastHourlyUrl


            val updatedWeatherProperties = weatherRepository.retrieveWeatherProperties(url)

            if (oldWeatherProperties == null || (oldGridId != weatherGridPoints.value?.properties?.gridId) ||
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

    fun retrieveLastGeneralWeatherPeriodEndDate(): OffsetDateTime? {
        return _generalForecast.value?.periods?.last()?.endTime
    }
}