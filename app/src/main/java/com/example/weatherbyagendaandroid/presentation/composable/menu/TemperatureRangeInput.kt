package com.example.weatherbyagendaandroid.presentation.composable.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.presentation.domain.TemperatureFilter
import com.example.weatherbyagendaandroid.presentation.model.WeatherFilterViewModel

@Composable
fun TemperatureRangeInput(
    temperatureRangeFilter: TemperatureFilter,
    onRangeChanged: (updatedLowerTemp: Int, updatedHigherTemp: Int) -> Unit,
    clear: Boolean,
    weatherFilterViewModel: WeatherFilterViewModel = viewModel()
) {
    var lowerTempText by remember { mutableStateOf(if(temperatureRangeFilter.lowerTemperature == Int.MIN_VALUE) "" else temperatureRangeFilter.lowerTemperature.toString()) }
    var higherTempText by remember { mutableStateOf(if(temperatureRangeFilter.higherTemperature == Int.MAX_VALUE) "" else temperatureRangeFilter.higherTemperature.toString()) }

    if(clear || weatherFilterViewModel.hasSelectedWeatherFilterGroup()) {
        lowerTempText = ""
        higherTempText = ""
    }

    val keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)

    fun convertTempStringToInt(temperature: String, defaultTemp: Int): Int {
        return if(temperature.isBlank() || temperature == "-") {
            defaultTemp
        } else {
            temperature.toInt()
        }
    }

    fun onTemperatureChange(temperature: String, isLowTemperature: Boolean) {
        if(temperature.matches(Regex("-?\\d*"))) {
            if(isLowTemperature) {
                lowerTempText = temperature
                onRangeChanged(convertTempStringToInt(temperature, Int.MIN_VALUE), convertTempStringToInt(higherTempText, Int.MAX_VALUE))
            } else {
                higherTempText = temperature
                onRangeChanged(convertTempStringToInt(lowerTempText, Int.MIN_VALUE), convertTempStringToInt(temperature, Int.MAX_VALUE))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text("Set Temperature Range", Modifier.padding(PaddingValues(bottom = 4.dp)),
            color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = lowerTempText,
                onValueChange = { onTemperatureChange(it, true) },
                label = { Text("Min Temp", color = MaterialTheme.colorScheme.onSecondary) },
                keyboardOptions = keyboardOptions,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = higherTempText,
                onValueChange = { onTemperatureChange(it, false) },
                label = { Text("Max Temp", color = MaterialTheme.colorScheme.onSecondary) },
                keyboardOptions = keyboardOptions,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
