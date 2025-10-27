package com.example.weatherbyagendaandroid.presentation.composable.menu.agenda

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AgendaItemView() {
    var showAddAgendaItemAlertView by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        SavedAgendaItemsView()
        OutlinedButton(
            onClick = { showAddAgendaItemAlertView = true },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSecondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Add Item")
        }
    }

    if(showAddAgendaItemAlertView) {
        AddEditAgendaItemAlertView({ showAddAgendaItemAlertView = false })
    }
}