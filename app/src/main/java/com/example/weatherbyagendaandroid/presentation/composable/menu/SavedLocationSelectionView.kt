package com.example.weatherbyagendaandroid.presentation.composable.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.presentation.domain.SavedLocation
import com.example.weatherbyagendaandroid.presentation.domain.SavedLocations
import com.example.weatherbyagendaandroid.presentation.model.LocationViewModel

@Composable
fun SavedLocationSelectionView(currentSavedLocationName: String, savedLocations: SavedLocations,
                               isEditable: Boolean = true, selectLocation: (locationName: String) -> Unit,
                               locationViewModel: LocationViewModel = viewModel()) {

    var newLocationName by remember { mutableStateOf("") }
    var locationUnderEditName: String? by remember { mutableStateOf(null) }

    if(savedLocations.hasSaveLocations()) {
        Column{
            savedLocations.locationsByName.keys.forEach { locationName ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectLocation(locationName) }
                    ) {
                        RadioButton(
                            selected = currentSavedLocationName == locationName,
                            onClick = { selectLocation(locationName) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = locationName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    if(isEditable) {
                        if(locationName == locationUnderEditName) {
                            Column {
                                OutlinedTextField(
                                    value = newLocationName,
                                    onValueChange = {
                                        newLocationName = it
                                    },
                                    label = {
                                        Text(
                                            "Agenda Item Name",
                                            color = MaterialTheme.colorScheme.onSecondary
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Cancel",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.clickable {
                                            newLocationName = ""
                                            locationUnderEditName = null
                                        }
                                    )
                                    Text(
                                        text = "Save",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(horizontal = 8.dp).clickable {
                                            locationViewModel.updateLocationName(locationName, newLocationName)
                                        }
                                    )
                                }
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = "Rename",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.clickable {
                                        newLocationName = ""
                                        locationUnderEditName = locationName
                                    }
                                )
                                Text(
                                    text = "Delete",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(horizontal = 8.dp).clickable {
                                        locationViewModel.deleteSavedLocation(locationName)
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(thickness = 2.dp)
            }
        }
    } else {
        Box(Modifier.background(MaterialTheme.colorScheme.secondary)) {
            Text(
                "No weather filter groups available. Please create one using the Tab below.",
                Modifier.padding(8.dp)
            )
        }
    }
}