package com.example.weatherbyagendaandroid.presentation.composable.menu

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.presentation.composable.ExpandableView
import com.example.weatherbyagendaandroid.presentation.composable.SecondaryThemedButtons
import com.example.weatherbyagendaandroid.presentation.domain.TemperatureFilter
import com.example.weatherbyagendaandroid.presentation.domain.WeatherFilterGroup
import com.example.weatherbyagendaandroid.presentation.domain.WeatherKeywordFilter
import com.example.weatherbyagendaandroid.presentation.domain.WindFilter
import com.example.weatherbyagendaandroid.presentation.model.WeatherViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WeatherFilterGroupInputView(currentWeatherFilterGroup: WeatherFilterGroup,
                                editingFilterGroup: Boolean,
                                cancelEditClicked: () -> Unit,
                                saveClicked: (String) -> Unit,
                                weatherViewModel: WeatherViewModel = viewModel()
) {
    val LOG_TAG = "CreateEditWeatherFilterGroup"

    var currentFilterGroupName by remember { mutableStateOf(currentWeatherFilterGroup.name) }
    var weatherKeywordFilterExpanded by remember { mutableStateOf(false) }
    var clearFilters by remember { mutableStateOf(false) }

    val temperatureRangeFilter = currentWeatherFilterGroup.retrieveWeatherFilter(TemperatureFilter::class.simpleName!!) as TemperatureFilter
    val windRangeFilter = currentWeatherFilterGroup.retrieveWeatherFilter(WindFilter::class.simpleName!!) as WindFilter
    val weatherKeywordFilter = currentWeatherFilterGroup.retrieveWeatherFilter(WeatherKeywordFilter::class.simpleName!!) as WeatherKeywordFilter

    // Used for filters that require typing to try and mitigate
    var debounceJob: Job? = null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        OutlinedTextField(
            value = currentFilterGroupName,
            onValueChange = { currentFilterGroupName = it },
            label = {
                Row {
                    Text("Filter Group Name", color = MaterialTheme.colorScheme.onSecondary)
                    Text("*Required", color = Color.Red)
                }
                    },
            modifier = Modifier.fillMaxWidth()
        )

        TemperatureRangeInput(
            temperatureRangeFilter,
            { updatedLowerTemp, updatedHigherTemp ->
                debounceJob?.cancel()
                debounceJob = CoroutineScope(Dispatchers.Main).launch {
                    // Wait to make sure user is done inputting to help avoid
                    // unnecessary renders of the screen.
                    delay(1000L)
                    // Since the default values are used for both, this means that the filter
                    // can be removed since it won't actual filter anything
                    if(updatedLowerTemp == Int.MIN_VALUE && updatedHigherTemp == Int.MAX_VALUE) {
                        weatherViewModel.removeWeatherFilter(TemperatureFilter::class.simpleName!!, currentWeatherFilterGroup.id)

                        Log.i(LOG_TAG, "Removed temperature range filter")
                    } else {
                        weatherViewModel.setWeatherFilter(
                            TemperatureFilter::class.simpleName!!,
                            TemperatureFilter(updatedLowerTemp, updatedHigherTemp),
                            currentWeatherFilterGroup.id
                        )
                        Log.i(LOG_TAG, "Updated temperature range filter")
                    }
                }
            },
            clearFilters
        )
        WindRangeInput(
            windRangeFilter,
            { updatedLowerWindSpeed, updatedHigherWindSpeed ->
                debounceJob?.cancel()
                debounceJob = CoroutineScope(Dispatchers.Main).launch {
                    // Wait to make sure user is done inputting to help avoid
                    // unnecessary renders of the screen.
                    delay(1000L)
                    weatherViewModel.setWeatherFilter(WindFilter::class.simpleName!!,
                        WindFilter(updatedLowerWindSpeed, updatedHigherWindSpeed),
                        currentWeatherFilterGroup.id)
                    Log.i(LOG_TAG, "Updated wind range filter")
                }
            },
            clearFilters
        )
        ExpandableView("Select Weather Keywords", weatherKeywordFilterExpanded) { weatherKeywordFilterExpanded = !weatherKeywordFilterExpanded}
        if(weatherKeywordFilterExpanded) {
            WeatherKeywordInput(
                weatherKeywordFilter,
                { updatedDefaultSelectedKeywords ->
                    // No delay needed here since user action should reflect immediately
                    if(updatedDefaultSelectedKeywords.isEmpty() && weatherKeywordFilter.customSelectedKeywords.isEmpty()) {
                        weatherViewModel.removeWeatherFilter(WeatherKeywordFilter::class.simpleName!!, currentWeatherFilterGroup.id)
                    } else {
                        weatherViewModel.setWeatherFilter(
                            WeatherKeywordFilter::class.simpleName!!,
                            WeatherKeywordFilter(
                                updatedDefaultSelectedKeywords,
                                weatherKeywordFilter.customSelectedKeywords
                            ),
                            currentWeatherFilterGroup.id
                        )
                    }
                    Log.i(LOG_TAG, "Updated weather selectable keywords for filter")
                },
                { updatedCustomSelectedKeywords ->
                    // No delay needed here since user action should reflect immediately
                    if(updatedCustomSelectedKeywords.isEmpty() && weatherKeywordFilter.defaultSelectedKeywords.isEmpty()) {
                        weatherViewModel.removeWeatherFilter(WeatherKeywordFilter::class.simpleName!!, currentWeatherFilterGroup.id)
                    } else {
                        weatherViewModel.setWeatherFilter(
                            WeatherKeywordFilter::class.simpleName!!,
                            WeatherKeywordFilter(
                                weatherKeywordFilter.defaultSelectedKeywords,
                                updatedCustomSelectedKeywords
                            ),
                            currentWeatherFilterGroup.id
                        )
                    }
                    Log.i(LOG_TAG, "Updated weather selectable keywords for filter")
                },
                clearFilters
            )
        }

        SecondaryThemedButtons(if(editingFilterGroup) "Update" else "Save",
            if(editingFilterGroup) "Cancel" else "Clear",
            currentFilterGroupName.isNotBlank(),
            {
                weatherViewModel.clearInCreationOrEditWeatherFilterGroup(currentWeatherFilterGroup.id)
                clearFilters = true
                // Collapse the editing screen if editing
                if(editingFilterGroup) {
                    cancelEditClicked()
                }
            })
        {
            saveClicked(currentFilterGroupName)
        }
    }

    // Set this to false here since if it is true it will run through and clear all the
    // composables but don't want that on any other refreshes.
    clearFilters = false
}