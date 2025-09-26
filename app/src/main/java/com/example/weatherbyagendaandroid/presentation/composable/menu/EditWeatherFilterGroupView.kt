package com.example.weatherbyagendaandroid.presentation.composable.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.presentation.model.WeatherViewModel

@Composable
fun EditWeatherFilterGroupView(filterGroupId: Int, closeEditingView: () -> Unit,
                               weatherViewModel: WeatherViewModel = viewModel()
) {
    val inEditWeatherFilterGroup by weatherViewModel.inEditFilterGroupHolders.collectAsStateWithLifecycle()

    WeatherFilterGroupInputView(inEditWeatherFilterGroup[filterGroupId]!!.weatherFilterGroupToEdit,
        true,
        {
            closeEditingView()
            weatherViewModel.removeWeatherFilterGroupFromEditing(filterGroupId)
        }, {
            weatherViewModel.updateWeatherFilterGroup(filterGroupId, it)
        })
}