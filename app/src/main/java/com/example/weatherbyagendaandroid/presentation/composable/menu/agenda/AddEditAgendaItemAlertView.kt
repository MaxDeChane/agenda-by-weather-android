package com.example.weatherbyagendaandroid.presentation.composable.menu.agenda

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.domain.agenda.AgendaItem
import com.example.weatherbyagendaandroid.presentation.composable.ExpandableView
import com.example.weatherbyagendaandroid.presentation.composable.menu.datetime.DateTimeInput
import com.example.weatherbyagendaandroid.presentation.composable.menu.SavedLocationSelectionView
import com.example.weatherbyagendaandroid.presentation.composable.menu.WeatherFilterGroupsSelectionView
import com.example.weatherbyagendaandroid.presentation.composable.menu.notification.NotificationPermissionRationalAlertView
import com.example.weatherbyagendaandroid.presentation.model.AgendaViewModel
import com.example.weatherbyagendaandroid.presentation.model.LocationViewModel
import com.example.weatherbyagendaandroid.presentation.model.PermissionsViewModel
import com.example.weatherbyagendaandroid.presentation.model.WeatherViewModel
import java.time.LocalDateTime

@Composable
fun AddEditAgendaItemAlertView(
    dismiss: () -> Unit, agendaItemToEdit: AgendaItem? = null,
    locationViewModel: LocationViewModel = viewModel(),
    agendaViewModel: AgendaViewModel = viewModel(),
    weatherViewModel: WeatherViewModel = viewModel(),
    permissionsViewModel: PermissionsViewModel = viewModel()
) {
    val context = LocalContext.current
    val isEditing = agendaItemToEdit != null

    val startDateNotBefore = LocalDateTime.now().plusHours(1).withMinute(0)

    var agendaItemName by remember {
        mutableStateOf(
            if (isEditing) {
                agendaItemToEdit.name
            } else ""
        )
    }
    var startDateTime by remember {
        mutableStateOf(
            if (isEditing) {
                agendaItemToEdit.startTime
            } else {
                val endPeriod = weatherViewModel.retrieveLastGeneralWeatherPeriodEndDate()
                if(endPeriod != null) {
                    endPeriod.toLocalDateTime()
                } else {
                    LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0)
                }
            }
        )
    }
    var endDateTime by remember {
        mutableStateOf(
            if (isEditing) {
                agendaItemToEdit.endTime
            } else startDateTime.plusHours(1)
        )
    }
    var locationExpanded by remember { mutableStateOf(false) }
    var selectedLocationId by remember {
        mutableIntStateOf(
            if (isEditing) {
                agendaItemToEdit.locationId
            } else -1
        )
    }
    var weatherFilterGroupsExpanded by remember { mutableStateOf(false) }
    var selectedFilterGroupId by remember {
        mutableIntStateOf(
            if (isEditing) {
                agendaItemToEdit.weatherFilterGroupId
            } else -1
        )
    }

    var showNotificationPermissionRationalAlertView by remember { mutableStateOf(false) }

    val gpsLocation by locationViewModel.gpsLocation.collectAsStateWithLifecycle()
    val savedLocations by locationViewModel.savedLocations.collectAsStateWithLifecycle()

    if(showNotificationPermissionRationalAlertView) {
        NotificationPermissionRationalAlertView({
            showNotificationPermissionRationalAlertView = false
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                0
            )
            dismiss()
        })
    }

    AlertDialog(
        title = {
            Text(text = "Add Agenda Item")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = agendaItemName,
                    onValueChange = {
                        agendaItemName = it
                    },
                    label = {
                        Text(
                            "Agenda Item Name",
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DateTimeInput(startDateTime, true, startDateNotBefore) {
                        startDateTime = it
                        // If selected startDateTime is after the endDateTime, update the
                        // endDateTime to be after the startDateTime since it can be assumed
                        // that is the intention of the user and time can't go backwords.
                        if(it.isAfter(endDateTime)) {
                            endDateTime = it.plusHours(1)
                        }
                    }
                    DateTimeInput(endDateTime, false, startDateTime) { endDateTime = it }
                }

                ExpandableView("Location", locationExpanded) {
                    locationExpanded = !locationExpanded
                }

                if (locationExpanded) {
                    SavedLocationSelectionView(selectedLocationId, savedLocations, false, {
                        if (selectedLocationId == it) {
                            selectedLocationId = -1
                        } else {
                            selectedLocationId = it
                        }
                    })
                }

                ExpandableView(
                    "Weather Filter Groups",
                    weatherFilterGroupsExpanded
                ) { weatherFilterGroupsExpanded = !weatherFilterGroupsExpanded }

                if (weatherFilterGroupsExpanded) {
                    WeatherFilterGroupsSelectionView(selectedFilterGroupId, false, {
                        if (selectedFilterGroupId == it) {
                            selectedFilterGroupId = -1
                        } else {
                            selectedFilterGroupId = it
                        }
                    })
                }
            }
        },
        onDismissRequest = {
            dismiss()
        },
        confirmButton = {
            TextButton(
                enabled = agendaItemName.isNotBlank(),
                onClick = {
                    val id = agendaItemToEdit?.id ?: -1
                    val agendaItem = AgendaItem(
                        id, agendaItemName, startDateTime, endDateTime,
                        selectedLocationId, selectedFilterGroupId
                    )
                    agendaViewModel.addAgendaItem(agendaItem)

                    if (!permissionsViewModel.checkNotificationPermissions(context)) {
                        val permission = android.Manifest.permission.POST_NOTIFICATIONS
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                context as Activity,
                                permission
                            )
                        ) {
                            showNotificationPermissionRationalAlertView = true
                        } else {
                            ActivityCompat.requestPermissions(
                                context,
                                arrayOf(permission),
                                0
                            )
                            dismiss()
                        }
                    } else {
                        dismiss()
                    }
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