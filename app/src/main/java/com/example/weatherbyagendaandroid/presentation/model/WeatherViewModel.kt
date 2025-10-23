package com.example.weatherbyagendaandroid.presentation.model

import android.app.Activity
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbyagendaandroid.dao.domain.GridPointsResponse
import com.example.weatherbyagendaandroid.dao.domain.WeatherPeriod
import com.example.weatherbyagendaandroid.dao.domain.WeatherProperties
import com.example.weatherbyagendaandroid.domain.weather.WeatherInfo
import com.example.weatherbyagendaandroid.enums.LoadingStatusEnum
import com.example.weatherbyagendaandroid.helpers.LocationHelper
import com.example.weatherbyagendaandroid.presentation.domain.WeatherPeriodDisplayBlock
import com.example.weatherbyagendaandroid.service.WeatherService
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
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(@ApplicationContext private val context: Context,
                                           private val locationHelper: LocationHelper,
                                           private val weatherService: WeatherService): ViewModel() {

   companion object {
       val LOG_TAG = "WeatherViewModel"
   }

    private val _weatherLoadingStatus = MutableStateFlow(LoadingStatusEnum.LOADING)
    val weatherLoadingStatus = _weatherLoadingStatus.asStateFlow()

    var weatherInfo: WeatherInfo? = null

    private var weatherJob: Job? = null

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
        // Cancel any existing Jobs here since we don't want it to say it is done when it is
        // actually reloading.
        weatherJob?.cancel()

        // Set the loading status to loading. This is done after the cancel job to make sure
        // there isn't any race conditions.
        _weatherLoadingStatus.value = LoadingStatusEnum.LOADING
        weatherJob = viewModelScope.launch(Dispatchers.Default) {
            weatherInfo = weatherService.updateWeatherInfo(latitude, longitude,weatherInfo)
            _weatherLoadingStatus.value = LoadingStatusEnum.DONE
        }

        return weatherJob!!
    }

    fun retrieveLastGeneralWeatherPeriodEndDate(): OffsetDateTime? {
        return weatherInfo?.generalForecast?.periods?.last()?.endTime
    }
}