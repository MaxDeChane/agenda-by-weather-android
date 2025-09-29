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

@Composable
fun LocationView(locationViewModel: LocationViewModel = viewModel()
) {
    var showAddLocationAlertView by remember { mutableStateOf(false) }

    val currentSavedLocation by locationViewModel.selectedSavedLocation.collectAsStateWithLifecycle()
    val savedLocations by locationViewModel.savedLocations.collectAsStateWithLifecycle()

    val currentSavedLocationId = if(currentSavedLocation != null) currentSavedLocation!!.id else -1
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        SavedLocationSelectionView(currentSavedLocationId, savedLocations, true, { locationViewModel.selectLocation(it) })
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