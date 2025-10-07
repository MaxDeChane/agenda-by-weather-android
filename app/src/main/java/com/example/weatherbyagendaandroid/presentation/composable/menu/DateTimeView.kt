package com.example.weatherbyagendaandroid.presentation.composable.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun DateTimeView(startEndText: String, time: LocalTime, date: LocalDate,
                 timeClickHandler: () -> Unit = {},
                 dateClickHandler: () -> Unit = {}) {
    Column {
        Text(
            text = "$startEndText Time:",
            color = MaterialTheme.colorScheme.onSecondary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = time.format(DateTimeFormatter.ofPattern("hh:mm a")),
            color = MaterialTheme.colorScheme.onSecondary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { timeClickHandler() }
        )
        Text(
            text = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            color = MaterialTheme.colorScheme.onSecondary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { dateClickHandler() }
        )
    }
}