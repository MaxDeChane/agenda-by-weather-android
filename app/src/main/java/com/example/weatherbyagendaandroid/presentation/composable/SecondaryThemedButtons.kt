package com.example.weatherbyagendaandroid.presentation.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SecondaryThemedButtons(
    rightButtonText: String,
    leftButtonText: String,
    isSaveEnabled: Boolean,
    onClearClick: () -> Unit,
    onButtonClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Clear button (outlined style)
        OutlinedButton(
            onClick = onClearClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSecondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(leftButtonText)
        }
        // Secondary button/
        OutlinedButton(
            enabled = isSaveEnabled,
            onClick = onButtonClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSecondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(rightButtonText)
        }
    }
}
