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
import com.example.weatherbyagendaandroid.presentation.domain.WindFilter

@Composable
fun WindRangeInput(
    windRangeFilter: WindFilter,
    onRangeChanged: (updatedLowerTemp: Int, updatedHigherTemp: Int) -> Unit,
    clear: Boolean
) {
    var minWindSpeedText by remember { mutableStateOf(if(windRangeFilter.lowerWindSpeed == -1) "" else windRangeFilter.lowerWindSpeed.toString()) }
    var maxWindSpeedText by remember { mutableStateOf(if(windRangeFilter.higherWindSpeed == Int.MAX_VALUE) "" else windRangeFilter.higherWindSpeed.toString()) }

    if(clear) {
        minWindSpeedText = ""
        maxWindSpeedText = ""
    }

    val keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)

    fun convertWindSpeedStringToInt(temperature: String, defaultTemp: Int): Int {
        return if(temperature.isBlank()) {
            defaultTemp
        } else {
            temperature.toInt()
        }
    }

    fun onWindRangeInput(temperature: String, isLowTemperature: Boolean) {
        if(temperature.matches(Regex("\\d*"))) {
            if(isLowTemperature) {
                minWindSpeedText = temperature
                onRangeChanged(convertWindSpeedStringToInt(temperature, -1), convertWindSpeedStringToInt(maxWindSpeedText, Int.MAX_VALUE))
            } else {
                maxWindSpeedText = temperature
                onRangeChanged(convertWindSpeedStringToInt(minWindSpeedText, -1), convertWindSpeedStringToInt(temperature, Int.MAX_VALUE))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text("Set Wind Speed Range", Modifier.padding(PaddingValues(bottom = 4.dp)),
            color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = minWindSpeedText,
                onValueChange = { onWindRangeInput(it, true) },
                label = { Text("Min Wind Speed", color = MaterialTheme.colorScheme.onSecondary) },
                keyboardOptions = keyboardOptions,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = maxWindSpeedText,
                onValueChange = { onWindRangeInput(it, false) },
                label = { Text("Max Wind Speed", color = MaterialTheme.colorScheme.onSecondary) },
                keyboardOptions = keyboardOptions,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
