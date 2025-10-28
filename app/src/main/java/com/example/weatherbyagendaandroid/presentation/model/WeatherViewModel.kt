package com.example.weatherbyagendaandroid.presentation.model

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbyagendaandroid.dao.repository.SelectedMenuOptionsRepository
import com.example.weatherbyagendaandroid.domain.weather.WeatherInfo
import com.example.weatherbyagendaandroid.helpers.LocationHelper
import com.example.weatherbyagendaandroid.service.WeatherService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime
import javax.inject.Inject

enum class WeatherDataStateEnum {
    LOADING,
    REQUEST_FILTERING,
    FILTERING_IN_PROGRESS,
    DONE
}

@HiltViewModel
class WeatherViewModel @Inject constructor(@ApplicationContext private val context: Context,
                                           private val locationHelper: LocationHelper,
                                           private val weatherService: WeatherService,
                                           private val selectedMenuOptionsRepository: SelectedMenuOptionsRepository): ViewModel() {

   companion object {
       val LOG_TAG = "WeatherViewModel"
   }

    private val _weatherLoadingState = MutableStateFlow(WeatherDataStateEnum.LOADING)
    val weatherLoadingState = _weatherLoadingState.asStateFlow()

    var weatherInfo: WeatherInfo? = null

    private var weatherUpdateJob: Job? = null
    private var weatherFilterJob: Job? = null

    init{
        viewModelScope.launch {
            selectedMenuOptionsRepository.selectedLocationLatLon.collect { selectedLocationLatLon ->
                when (selectedLocationLatLon) {
                    SelectedMenuOptionsRepository.LocationLatLon.GpsLatLon -> updateWeatherInfoUsingCurrentLocation()
                    is SelectedMenuOptionsRepository.LocationLatLon.SavedLocationLatLon -> {
                        updateWeatherInfo(
                            selectedLocationLatLon.latitude,
                            selectedLocationLatLon.longitude
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            merge(
                selectedMenuOptionsRepository.adhocFilterGroup,
                selectedMenuOptionsRepository.currentWeatherFilterGroup
            ).collect { filterGroup ->
                // Cancel any filtering jobs that may be already going and
                // set status to request filtering to kick off the filtering again.
                if (_weatherLoadingState.value != WeatherDataStateEnum.LOADING) {
                    _weatherLoadingState.value = WeatherDataStateEnum.REQUEST_FILTERING
                }
            }
        }
    }

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
        _weatherLoadingState.value = WeatherDataStateEnum.LOADING
        weatherUpdateJob = viewModelScope.launch(Dispatchers.Default) {
            weatherInfo = weatherService.updateWeatherInfo(latitude, longitude,weatherInfo)
            _weatherLoadingState.value = WeatherDataStateEnum.REQUEST_FILTERING
        }

        return weatherUpdateJob!!
    }

    fun retrieveLastGeneralWeatherPeriodEndDate(): OffsetDateTime? {
        return weatherInfo?.generalForecast?.periods?.last()?.endTime
    }

    suspend fun runWeatherDisplayBlockThroughFilters() {
        val weatherFilterGroup =
            if (selectedMenuOptionsRepository.adhocFilterGroup.value.hasFilters())
                selectedMenuOptionsRepository.adhocFilterGroup.value
            else
                selectedMenuOptionsRepository.currentWeatherFilterGroup.value

        weatherFilterJob?.cancel()

        _weatherLoadingState.value = WeatherDataStateEnum.FILTERING_IN_PROGRESS

        weatherFilterJob = viewModelScope.launch(Dispatchers.Default) {
            // Add scope here to make sure all the child coroutines finish before we continue.
            coroutineScope {
                for (weatherPeriodDisplayBlock in weatherInfo!!.weatherPeriodDisplayBlocks) {
                    launch {
                        weatherFilterGroup.runWeatherDisplayBlockThroughFilters(
                            weatherPeriodDisplayBlock
                        )
                    }
                }
            }

            _weatherLoadingState.value = WeatherDataStateEnum.DONE
        }
    }
}