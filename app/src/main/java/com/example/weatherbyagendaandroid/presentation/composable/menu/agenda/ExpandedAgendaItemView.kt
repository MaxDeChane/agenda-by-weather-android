package com.example.weatherbyagendaandroid.presentation.composable.menu.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.domain.agenda.AgendaItem
import com.example.weatherbyagendaandroid.presentation.composable.menu.DateTimeView
import com.example.weatherbyagendaandroid.presentation.model.LocationViewModel

@Composable
fun ExpandedAgendaItemView(agendaItem: AgendaItem,
                           locationViewModel: LocationViewModel = viewModel()) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DateTimeView(
                "Start", agendaItem.startTime.toLocalTime(),
                agendaItem.startTime.toLocalDate()
            )
            DateTimeView(
                "End", agendaItem.endTime.toLocalTime(),
                agendaItem.endTime.toLocalDate()
            )
        }
    }
}