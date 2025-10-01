package com.example.weatherbyagendaandroid.presentation.composable.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.presentation.composable.ExpandableView
import com.example.weatherbyagendaandroid.presentation.composable.menu.agenda.AgendaItemView
import com.example.weatherbyagendaandroid.presentation.model.MenuViewModel

@Composable
fun MenuView(
    innerPadding: PaddingValues,
    menuViewModel: MenuViewModel = viewModel()
) {
    val LOG_TAG = "WeatherFilters"

    val showAgendaItemsExpanded by menuViewModel.showAgendaItemsExpanded.collectAsStateWithLifecycle()
    val showWeatherFiltersExpanded by menuViewModel.showWeatherFiltersExpanded.collectAsStateWithLifecycle()
    val showWeatherFilterGroupsExpanded by menuViewModel.showWeatherFilterGroupsExpanded.collectAsStateWithLifecycle()
    val showUpdateLocationExpanded by menuViewModel.showUpdateLocationExpanded.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxWidth(), Alignment.CenterEnd) {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.primary)
                .border(1.dp, Color.Black)
                .fillMaxHeight()
                .fillMaxWidth(.5f)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            ExpandableView("Agenda Items", showAgendaItemsExpanded, menuViewModel::showAgendaItemsClick)

            if(showAgendaItemsExpanded) {
                AgendaItemView()
            }

            ExpandableView("Weather Filters", showWeatherFiltersExpanded, menuViewModel::showWeatherFiltersClick)

            if(showWeatherFiltersExpanded) {
                CreateNewWeatherFilterGroupView()
            }

            ExpandableView("Weather Filter Groups", showWeatherFilterGroupsExpanded, menuViewModel::showWeatherFilterGroupsExpansionClick)

            if(showWeatherFilterGroupsExpanded) {
                WeatherFilterGroupsMenuView()
            }

            ExpandableView("Locations", showUpdateLocationExpanded, menuViewModel::showUpdateLocationExpansionClick)

            if(showUpdateLocationExpanded) {
                LocationView()
            }
        }
    }
}