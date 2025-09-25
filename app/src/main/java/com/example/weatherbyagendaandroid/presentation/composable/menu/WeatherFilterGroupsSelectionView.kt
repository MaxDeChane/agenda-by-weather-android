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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.presentation.model.WeatherViewModel

@Composable
fun WeatherFilterGroupsSelectionView(isEditable: Boolean = true, weatherViewModel: WeatherViewModel = viewModel()
) {
    var isEditing by remember { mutableStateOf(false) }

    val weatherFilterGroups by weatherViewModel.weatherFilterGroups.collectAsStateWithLifecycle()
    val selectedWeatherFilterGroups by weatherViewModel.selectedWeatherFilterGroups.collectAsStateWithLifecycle()
    val inEditWeatherFilterGroups by weatherViewModel.inEditFilterGroupHolders.collectAsStateWithLifecycle()

    if(weatherFilterGroups.filterGroups.isNotEmpty()) {
        Column {
            weatherFilterGroups.filterGroups.keys.forEach { option ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { weatherViewModel.addRemoveSelectedWeatherFilterGroup(option) }
                    ) {
                        RadioButton(
                            selected = selectedWeatherFilterGroups.contains(option),
                            onClick = { weatherViewModel.addRemoveSelectedWeatherFilterGroup(option) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    if(isEditable) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.clickable {
                                    weatherViewModel.setupWeatherFilterGroupForEditing(option)
                                    isEditing = !isEditing
                                }
                            )
                            Text(
                                text = "Delete",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(horizontal = 8.dp).clickable {
                                    weatherViewModel.deleteWeatherFilterGroup(option)
                                }
                            )
                        }

                        if (isEditing && inEditWeatherFilterGroups.contains(option)) {
                            EditWeatherFilterGroupView(option, { isEditing = false })
                        }
                    }
                }

                HorizontalDivider(thickness = 2.dp)
            }
        }
    }  else {
        Box(Modifier.background(MaterialTheme.colorScheme.secondary)) {
            Text("No weather filter groups available. Please create one using the Tab below.",
                Modifier.padding(8.dp))
        }
    }
}