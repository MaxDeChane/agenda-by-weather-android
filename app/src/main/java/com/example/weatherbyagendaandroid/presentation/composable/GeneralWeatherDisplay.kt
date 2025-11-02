package com.example.weatherbyagendaandroid.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.presentation.model.WeatherViewModel

@Composable
fun GeneralWeatherDisplay(innerPadding: PaddingValues,
                          weatherViewModel: WeatherViewModel = viewModel()) {

    val weatherLoadingStatus by weatherViewModel.weatherLoadingState.collectAsStateWithLifecycle()

    if(weatherLoadingStatus == WeatherViewModel.WeatherDataState.Loading) {
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
        if (weatherLoadingStatus == WeatherViewModel.WeatherDataState.FilteringInProgress) {
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
                weatherLoadingStatus)
        }
    }
}