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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(@ApplicationContext private val context: Context,
                                           private val locationHelper: LocationHelper,
                                           private val weatherService: WeatherService,
                                           private val selectedMenuOptionsRepository: SelectedMenuOptionsRepository): ViewModel() {

   companion object {
       val LOG_TAG = "WeatherViewModel"
   }

    // Make this a sealed class so a new instance of Done can be created
    // to make sure it is a new instance and causes refreshes in compose.
    sealed class WeatherDataState {
        object Loading: WeatherDataState()
        object FilteringInProgress: WeatherDataState()
        class Done: WeatherDataState()
    }

    private val _weatherLoadingState = MutableStateFlow<WeatherDataState>(WeatherDataState.Loading)
    val weatherLoadingState = _weatherLoadingState.asStateFlow()

    var weatherInfo: WeatherInfo? = null

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
                if (weatherInfo != null) {
                    runWeatherDisplayBlockThroughFilters()
                }
            }
        }
    }

    fun updateWeatherInfoUsingCurrentLocation() {
        locationHelper.retrieveCurrentLocation({ location ->
            viewModelScope.launch {
                updateWeatherInfo(location.latitude, location.longitude)
            }
        }) {
            // Reload activity. This will kick off the prompting for access.
            (context as Activity).recreate()
        }
    }

    suspend fun updateWeatherInfo(latitude: Double, longitude: Double) {
        // Set the loading status to loading. This is done after the cancel job to make sure
        // there isn't any race conditions.
        _weatherLoadingState.value = WeatherDataState.Loading
        withContext(Dispatchers.Default) {
            weatherInfo = weatherService.updateWeatherInfo(latitude, longitude,weatherInfo)
        }
        runWeatherDisplayBlockThroughFilters()
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

        // Switch to main to update the state value
        withContext(Dispatchers.Main) {
            _weatherLoadingState.value = WeatherDataState.FilteringInProgress
        }

        withContext(Dispatchers.Default) {
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
        }

        // Switch to main to update the state value
        withContext(Dispatchers.Main) {
            _weatherLoadingState.value = WeatherDataState.Done()
        }
    }
}