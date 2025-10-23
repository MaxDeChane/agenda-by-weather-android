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
import com.example.weatherbyagendaandroid.presentation.model.WeatherFilterViewModel

@Composable
fun WeatherFilterGroupsSelectionView(currentFilterGroupId: Int, isEditable: Boolean = true,
                                     selectLocation: (locationId: Int) -> Unit,
                                     weatherFilterViewModel: WeatherFilterViewModel = viewModel()
) {
    var isEditing by remember { mutableStateOf(false) }

    val weatherFilterGroups by weatherFilterViewModel.weatherFilterGroups.collectAsStateWithLifecycle()
    val inEditWeatherFilterGroups by weatherFilterViewModel.inEditFilterGroupHolders.collectAsStateWithLifecycle()

    if(weatherFilterGroups.filterGroups.isNotEmpty()) {
        Column {
            weatherFilterGroups.filterGroups.forEach { filterGroupEntry ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectLocation(filterGroupEntry.key) }
                    ) {
                        RadioButton(
                            selected = currentFilterGroupId == filterGroupEntry.key,
                            onClick = { selectLocation(filterGroupEntry.key) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = filterGroupEntry.value.name,
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
                                    weatherFilterViewModel.setupWeatherFilterGroupForEditing(filterGroupEntry.key)
                                    isEditing = !isEditing
                                }
                            )
                            Text(
                                text = "Delete",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(horizontal = 8.dp).clickable {
                                    weatherFilterViewModel.deleteWeatherFilterGroup(filterGroupEntry.key)
                                }
                            )
                        }

                        if (isEditing && inEditWeatherFilterGroups.containsKey(filterGroupEntry.key)) {
                            EditWeatherFilterGroupView(filterGroupEntry.key,
                                inEditWeatherFilterGroups[filterGroupEntry.key]!!.weatherFilterGroupToEdit,
                                { isEditing = false })
                        }
                    }
                }

                HorizontalDivider(thickness = 2.dp)
            }
        }
    }  else {
        Box(Modifier.background(MaterialTheme.colorScheme.secondary)) {
            Text("No weather filter groups available. Please create one using the Weather Filters tab.",
                Modifier.padding(8.dp))
        }
    }
}