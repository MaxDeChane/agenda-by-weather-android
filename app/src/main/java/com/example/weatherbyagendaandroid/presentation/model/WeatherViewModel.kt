package com.example.weatherbyagendaandroid.presentation.model

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbyagendaandroid.dao.repository.SelectedMenuOptionsRepository
import com.example.weatherbyagendaandroid.domain.weather.WeatherInfo
import com.example.weatherbyagendaandroid.enums.LoadingStatusEnum
import com.example.weatherbyagendaandroid.helpers.LocationHelper
import com.example.weatherbyagendaandroid.service.WeatherService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(@ApplicationContext private val context: Context,
                                           private val locationHelper: LocationHelper,
                                           private val weatherService: WeatherService,
                                           selectedMenuOptionsRepository: SelectedMenuOptionsRepository): ViewModel() {

   companion object {
       val LOG_TAG = "WeatherViewModel"
   }

    private val _weatherLoadingStatus = MutableStateFlow(LoadingStatusEnum.LOADING)
    val weatherLoadingStatus = _weatherLoadingStatus.asStateFlow()

    val selectedLocationLatLon = selectedMenuOptionsRepository.selectedLocationLatLon

    var weatherInfo: WeatherInfo? = null

    private var weatherUpdateJob: Job? = null

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
        weatherUpdateJob?.cancel()

        // Set the loading status to loading. This is done after the cancel job to make sure
        // there isn't any race conditions.
        _weatherLoadingStatus.value = LoadingStatusEnum.LOADING
        weatherUpdateJob = viewModelScope.launch(Dispatchers.Default) {
            weatherInfo = weatherService.updateWeatherInfo(latitude, longitude,weatherInfo)
            _weatherLoadingStatus.value = LoadingStatusEnum.DONE
        }

        return weatherUpdateJob!!
    }

    fun retrieveLastGeneralWeatherPeriodEndDate(): OffsetDateTime? {
        return weatherInfo?.generalForecast?.periods?.last()?.endTime
    }
}