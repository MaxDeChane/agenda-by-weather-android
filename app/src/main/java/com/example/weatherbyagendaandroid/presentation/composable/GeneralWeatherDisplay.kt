package com.example.weatherbyagendaandroid.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.example.weatherbyagendaandroid.dao.repository.SelectedMenuOptionsRepository
import com.example.weatherbyagendaandroid.enums.LoadingStatusEnum
import com.example.weatherbyagendaandroid.presentation.model.FilterStatus
import com.example.weatherbyagendaandroid.presentation.model.WeatherFilterViewModel
import com.example.weatherbyagendaandroid.presentation.model.WeatherViewModel

@Composable
fun GeneralWeatherDisplay(innerPadding: PaddingValues,
                          weatherFilterViewModel: WeatherFilterViewModel = viewModel(),
                          weatherViewModel: WeatherViewModel = viewModel()) {

    val weatherLoadingStatus by weatherViewModel.weatherLoadingStatus.collectAsStateWithLifecycle()
    val filterStatus by weatherFilterViewModel.filterStatus.collectAsStateWithLifecycle()
    var refreshKey by remember { mutableStateOf(false) }

    // Run weatherPeriods through the selected filters whenever the filter status is changed
    // and in progress.
    LaunchedEffect(filterStatus.name) {
        if (filterStatus == FilterStatus.IN_PROGRESS && weatherLoadingStatus == LoadingStatusEnum.DONE &&
            weatherViewModel.weatherInfo?.weatherPeriodDisplayBlocks != null) {
            weatherFilterViewModel.runWeatherDisplayBlockThroughFilters(
                weatherViewModel.weatherInfo!!.weatherPeriodDisplayBlocks
            )
            refreshKey = !refreshKey
        }
    }

    if(weatherLoadingStatus == LoadingStatusEnum.LOADING) {
        Box(
            modifier = Modifier
                .fillMaxSize() // take up the entire available space
                .background(MaterialTheme.colorScheme.primary) // your background color
        ) {
            Text("Loading Weather Data...", Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.displayLarge)
        }
    } else {
        if (filterStatus == FilterStatus.IN_PROGRESS) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize() // take up the entire available space
                    .background(MaterialTheme.colorScheme.primary) // your background color
            ) {
                Text("Filtering...", Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.displayLarge)
            }
        } else if(weatherViewModel.weatherInfo?.weatherPeriodDisplayBlocks != null) {
            WeatherPeriodsView(innerPadding, weatherViewModel.weatherInfo!!.weatherPeriodDisplayBlocks,
                refreshKey)
        }
    }
}