package com.example.weatherbyagendaandroid.presentation.composable.menu.datetime

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun InvalidDateTimeInputAlert(isStartDateTime: Boolean, onConfirmClicked: () -> Unit) {
    val alertText = if(isStartDateTime)  "Start Date and Time can't come before the current time." else
        "End Date and Time needs to be after the Start Date and Time."
    AlertDialog(title = {
        Text(text = "Invalid Date Time Selection")
    },
        text = {
            Column {
                Text(text = "$alertText Can't travel back in time... yet.")
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