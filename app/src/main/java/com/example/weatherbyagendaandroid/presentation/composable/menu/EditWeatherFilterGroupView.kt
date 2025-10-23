package com.example.weatherbyagendaandroid.presentation.composable.menu

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.presentation.domain.WeatherFilterGroup
import com.example.weatherbyagendaandroid.presentation.model.WeatherFilterViewModel

@Composable
fun EditWeatherFilterGroupView(filterGroupId: Int, weatherFilterGroupToEdit: WeatherFilterGroup,
                               closeEditingView: () -> Unit,
                               weatherFilterViewModel: WeatherFilterViewModel = viewModel()
) {

    WeatherFilterGroupInputView(weatherFilterGroupToEdit,
        true,
        {
            closeEditingView()
            weatherFilterViewModel.removeWeatherFilterGroupFromEditing(filterGroupId)
        }, {
            weatherFilterViewModel.updateWeatherFilterGroup(filterGroupId)
        })
}