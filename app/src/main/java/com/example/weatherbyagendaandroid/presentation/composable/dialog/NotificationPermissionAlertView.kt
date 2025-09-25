package com.example.weatherbyagendaandroid.presentation.composable.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

@Composable
fun NotificationPermissionAlertView(onDismissClicked: (dontAskAgain: Boolean) -> Unit, onConfirmClicked: () -> Unit) {

    var dontAskAgain by remember { mutableStateOf(false) }

    AlertDialog(title = {
        Text(text = "Notification Permissions")
    },
        text = {
            Column {
                Text(text = "Please allow notifications to get weather and agenda alerts/notifications.")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = dontAskAgain,
                        onCheckedChange = { dontAskAgain = it }
                    )
                    Text("Don't ask again")
                }
            }
        },
        onDismissRequest = {
            onDismissClicked(dontAskAgain)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmClicked()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissClicked(dontAskAgain)
                }
            ) {
                Text("Dismiss")
            }
        })
}