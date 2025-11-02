package com.example.weatherbyagendaandroid.presentation.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbyagendaandroid.dao.repository.MenuOptionsRepository
import com.example.weatherbyagendaandroid.dao.repository.SelectedMenuOptionsRepository
import com.example.weatherbyagendaandroid.domain.agenda.AgendaItem
import com.example.weatherbyagendaandroid.enums.LoadingStatusEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val menuOptionsRepository: MenuOptionsRepository,
    private val selectedMenuOptionsRepository: SelectedMenuOptionsRepository
): ViewModel() {

    val loadingStatus = menuOptionsRepository.agendaItemsLoadingStatus
    val selectedAgendaItem = selectedMenuOptionsRepository.selectedAgendaItem
    val agendaItems = menuOptionsRepository.agendaItems

    init {
        viewModelScope.launch {
            // Only reload if not already loaded.
            if(loadingStatus.value != LoadingStatusEnum.DONE) {
                menuOptionsRepository.loadAgendaItems(context)
            }
        }
    }

    fun addAgendaItem(agendaItem: AgendaItem) {
        viewModelScope.launch {
            menuOptionsRepository.addAgendaItem(agendaItem, context)
        }
    }

    fun deleteAgendaItem(id: Int) {
        viewModelScope.launch {
            menuOptionsRepository.deleteAgendaItem(id, context)
        }
    }

    fun selectAgendaItem(agendaItem: AgendaItem) {
        if (selectedAgendaItem.value == null ||
            selectedAgendaItem.value!!.id != agendaItem.id
        ) {
            selectedMenuOptionsRepository.setSelectedAgendaItem(agendaItem)
        } else {
            selectedMenuOptionsRepository.setSelectedAgendaItem(null)
        }
    }
}