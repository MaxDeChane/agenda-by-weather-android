package com.example.weatherbyagendaandroid.presentation.composable.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.presentation.model.WeatherFilterViewModel

@Composable
fun EditWeatherFilterGroupView(filterGroupId: Int, closeEditingView: () -> Unit,
                               weatherFilterViewModel: WeatherFilterViewModel = viewModel()
) {
    val inEditWeatherFilterGroup by weatherFilterViewModel.inEditFilterGroupHolders.collectAsStateWithLifecycle()

    WeatherFilterGroupInputView(inEditWeatherFilterGroup[filterGroupId]!!.weatherFilterGroupToEdit,
        true,
        {
            closeEditingView()
            weatherFilterViewModel.removeWeatherFilterGroupFromEditing(filterGroupId)
        }, {
            weatherFilterViewModel.updateWeatherFilterGroup(filterGroupId)
        })
}