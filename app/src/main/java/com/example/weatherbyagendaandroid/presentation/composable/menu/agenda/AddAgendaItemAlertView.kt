package com.example.weatherbyagendaandroid.presentation.composable.menu.agenda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.domain.agenda.AgendaItem
import com.example.weatherbyagendaandroid.presentation.composable.ExpandableView
import com.example.weatherbyagendaandroid.presentation.composable.menu.DateTimeInput
import com.example.weatherbyagendaandroid.presentation.composable.menu.SavedLocationSelectionView
import com.example.weatherbyagendaandroid.presentation.composable.menu.WeatherFilterGroupsSelectionView
import com.example.weatherbyagendaandroid.presentation.model.LocationViewModel
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Composable
fun AddAgendaItemAlertView(dismiss: () -> Unit, locationViewModel: LocationViewModel = viewModel()) {
    var agendaItemName by remember { mutableStateOf("") }
    var startDateTime by remember { mutableStateOf(LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0)) }
    var endDateTime by remember { mutableStateOf(startDateTime.plusHours(1)) }
    var locationExpanded by remember { mutableStateOf(false) }
    var selectedLocationId by remember { mutableStateOf(-1) }
    var weatherFilterGroupsExpanded by remember { mutableStateOf(false) }
    var selectedFilterGroupName by remember { mutableStateOf("") }

    val gpsLocation by locationViewModel.gpsLocation.collectAsStateWithLifecycle()
    val savedLocations by locationViewModel.savedLocations.collectAsStateWithLifecycle()

    AlertDialog(title = {
        Text(text = "Add Agenda Item")
    },
        text = {
            Column {
                OutlinedTextField(
                    value = agendaItemName,
                    onValueChange = {
                        agendaItemName = it
                    },
                    label = {
                        Text(
                            "Agenda Item Name",
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DateTimeInput(startDateTime, true) { startDateTime = it }
                    DateTimeInput(endDateTime, false) { endDateTime = it }
                }

                ExpandableView("Location", locationExpanded) { locationExpanded = !locationExpanded }

                if(locationExpanded) {
                    SavedLocationSelectionView(selectedLocationId, savedLocations,false, { selectedLocationId = it })
                }

                ExpandableView("Weather Filter Groups", weatherFilterGroupsExpanded) { weatherFilterGroupsExpanded = !weatherFilterGroupsExpanded }

                if(weatherFilterGroupsExpanded) {
                    WeatherFilterGroupsSelectionView(false)
                }
            }
        },
        onDismissRequest = {
            dismiss()
        },
        confirmButton = {
            TextButton(
                enabled = agendaItemName.isNotBlank(),
                onClick = {
//                    val agendaItem = AgendaItem()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    dismiss()
                }
            ) {
                Text("Cancel")
            }
        })
}