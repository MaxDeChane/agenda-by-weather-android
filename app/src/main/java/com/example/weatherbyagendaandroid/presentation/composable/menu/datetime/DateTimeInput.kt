package com.example.weatherbyagendaandroid.presentation.composable.menu.datetime

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

@Composable
fun DateTimeInput(initialDateTime: LocalDateTime,
                  isStartDateTime: Boolean = false,
                  notBeforeDateTime: LocalDateTime,
                  onDateTimeSelected: (updateDateTime: LocalDateTime) -> Unit
) {
    var showInvalidDateTimeInputAlert by remember { mutableStateOf(false) }

    val context = LocalContext.current

    var selectedDate = initialDateTime.toLocalDate()
    var selectedTime = initialDateTime.toLocalTime()

    if(showInvalidDateTimeInputAlert) {
        InvalidDateTimeInputAlert(isStartDateTime) { showInvalidDateTimeInputAlert = false }
    }

    // Date picker dialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            // If the date selected in the notBeforeDate then set the time
            // to the notBeforeDateTime to make sure it isn't in the past.
            if(isStartDateTime && selectedDate == notBeforeDateTime.toLocalDate()) {
                selectedTime = notBeforeDateTime.toLocalTime()
            }

            onDateTimeSelected(LocalDateTime.of(selectedDate, selectedTime))
        },
        selectedDate.year,
        selectedDate.monthValue - 1,
        selectedDate.dayOfMonth
    ).apply {
        datePicker.minDate = notBeforeDateTime.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli() // today as min date
    }

    // Time picker dialog
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            val updatedSelectedTime = LocalDateTime.of(selectedDate, LocalTime.of(hour, minute))
            if(updatedSelectedTime.isBefore(notBeforeDateTime)) {
                showInvalidDateTimeInputAlert = true
            } else {
                selectedTime = LocalTime.of(hour, minute)
                onDateTimeSelected(updatedSelectedTime)
            }
        },
        selectedTime.hour,
        selectedTime.minute,
        false
    )

    val startEndText = if(isStartDateTime) "Start" else "End"

    DateTimeView(startEndText, selectedTime, selectedDate,
        { timePickerDialog.show() }, { datePickerDialog.show() })
}