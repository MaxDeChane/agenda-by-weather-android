package com.example.weatherbyagendaandroid.presentation.composable.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.presentation.composable.dialog.UpdateLocationAlertView
import com.example.weatherbyagendaandroid.presentation.model.LocationViewModel
import com.example.weatherbyagendaandroid.presentation.model.WeatherViewModel

@Composable
fun LocationView(locationViewModel: LocationViewModel = viewModel(),
                 weatherViewModel: WeatherViewModel = viewModel()
) {
    var showAddLocationAlertView by remember { mutableStateOf(false) }
    val isFirstTimeThrough = remember { mutableListOf(true) }

    val gpsLocation by locationViewModel.gpsLocation.collectAsStateWithLifecycle()
    val currentSelectedLocation by locationViewModel.selectedSavedLocation.collectAsStateWithLifecycle()

    LaunchedEffect(currentSelectedLocation) {
        // Don't reload the weather if it is the first time this is loaded since
        // it couldn't have changed if the options haven't been displayed yet.
        if(!isFirstTimeThrough[0]) {
            if (currentSelectedLocation != null) {
                weatherViewModel.updateWeatherInfo(
                    currentSelectedLocation!!.latitude,
                    currentSelectedLocation!!.longitude
                )
            } else if (gpsLocation != null) {
                weatherViewModel.updateWeatherInfo(gpsLocation!!.latitude, gpsLocation!!.longitude)
            }
        }
        isFirstTimeThrough[0] = false
    }

    val currentSavedLocationId = if(currentSelectedLocation != null) currentSelectedLocation!!.id else -1
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        SavedLocationSelectionView(currentSavedLocationId, true, { locationViewModel.selectLocation(it) })
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { showAddLocationAlertView = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSecondary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Search For Location")
            }
        }
    }

    if(showAddLocationAlertView) {
        UpdateLocationAlertView({ showAddLocationAlertView = false })
    }
}