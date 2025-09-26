package com.example.weatherbyagendaandroid.presentation.composable.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.presentation.model.MenuViewModel
import com.example.weatherbyagendaandroid.presentation.model.WeatherViewModel

@Composable
fun CreateNewWeatherFilterGroupView(menuViewModel: MenuViewModel = viewModel(), weatherViewModel: WeatherViewModel = viewModel()) {

//    var showNameFilterGroupAlertView by remember { mutableStateOf(false) }
    val inCreationFilterGroup by weatherViewModel.inCreationFilterGroup.collectAsStateWithLifecycle()
    val showWeatherFilterGroupsExpanded by menuViewModel.showWeatherFilterGroupsExpanded.collectAsStateWithLifecycle()

    fun handleSave(filterGroupName: String) {
        val savedFilterGroupId = weatherViewModel.saveNewWeatherFilterGroup(filterGroupName)
        menuViewModel.showWeatherFiltersClick()
        if(!showWeatherFilterGroupsExpanded) {
            menuViewModel.showWeatherFilterGroupsExpansionClick()
        }



        weatherViewModel.addRemoveSelectedWeatherFilterGroup(savedFilterGroupId)
    }

    WeatherFilterGroupInputView(inCreationFilterGroup, false, {}, { handleSave(it) })

//    if(showNameFilterGroupAlertView) {
//        NameNewFilterGroupAlertView({ showNameFilterGroupAlertView = false}, ) {
//            handleSave(it)
//        }
//    }
}