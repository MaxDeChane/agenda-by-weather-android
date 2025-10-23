package com.example.weatherbyagendaandroid.presentation.composable

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.enums.LoadingStatusEnum
import com.example.weatherbyagendaandroid.presentation.model.AgendaViewModel
import com.example.weatherbyagendaandroid.presentation.model.LocationViewModel
import com.example.weatherbyagendaandroid.presentation.model.WeatherFilterViewModel
import com.example.weatherbyagendaandroid.presentation.model.WeatherViewModel

@Composable
fun AgendaItemWeatherPeriodsView(innerPadding: PaddingValues, agendaItemId: Int,
                                              agendaViewModel: AgendaViewModel = viewModel(),
                                              locationViewModel: LocationViewModel = viewModel(),
                                              weatherFilterViewModel: WeatherFilterViewModel = viewModel(),
                                              weatherViewModel: WeatherViewModel = viewModel()) {
    var loadingStatus by remember { mutableStateOf(LoadingStatusEnum.LOADING) }

    val agendaItems by agendaViewModel.agendaItems.collectAsStateWithLifecycle()
    val agendaItemsLoadingStatus by agendaViewModel.loadingStatus.collectAsStateWithLifecycle()
    val savedLocations by locationViewModel.savedLocations.collectAsStateWithLifecycle()
    val savedLocationsLoadingStatus by locationViewModel.loadingStatus.collectAsStateWithLifecycle()
    val weatherFilterGroupsLoadingStatus by weatherFilterViewModel.loadingStatus.collectAsStateWithLifecycle()

    LaunchedEffect(agendaItemsLoadingStatus.name + savedLocationsLoadingStatus.name + agendaItemId) {
        if(agendaItemId != -1 && agendaItemsLoadingStatus == LoadingStatusEnum.DONE &&
            savedLocationsLoadingStatus == LoadingStatusEnum.DONE && weatherFilterGroupsLoadingStatus == LoadingStatusEnum.DONE) {

            val agendaItem = agendaItems.items[agendaItemId]
            if(agendaItem == null) {
                Log.e("SetupViewModelsFromIntent", "Unable to retrieve agenda item with id $agendaItemId." +
                        " This should not happen and needs to be investigated.")

                return@LaunchedEffect
            }

            if(agendaItem.locationId != -1) {
                val savedLocation = savedLocations.locations[agendaItem.locationId]

                if(savedLocation == null) {

                } else {
                    // Set the selected location so it shows up if menu is opened and
                    // update the weather view model.
                    locationViewModel.selectLocation(agendaItem.locationId)
                    weatherViewModel.updateWeatherInfo(
                        savedLocation.latitude,
                        savedLocation.longitude
                    ).invokeOnCompletion {
                        weatherFilterViewModel.selectWeatherFilterGroup(agendaItem.weatherFilterGroupId)
                        loadingStatus = LoadingStatusEnum.DONE
                    }
                }
            }
        }
    }

    if(loadingStatus == LoadingStatusEnum.LOADING) {
        Box(
            modifier = Modifier
                .fillMaxSize() // take up the entire available space
                .background(MaterialTheme.colorScheme.primary) // your background color
        ) {
            Text("Loading Weather Data...", Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.displayLarge)
        }
    } else if(loadingStatus == LoadingStatusEnum.DONE) {
        if(weatherViewModel.weatherInfo?.weatherPeriodDisplayBlocks != null) {
            WeatherPeriodsView(innerPadding, weatherViewModel.weatherInfo!!.weatherPeriodDisplayBlocks,
                false)
        }
    }
}