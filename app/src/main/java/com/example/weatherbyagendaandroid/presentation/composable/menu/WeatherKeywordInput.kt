package com.example.weatherbyagendaandroid.presentation.composable.menu

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import com.example.weatherbyagendaandroid.presentation.domain.WeatherKeywordFilter
import com.example.weatherbyagendaandroid.presentation.model.WeatherFilterViewModel

@SuppressLint("MutableCollectionMutableState")
@Composable
fun WeatherKeywordInput(
    weatherKeywordFilter: WeatherKeywordFilter,
    onDefaultSelectedKeywordsUpdated: (updatedSelectedKeywords: Set<String>) -> Unit,
    onCustomSelectedKeywordsUpdated: (updatedCustomKeywords: Set<String>) -> Unit,
    clear: Boolean,
    weatherFilterViewModel: WeatherFilterViewModel = viewModel()
) {
    val LOG_TAG = "WeatherKeywordInput"

    var currentDefaultSelectedKeywords by remember { mutableStateOf(weatherKeywordFilter.defaultSelectedKeywords) }
    var currentCustomSelectedKeywords by remember { mutableStateOf(weatherKeywordFilter.customSelectedKeywords) }

    var customKeywordOptions by remember { mutableStateOf(setOf<String>()) }
    var customKeyword by remember { mutableStateOf("") }

    if(clear || weatherFilterViewModel.hasSelectedWeatherFilterGroup()) {
        currentDefaultSelectedKeywords = setOf()
        currentCustomSelectedKeywords = setOf()
        customKeywordOptions = setOf()
        customKeyword = ""
    }

    fun customKeywordAdd() {
        val newList = customKeywordOptions.toMutableSet()
        newList.add(customKeyword)
        customKeywordOptions = newList.toSet()
        Log.i(LOG_TAG, "Updated weather selectable keywords for filter")
    }

    @Composable
    fun createKeywordSelectionUI(keywordOptions: Collection<String>, alreadySelectedKeywords: Set<String>,
                                 isCustomKeyword: Boolean, onSelectedKeywordsUpdated: (updatedSelectedKeywords: Set<String>) -> Unit) {
        keywordOptions.forEach { keyword ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = alreadySelectedKeywords.contains(keyword),
                        onClick = {
                            val updatedSelectedKeywords = alreadySelectedKeywords.toMutableSet()
                            if (updatedSelectedKeywords.contains(keyword)) {
                                updatedSelectedKeywords.remove(keyword)
                            } else {
                                updatedSelectedKeywords.add(keyword)
                            }

                            // Set the new value on the appropriate state variable to cause
                            // a refresh to reflect changes.
                            if(isCustomKeyword) {
                                currentCustomSelectedKeywords = updatedSelectedKeywords.toSet()
                            } else {
                                currentDefaultSelectedKeywords = updatedSelectedKeywords.toSet()
                            }
                            onSelectedKeywordsUpdated(updatedSelectedKeywords.toSet())
                        }
                    )
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = currentDefaultSelectedKeywords.contains(keyword) || currentCustomSelectedKeywords.contains(keyword),
                    onClick = null // handled by parent
                )
                Spacer(Modifier.width(8.dp))
                Text(keyword, color = MaterialTheme.colorScheme.onSecondary)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        createKeywordSelectionUI(defaultKeywordOptions, currentDefaultSelectedKeywords, false, onDefaultSelectedKeywordsUpdated)
        createKeywordSelectionUI(customKeywordOptions, currentCustomSelectedKeywords, true, onCustomSelectedKeywordsUpdated)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = customKeyword,
            onValueChange = { customKeyword = it },
            label = { Text("Custom Keyword", color = MaterialTheme.colorScheme.onSecondary) },
            modifier = Modifier.fillMaxWidth()
        )
        Button({
            customKeywordAdd()
            val updatedSelectedKeywords = customKeywordOptions.toMutableList()
            updatedSelectedKeywords.add(customKeyword)

            currentCustomSelectedKeywords = updatedSelectedKeywords.toSet()
            onCustomSelectedKeywordsUpdated(currentCustomSelectedKeywords)
            customKeyword = "" },
            enabled = customKeyword.isNotBlank() && !customKeywordOptions.contains(customKeyword)){
            Text("Add Keyword", color = MaterialTheme.colorScheme.onSecondary)
        }
    }
}

val defaultKeywordOptions = listOf(
    "Rain",
    "Snow",
    "Sunny",
    "Clear",
    "Cloudy",
    "Windy",
    "Fog",
    "Storm",
    "Drizzle",
    "Thunder",
    "Haze"
)
