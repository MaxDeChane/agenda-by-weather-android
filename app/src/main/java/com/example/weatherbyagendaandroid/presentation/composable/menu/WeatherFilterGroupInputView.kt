package com.example.weatherbyagendaandroid.presentation.composable.menu

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.presentation.composable.ExpandableView
import com.example.weatherbyagendaandroid.presentation.composable.SecondaryThemedButtons
import com.example.weatherbyagendaandroid.presentation.domain.DateTimeFilter
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
import java.time.LocalDateTime

@Composable
fun WeatherFilterGroupInputView(currentWeatherFilterGroup: WeatherFilterGroup,
                                editingFilterGroup: Boolean,
                                cancelEditClicked: () -> Unit,
                                saveClicked: () -> Unit,
                                weatherViewModel: WeatherViewModel = viewModel()
) {
    val LOG_TAG = "CreateEditWeatherFilterGroup"

    var weatherKeywordFilterExpanded by remember { mutableStateOf(false) }
    var clearFilters by remember { mutableStateOf(false) }

    val dateTimeFilter = currentWeatherFilterGroup.retrieveWeatherFilter(DateTimeFilter::class.simpleName!!) as DateTimeFilter
    val temperatureRangeFilter = currentWeatherFilterGroup.retrieveWeatherFilter(TemperatureFilter::class.simpleName!!) as TemperatureFilter
    val windRangeFilter = currentWeatherFilterGroup.retrieveWeatherFilter(WindFilter::class.simpleName!!) as WindFilter
    val weatherKeywordFilter = currentWeatherFilterGroup.retrieveWeatherFilter(WeatherKeywordFilter::class.simpleName!!) as WeatherKeywordFilter

    val defaultStartTime: LocalDateTime
    val defaultEndTime: LocalDateTime

//    if(currentWeatherFilterGroup.hasFilter(DateTimeFilter::class.simpleName!!)) {
//        defaultStartTime = LocalDateTime.of(dateTimeFilter.startDateTime.toLocalDate(), dateTimeFilter.startDateTime.toLocalTime())
//        defaultEndTime = LocalDateTime.of(dateTimeFilter.endDateTime.toLocalDate(), dateTimeFilter.endDateTime.toLocalTime())
//    }else if(!CollectionUtils.isEmpty(generalHourlyPeriods)) {
//        defaultStartTime = generalHourlyPeriods!![0].startTime.toLocalDateTime()
//        defaultEndTime = generalHourlyPeriods[generalHourlyPeriods.size - 1].startTime.toLocalDateTime()
//    } else {
//        defaultStartTime = LocalDateTime.now()
//        defaultEndTime = defaultStartTime
//    }

    // Used for filters that require typing to try and mitigate
    var debounceJob: Job? = null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        // TODO Move this to notification creation when created
//        DateTimeInput(defaultStartTime.toLocalDate(),
//            defaultStartTime.toLocalTime(),
//            true)
//        {updatedStartTime ->
//            // Check that start time does come after the end time and if it does, move
//            // the end date to be equal to start date.
//            if(dateTimeFilter.endDateTime < updatedStartTime) {
//                dateTimeFilter.endDateTime = updatedStartTime
//            }
//            weatherViewModel.setWeatherFilter(filterGroupName, DateTimeFilter::class.simpleName!!,
//                DateTimeFilter(updatedStartTime, defaultEndTime))
//        }
//
//        DateTimeInput(defaultEndTime.toLocalDate(),
//            defaultEndTime.toLocalTime())
//        {updatedEndTime ->
////                        // Check that end time does come after the end time and if it does, move
////                        // the start date to be equal to end date.
////                        if(updatedEndTime < dateTimeFilter.startDateTime) {
////                            dateTimeFilter.startDateTime = updatedEndTime
////                        }
//
//            weatherViewModel.setWeatherFilter(filterGroupName, DateTimeFilter::class.simpleName!!,
//                DateTimeFilter(defaultStartTime, updatedEndTime))
//        }

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
                        weatherViewModel.removeWeatherFilter(TemperatureFilter::class.simpleName!!, currentWeatherFilterGroup.name)

                        Log.i(LOG_TAG, "Removed temperature range filter")
                    } else {
                        weatherViewModel.setWeatherFilter(
                            TemperatureFilter::class.simpleName!!,
                            TemperatureFilter(updatedLowerTemp, updatedHigherTemp),
                            currentWeatherFilterGroup.name
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
                        currentWeatherFilterGroup.name)
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
                        weatherViewModel.removeWeatherFilter(WeatherKeywordFilter::class.simpleName!!, currentWeatherFilterGroup.name)
                    } else {
                        weatherViewModel.setWeatherFilter(
                            WeatherKeywordFilter::class.simpleName!!,
                            WeatherKeywordFilter(
                                updatedDefaultSelectedKeywords,
                                weatherKeywordFilter.customSelectedKeywords
                            ),
                            currentWeatherFilterGroup.name
                        )
                    }
                    Log.i(LOG_TAG, "Updated weather selectable keywords for filter")
                },
                { updatedCustomSelectedKeywords ->
                    // No delay needed here since user action should reflect immediately
                    if(updatedCustomSelectedKeywords.isEmpty() && weatherKeywordFilter.defaultSelectedKeywords.isEmpty()) {
                        weatherViewModel.removeWeatherFilter(WeatherKeywordFilter::class.simpleName!!, currentWeatherFilterGroup.name)
                    } else {
                        weatherViewModel.setWeatherFilter(
                            WeatherKeywordFilter::class.simpleName!!,
                            WeatherKeywordFilter(
                                weatherKeywordFilter.defaultSelectedKeywords,
                                updatedCustomSelectedKeywords
                            ),
                            currentWeatherFilterGroup.name
                        )
                    }
                    Log.i(LOG_TAG, "Updated weather selectable keywords for filter")
                },
                clearFilters
            )
        }

        SecondaryThemedButtons(if(editingFilterGroup) "Update" else "Save",
            if(editingFilterGroup) "Cancel" else "Clear",
            currentWeatherFilterGroup.hasFilters(),
            {
                weatherViewModel.clearInCreationWeatherFilterGroup(currentWeatherFilterGroup.name)
                clearFilters = true
                // Collapse the editing screen if editing
                if(editingFilterGroup) {
                    cancelEditClicked()
                }
            })
        {
            saveClicked()
        }
    }

    // Set this to false here since if it is true it will run through and clear all the
    // composables but don't want that on any other refreshes.
    clearFilters = false
}