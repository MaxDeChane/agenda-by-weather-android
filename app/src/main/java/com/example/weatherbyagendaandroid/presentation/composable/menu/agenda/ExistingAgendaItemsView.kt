package com.example.weatherbyagendaandroid.presentation.composable.menu.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.domain.agenda.AgendaItem
import com.example.weatherbyagendaandroid.domain.agenda.AgendaItems
import com.example.weatherbyagendaandroid.presentation.model.AgendaViewModel

@Composable
fun ExistingAgendaItemsView(agendaViewModel: AgendaViewModel = viewModel()
) {
    var expandedAgendaItems by remember { mutableStateOf(AgendaItems()) }
    var inEditAgendaItem by remember { mutableStateOf<AgendaItem?>(null) }

    val agendaItems by agendaViewModel.agendaItems.collectAsStateWithLifecycle()

    fun expandAgendaItemInfo(agendaItem: AgendaItem) {
        expandedAgendaItems = if(expandedAgendaItems.containsAgendaItem(agendaItem.id)) {
            expandedAgendaItems.deleteAgendaItem(agendaItem.id)
        } else {
            expandedAgendaItems.addAgendaItem(agendaItem)
        }
    }

    if(agendaItems.items.isNotEmpty()) {
        Column {
            agendaItems.items.forEach { (id, agendaItem) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandAgendaItemInfo(agendaItem)
                            }
                    ) {
                        Text(
                            text = agendaItem.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "View",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 8.dp).clickable {
                                expandAgendaItemInfo(agendaItem)
                            }
                        )
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.clickable {
                                inEditAgendaItem = agendaItem.copy()
                            }
                        )
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 8.dp).clickable {
                                agendaViewModel.deleteAgendaItem(id)
                            }
                        )
                    }

                    if (expandedAgendaItems.containsAgendaItem(id)) {
                        ExpandedAgendaItemView(agendaItem)
                    }
                }

                HorizontalDivider(thickness = 2.dp)
            }
        }

        if(inEditAgendaItem != null) {
            AddEditAgendaItemAlertView({ inEditAgendaItem = null }, inEditAgendaItem)
        }
    }  else {
        Box(Modifier.background(MaterialTheme.colorScheme.secondary)) {
            Text("No weather filter groups available. Please create one using the Weather Filters tab.",
                Modifier.padding(8.dp))
        }
    }
}