package com.example.weatherbyagendaandroid.presentation.composable.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun NameNewFilterGroupAlertView(cancelDialog: () -> Unit, saveFilterGroup: (filterGroupName: String) -> Unit) {
    var currentFilterGroupName by remember { mutableStateOf("") }
    var showFilterGroupNameNeeded by remember { mutableStateOf(false) }

    AlertDialog(title = {
        Text(text = "Filter Group Name")
    },
        text = {
            Column {
                OutlinedTextField(
                    value = currentFilterGroupName,
                    onValueChange = { currentFilterGroupName = it },
                    label = { Text("Filter Group Name", color = MaterialTheme.colorScheme.onSecondary) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (showFilterGroupNameNeeded) {
                    Text("*A filter group name is required to save.", color = Color.Red)
                }
            }
        },
        onDismissRequest = {
            cancelDialog()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if(currentFilterGroupName.isNotBlank()) {
                        saveFilterGroup(currentFilterGroupName)
                    } else {
                        showFilterGroupNameNeeded = true
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    cancelDialog()
                }
            ) {
                Text("Cancel")
            }
        })
}