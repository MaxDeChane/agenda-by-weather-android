package com.example.weatherbyagendaandroid.presentation.composable.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.presentation.model.WeatherFilterViewModel

@Composable
fun WeatherFilterGroupsMenuView(weatherFilterViewModel: WeatherFilterViewModel = viewModel()) {

    val currentFilterGroup by weatherFilterViewModel.currentFilterGroup.collectAsStateWithLifecycle()

    val filterGroupId = if(currentFilterGroup != null) currentFilterGroup!!.id else -1

    WeatherFilterGroupsSelectionView(filterGroupId, true, weatherFilterViewModel::selectWeatherFilterGroup)
}