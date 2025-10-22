package com.example.weatherbyagendaandroid.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.R
import com.example.weatherbyagendaandroid.presentation.model.FilterStatus
import com.example.weatherbyagendaandroid.presentation.model.WeatherFilterViewModel
import com.example.weatherbyagendaandroid.presentation.model.WeatherViewModel

@Composable
fun WeatherGeneralHourlyPeriodsView(
    innerPadding: PaddingValues,
    loadFromIntent: Boolean,
    weatherFilterViewModel: WeatherFilterViewModel = viewModel(),
    weatherViewModel: WeatherViewModel = viewModel()
) {
    val LOG_TAG = remember { "WeatherGeneralHourlyPeriodsView" }

    val weatherPeriodDisplayBlocks by weatherViewModel.weatherPeriodDisplayBlocks.collectAsStateWithLifecycle()
    val filterStatus by weatherFilterViewModel.filterStatus.collectAsStateWithLifecycle()
    var refreshKey by remember { mutableStateOf(false) }

    // Run the weather periods through the filters whenever the weather periods change due
    // to location change or just weather update.
    LaunchedEffect(weatherPeriodDisplayBlocks) {
        if(weatherPeriodDisplayBlocks != null) {
            weatherFilterViewModel.runWeatherDisplayBlockThroughFilters(
                weatherPeriodDisplayBlocks!!
            )
            refreshKey = !refreshKey
        } else if(!loadFromIntent) {
            // If the weatherPeriodDisplayBlocks is null and not being loaded from the intent
            // then just load it using the current location of the phone.
            weatherViewModel.updateWeatherInfoUsingCurrentLocation()
        }
    }

    // Run weatherPeriods through the selected filters whenever the filter status is changed
    // and in progress.
    LaunchedEffect(filterStatus.name) {
        if(weatherPeriodDisplayBlocks != null) {
            if (filterStatus == FilterStatus.IN_PROGRESS) {
                weatherFilterViewModel.runWeatherDisplayBlockThroughFilters(
                    weatherPeriodDisplayBlocks!!
                )
                refreshKey = !refreshKey
            }
        }
    }

    if (filterStatus == FilterStatus.IN_PROGRESS) {
        Box(
            modifier = Modifier
                .fillMaxSize() // take up the entire available space
                .background(MaterialTheme.colorScheme.primary) // your background color
        ) {
            Text("Filtering...", Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.displayLarge)
        }
    } else if(weatherPeriodDisplayBlocks != null) {
        LazyColumn (
            Modifier
                .padding(innerPadding)) {
            items(weatherPeriodDisplayBlocks!!, { "${it.generalWeatherPeriod.startTime}-$refreshKey" }) { weatherPeriodDisplayBlock ->
                val generalPeriod = weatherPeriodDisplayBlock.generalWeatherPeriod
                if (!weatherPeriodDisplayBlock.isWholeBlockFiltered) {
                    Column(
                        Modifier
                            .background(generalPeriod.backgroundColor)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${generalPeriod.name}: ${generalPeriod.temperature}° ${generalPeriod.temperatureUnit}, ${generalPeriod.shortForecast}",
                            color = generalPeriod.textColor
                        )
                        Icon(
                            painter = painterResource(id = generalPeriod.icon),
                            contentDescription = generalPeriod.shortForecast,
                            tint = generalPeriod.textColor,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }

                    HorizontalDivider(color = Color.Black, thickness = 1.dp)

                    LazyRow(
                        Modifier
                            .border(1.dp, Color.Gray)
                            .height(175.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        items(weatherPeriodDisplayBlock.hourlyWeatherPeriods, { "${it.startTime}-$refreshKey" }) {currentHourlyPeriod ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    Modifier
                                        .background(currentHourlyPeriod.backgroundColor)
                                        .border(1.dp, Color.Gray)
                                        .fillMaxHeight()
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        painter = painterResource(id = currentHourlyPeriod.icon),
                                        contentDescription = currentHourlyPeriod.shortForecast,
                                        tint = currentHourlyPeriod.textColor
                                    )

                                    // TODO Add actual call to http://codes.wmo.int/common/unit/{unit}
                                    // as described in the weather api documentation.
                                    Text(
                                        "PoP: ${currentHourlyPeriod.probabilityOfPrecipitation.value}%",
                                        fontSize = 12.sp,
                                        color = currentHourlyPeriod.textColor
                                    )

                                    Text(
                                        "Wind: ${currentHourlyPeriod.windSpeed} ${currentHourlyPeriod.windDirection}",
                                        fontSize = 12.sp,
                                        color = currentHourlyPeriod.textColor
                                    )

                                    Column(
                                        Modifier.fillMaxHeight(),
                                        Arrangement.Bottom, Alignment.CenterHorizontally
                                    ) {
                                        Row {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_thermometer),
                                                contentDescription = "Thermometer Icon",
                                                tint = currentHourlyPeriod.textColor
                                            )

                                            Text(
                                                "${currentHourlyPeriod.temperature}° ${currentHourlyPeriod.temperatureUnit}",
                                                fontSize = 12.sp,
                                                color = currentHourlyPeriod.textColor
                                            )
                                        }

                                        VerticalDivider(
                                            modifier = Modifier.height(currentHourlyPeriod.temperatureTrendLineHeight),
                                            color = currentHourlyPeriod.temperatureTrendColor,
                                            thickness = 4.dp
                                        )

                                        Text(
                                            currentHourlyPeriod.startDisplayTime,
                                            fontSize = 12.sp,
                                            color = currentHourlyPeriod.textColor
                                        )
                                    }
                                }
                                if (currentHourlyPeriod.filtered) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(Color.Black.copy(alpha = 0.5f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
