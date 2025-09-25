package com.example.weatherbyagendaandroid.presentation.composable.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.presentation.composable.dialog.NameNewFilterGroupAlertView
import com.example.weatherbyagendaandroid.presentation.model.MenuViewModel
import com.example.weatherbyagendaandroid.presentation.model.WeatherViewModel

@Composable
fun CreateNewWeatherFilterGroupView(menuViewModel: MenuViewModel = viewModel(), weatherViewModel: WeatherViewModel = viewModel()) {

    var showNameFilterGroupAlertView by remember { mutableStateOf(false) }
    val inCreationFilterGroup by weatherViewModel.inCreationFilterGroup.collectAsStateWithLifecycle()
    val showWeatherFilterGroupsExpanded by menuViewModel.showWeatherFilterGroupsExpanded.collectAsStateWithLifecycle()

    fun handleSave(filterGroupName: String) {
        weatherViewModel.saveNewWeatherFilterGroup(filterGroupName)
        menuViewModel.showWeatherFiltersClick()
        if(!showWeatherFilterGroupsExpanded) {
            menuViewModel.showWeatherFilterGroupsExpansionClick()
        }
        weatherViewModel.addRemoveSelectedWeatherFilterGroup(filterGroupName)
    }

    WeatherFilterGroupInputView(inCreationFilterGroup, false, {}, {
        showNameFilterGroupAlertView = true
    })

    if(showNameFilterGroupAlertView) {
        NameNewFilterGroupAlertView({ showNameFilterGroupAlertView = false}, ) {
            handleSave(it)
        }
    }
}