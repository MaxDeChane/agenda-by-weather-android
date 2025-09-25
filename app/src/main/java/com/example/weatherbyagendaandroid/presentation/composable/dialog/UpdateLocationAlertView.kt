package com.example.weatherbyagendaandroid.presentation.composable.dialog

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.dao.entites.City
import com.example.weatherbyagendaandroid.presentation.domain.SavedLocation
import com.example.weatherbyagendaandroid.presentation.model.LocationViewModel
import com.google.android.gms.common.util.CollectionUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UpdateLocationAlertView(dismiss: () -> Unit,
                            locationViewModel: LocationViewModel = viewModel()
) {
    val LOG_TAG = "UpdateLocationAlertView"

    val scope = rememberCoroutineScope()

    var currentCityState by remember { mutableStateOf("") }
    var selectedCity: City? by remember { mutableStateOf(null) }
    var isSearching by remember { mutableStateOf(false) }
    var searchComplete by remember { mutableStateOf(false) }
    var searchJob: Job? = remember { null }

    val locationOptions by locationViewModel.cityOptions.collectAsStateWithLifecycle()

    AlertDialog(title = {
        Text(text = "New Location")
    },
        text = {
            Column {
                OutlinedTextField(
                    value = currentCityState,
                    onValueChange = {
                        currentCityState = it

                        if(it.isNotBlank()) {
                            isSearching = true
                            searchJob?.cancel()
                            searchJob = scope.launch {
                                delay(1000)
                                locationViewModel.findCityOptionsByName(currentCityState)
                                isSearching = false
                                searchComplete = true
                            }
                        } else {
                            searchJob?.cancel()
                        }},
                    label = { Text("City, State", color = MaterialTheme.colorScheme.onSecondary) },
                    modifier = Modifier.fillMaxWidth()
                )

                if(isSearching) {
                    Text("Loading")
                } else if(searchComplete) {
                    if(CollectionUtils.isEmpty(locationOptions)) {
                        Text("No results found! Please make sure casing is correct and comma" +
                                " between city name and state abbreviation.")
                    } else {
                        Column(
                            Modifier.verticalScroll(rememberScrollState())
                        ) {
                            locationOptions.forEach {
                                Text(
                                    text = "${it.name}, ${it.state}",
                                    modifier = Modifier.fillMaxWidth().clickable{
                                        selectedCity = it
                                        currentCityState = "${it.name}, ${it.state}"
                                        isSearching = false
                                        searchComplete = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        onDismissRequest = {
            dismiss()
        },
        confirmButton = {
            TextButton(
                enabled = selectedCity != null,
                onClick = {
                    Log.i(LOG_TAG, "Selected city is $selectedCity")
                    locationViewModel.addSavedLocation(SavedLocation(
                        "${selectedCity!!.name}, ${selectedCity!!.state}",
                        "${selectedCity!!.name}, ${selectedCity!!.state}",
                        selectedCity!!.latitude!!, selectedCity!!.longitude!!
                    ))
                    dismiss()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    dismiss()
                }
            ) {
                Text("Cancel")
            }
        })
}