package com.example.weatherbyagendaandroid.presentation.composable.menu

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun DateTimeInput(initialDateTime: LocalDateTime,
                  isStartDateTime: Boolean = false,
                  onDateTimeSelected: (LocalDateTime) -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDateTime.toLocalDate()) }
    var selectedTime by remember { mutableStateOf(initialDateTime.toLocalTime()) }

    val context = LocalContext.current

    // Date picker dialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            onDateTimeSelected(LocalDateTime.of(selectedDate, selectedTime))
        },
        selectedDate.year,
        selectedDate.monthValue - 1,
        selectedDate.dayOfMonth
    ).apply {
        datePicker.minDate = System.currentTimeMillis() // today as min date
    }

    // Time picker dialog
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, _ ->
            selectedTime = LocalTime.of(hour, 0)
            onDateTimeSelected(LocalDateTime.of(selectedDate, selectedTime))
        },
        selectedTime.hour,
        selectedTime.minute,
        false
    )

    val startEndText = if(isStartDateTime) "Start" else "End"

    Column {
        Text(
            text = "$startEndText Time:",
            color = MaterialTheme.colorScheme.onSecondary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = selectedTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
            color = MaterialTheme.colorScheme.onSecondary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { timePickerDialog.show() }
        )
        Text(
            text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            color = MaterialTheme.colorScheme.onSecondary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { datePickerDialog.show() }
        )
    }
}