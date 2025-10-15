package com.example.weatherbyagendaandroid.presentation.composable.menu.notification

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun NotificationPermissionRationalAlertView(onConfirmClicked: () -> Unit) {
    AlertDialog(title = {
        Text(text = "Notification Permissions")
    },
        text = {
            Column {
                Text(text = "Please allow notifications to get weather and agenda alerts/notifications when application not open." +
                        " If denied, will not be prompted again and will have to manually opt in using the Android application permission settings.")
            }
        },
        onDismissRequest = {
            onConfirmClicked()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmClicked()
                }
            ) {
                Text("Confirm")
            }
        })
}