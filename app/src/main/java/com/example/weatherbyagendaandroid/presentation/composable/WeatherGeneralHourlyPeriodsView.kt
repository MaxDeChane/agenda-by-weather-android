package com.example.weatherbyagendaandroid.presentation.composable

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.R
import com.example.weatherbyagendaandroid.dao.domain.WeatherPeriod
import com.example.weatherbyagendaandroid.presentation.model.LocationViewModel
import com.example.weatherbyagendaandroid.presentation.model.WeatherViewModel

@Composable
fun WeatherGeneralHourlyPeriodsView(
    innerPadding: PaddingValues,
    weatherViewModel: WeatherViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel()
) {
    val LOG_TAG = remember { "WeatherGeneralHourlyPeriodsView" }

    val gpsLocation by locationViewModel.gpsLocation.collectAsStateWithLifecycle()
    val currentSelectedLocation by locationViewModel.selectedSavedLocation.collectAsStateWithLifecycle()
    val weatherPeriodDisplayBlocks by weatherViewModel.weatherPeriodDisplayBlocks.collectAsStateWithLifecycle()
    val weatherFilterGroups by weatherViewModel.weatherFilterGroups.collectAsStateWithLifecycle()
    val selectedWeatherFilterGroupNames by weatherViewModel.selectedWeatherFilterGroups.collectAsStateWithLifecycle()
    val inProgressWeatherFilterGroup by weatherViewModel.inCreationFilterGroup.collectAsStateWithLifecycle()

    Log.i(LOG_TAG, "WeatherFilterGroups: $weatherFilterGroups")

    if(currentSelectedLocation != null) {
        weatherViewModel.updateWeatherInfo(currentSelectedLocation!!.latitude, currentSelectedLocation!!.longitude)
    } else if(gpsLocation != null) {
        weatherViewModel.updateWeatherInfo(gpsLocation!!.latitude, gpsLocation!!.longitude)
    }

    Column(
        Modifier
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())) {
        if (weatherPeriodDisplayBlocks != null) {
            for (weatherPeriodDisplayBlock in weatherPeriodDisplayBlocks!!) {
                weatherFilterGroups.runWeatherDisplayBlockThroughFilters(
                    weatherPeriodDisplayBlock,
                    selectedWeatherFilterGroupNames,
                    inProgressWeatherFilterGroup
                )
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

                    Row(
                        Modifier
                            .border(1.dp, Color.Gray)
                            .height(175.dp)
                            .horizontalScroll(rememberScrollState()),
                        Arrangement.Center
                    ) {
                        for (currentHourlyPeriod in weatherPeriodDisplayBlock.hourlyWeatherPeriods) {
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