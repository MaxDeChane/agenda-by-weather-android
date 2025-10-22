package com.example.weatherbyagendaandroid.presentation.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbyagendaandroid.dao.SavedAgendaItemsDao
import com.example.weatherbyagendaandroid.domain.agenda.AgendaItem
import com.example.weatherbyagendaandroid.domain.agenda.AgendaItems
import com.example.weatherbyagendaandroid.enums.LoadingStatusEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedAgendaItemsDao: SavedAgendaItemsDao
): ViewModel() {

    private val _loadingStatus = MutableStateFlow(LoadingStatusEnum.LOADING)
    val loadingStatus = _loadingStatus.asStateFlow()
    private val _agendaItems = MutableStateFlow(AgendaItems())
    val agendaItems = _agendaItems.asStateFlow()

    init {
        viewModelScope.launch {
            val agendaItems =
                savedAgendaItemsDao.retrieveAgendaItems(context)

            if(agendaItems != null && agendaItems.hasItems()) {
                _agendaItems.value = agendaItems
            }

            _loadingStatus.value = LoadingStatusEnum.DONE
        }
    }

    fun addAgendaItem(agendaItem: AgendaItem) {
        _agendaItems.value = agendaItems.value.addAgendaItem(agendaItem)

        viewModelScope.launch {
            savedAgendaItemsDao.saveAgendaItems(context, _agendaItems.value)
        }
    }

    fun deleteAgendaItem(id: Int) {
        _agendaItems.value = agendaItems.value.deleteAgendaItem(id)

        viewModelScope.launch {
            savedAgendaItemsDao.saveAgendaItems(context, _agendaItems.value)
        }
    }
}