package com.example.weatherbyagendaandroid.presentation.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherbyagendaandroid.dao.WeatherFilterGroupsDao
import com.example.weatherbyagendaandroid.enums.LoadingStatusEnum
import com.example.weatherbyagendaandroid.presentation.domain.WeatherFilter
import com.example.weatherbyagendaandroid.presentation.domain.WeatherFilterGroup
import com.example.weatherbyagendaandroid.presentation.domain.WeatherFilterGroupEditHolder
import com.example.weatherbyagendaandroid.presentation.domain.WeatherFilterGroups
import com.example.weatherbyagendaandroid.presentation.domain.WeatherPeriodDisplayBlock
import com.example.weatherbyagendaandroid.presentation.model.WeatherViewModel.Companion.LOG_TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class FilterStatus {
    IN_PROGRESS,
    DONE
}

@HiltViewModel
class WeatherFilterViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val weatherFilterGroupsDao: WeatherFilterGroupsDao): ViewModel() {

    private val _loadingStatus = MutableStateFlow(LoadingStatusEnum.LOADING)
    val loadingStatus = _loadingStatus.asStateFlow()

    private val _filterStatus = MutableStateFlow(FilterStatus.DONE)
    val filterStatus = _filterStatus.asStateFlow()

    private val _weatherFilterGroups = MutableStateFlow(WeatherFilterGroups())
    val weatherFilterGroups = _weatherFilterGroups.asStateFlow()

    private val _selectedFilterGroup = MutableStateFlow<WeatherFilterGroup?>(null)
    val selectedFilterGroup = _selectedFilterGroup.asStateFlow()

    // Holds the current filter group being created.
    private val _inCreationFilterGroup = MutableStateFlow(WeatherFilterGroup())
    val inCreationFilterGroup = _inCreationFilterGroup.asStateFlow()

    private val _inEditFilterGroupHolders = MutableStateFlow<Map<Int, WeatherFilterGroupEditHolder>>(mapOf())
    val inEditFilterGroupHolders = _inEditFilterGroupHolders.asStateFlow()

    init {
        viewModelScope.launch {
            val loadedWeatherFilterGroups = weatherFilterGroupsDao.retrieveWeatherFilterGroups(context)

            if(loadedWeatherFilterGroups != null && loadedWeatherFilterGroups.filterGroups.isNotEmpty()) {
                _weatherFilterGroups.value = loadedWeatherFilterGroups
            }

            _loadingStatus.value = LoadingStatusEnum.DONE
        }
    }

    fun hasSelectedWeatherFilterGroup(): Boolean {
        return selectedFilterGroup.value != null
    }

    fun selectWeatherFilterGroup(filterGroupId: Int) {
        if(selectedFilterGroup.value == null ||
            selectedFilterGroup.value!!.id != filterGroupId) {
            if(inCreationFilterGroup.value.hasFilters()) {
                _inCreationFilterGroup.value = WeatherFilterGroup()
            }
            _selectedFilterGroup.value = weatherFilterGroups.value.retrieveWeatherFilterGroup(filterGroupId)
            _filterStatus.value = FilterStatus.IN_PROGRESS
        } else {
            _selectedFilterGroup.value = null
            _filterStatus.value = FilterStatus.IN_PROGRESS
        }
    }

    // The saving is done by taking the filter in creation and moving it to the
    // collection of all weather filter groups. Returns the Id of the new weather
    // filter group.
    fun saveNewWeatherFilterGroup(filterGroupName: String): Int {
        val updateWeatherFilterGroups = weatherFilterGroups.value.saveNewWeatherFilterGroup(filterGroupName, inCreationFilterGroup.value)
        _weatherFilterGroups.value = updateWeatherFilterGroups
        _inCreationFilterGroup.value = WeatherFilterGroup()

        viewModelScope.launch {
            weatherFilterGroupsDao.saveWeatherFilterGroups(context, updateWeatherFilterGroups)
        }

        return updateWeatherFilterGroups.filterGroups.values.first { it.name == filterGroupName }.id
    }

    // Call this before editing. It will move things into the right properties
    // for editing.
    fun setupWeatherFilterGroupForEditing(originalFilterGroupId: Int) {
        if(!inEditFilterGroupHolders.value.contains(originalFilterGroupId)) {
            val weatherFilterGroupToEdit = weatherFilterGroups.value.filterGroups[originalFilterGroupId]
            val inEditFilterGroupsCopy = inEditFilterGroupHolders.value.toMutableMap()
            inEditFilterGroupsCopy[originalFilterGroupId] =
                WeatherFilterGroupEditHolder(weatherFilterGroupToEdit!!.copy(), weatherFilterGroupToEdit)
            _inEditFilterGroupHolders.value = inEditFilterGroupsCopy.toMap()
        }
    }

    fun removeWeatherFilterGroupFromEditing(filterGroupId: Int) {
        val inEditFilterGroupsCopy = inEditFilterGroupHolders.value.toMutableMap()
        inEditFilterGroupsCopy.remove(filterGroupId)
        _inEditFilterGroupHolders.value = inEditFilterGroupsCopy
    }

    fun updateWeatherFilterGroup(filterGroupId: Int) {
        if(!inEditFilterGroupHolders.value.containsKey(filterGroupId)) {
            Log.e(
                LOG_TAG, "Updated filter group not found by id $filterGroupId " +
                    "This should not happen and needs to be investigated.")
            return
        }

        val inEditFilterGroupHolderCopy = inEditFilterGroupHolders.value.toMutableMap()
        val updatedFilterGroup = inEditFilterGroupHolderCopy.remove(filterGroupId)!!.weatherFilterGroupToEdit

        val updatedFilterGroups = weatherFilterGroups.value.updateWeatherFilterGroup(filterGroupId, updatedFilterGroup)

        _weatherFilterGroups.value = updatedFilterGroups
        _inEditFilterGroupHolders.value = inEditFilterGroupHolderCopy

        viewModelScope.launch {
            weatherFilterGroupsDao.saveWeatherFilterGroups(context, updatedFilterGroups)
        }
    }

    fun deleteWeatherFilterGroup(locationId: Int) {
        _weatherFilterGroups.value = weatherFilterGroups.value.deleteWeatherFilterGroup(locationId)
        if(selectedFilterGroup.value?.id == locationId) {
            _selectedFilterGroup.value = null
        }

        viewModelScope.launch {
            weatherFilterGroupsDao.saveWeatherFilterGroups(context, _weatherFilterGroups.value)
        }
    }

    fun setWeatherFilter(filterClassName: String, weatherFilter: WeatherFilter, filterGroupId: Int) {
        if(filterGroupId != -1) {
            val inEditFilterGroupHoldersCopy = inEditFilterGroupHolders.value.toMutableMap()
            val filterGroupHolderToUpdate = inEditFilterGroupHoldersCopy[filterGroupId]
            val filterGroupUpdated = filterGroupHolderToUpdate!!.weatherFilterGroupToEdit.addWeatherFilter(filterClassName, weatherFilter)
            inEditFilterGroupHoldersCopy[filterGroupId] = filterGroupHolderToUpdate.copy(weatherFilterGroupToEdit = filterGroupUpdated)
            _inEditFilterGroupHolders.value = inEditFilterGroupHoldersCopy
        } else {
            _inCreationFilterGroup.value =
                inCreationFilterGroup.value.addWeatherFilter(filterClassName, weatherFilter)
            _filterStatus.value = FilterStatus.IN_PROGRESS
        }
    }

    fun removeWeatherFilter(filterClassName: String, filterGroupId: Int) {
        if(filterGroupId != -1) {
            val inEditFilterGroupHoldersCopy = inEditFilterGroupHolders.value.toMutableMap()
            val filterGroupHolderToUpdate = inEditFilterGroupHoldersCopy[filterGroupId]
            val filterGroupUpdated = filterGroupHolderToUpdate!!.weatherFilterGroupToEdit.removeWeatherFilter(filterClassName)
            inEditFilterGroupHoldersCopy[filterGroupId] = filterGroupHolderToUpdate.copy(weatherFilterGroupToEdit = filterGroupUpdated)
            _inEditFilterGroupHolders.value = inEditFilterGroupHoldersCopy
        } else {
            _inCreationFilterGroup.value =
                inCreationFilterGroup.value.removeWeatherFilter(filterClassName)
            _filterStatus.value = FilterStatus.IN_PROGRESS
        }
    }

    fun clearInCreationOrEditWeatherFilterGroup(filterGroupId: Int) {
        if(filterGroupId != -1) {
            val inEditFilterGroupHoldersCopy = inEditFilterGroupHolders.value.toMutableMap()
            val filterGroupHolderToUpdate = inEditFilterGroupHoldersCopy[filterGroupId]
            inEditFilterGroupHoldersCopy[filterGroupId] = filterGroupHolderToUpdate!!
                .copy(weatherFilterGroupToEdit = WeatherFilterGroup(filterGroupId))
            _inEditFilterGroupHolders.value = inEditFilterGroupHoldersCopy
        } else {
            _inCreationFilterGroup.value = WeatherFilterGroup()
        }
    }

    fun clearSelectedWeatherFilterGroup() {
        if(selectedFilterGroup.value != null) {
            this.selectWeatherFilterGroup(selectedFilterGroup.value!!.id)
        }
    }

    suspend fun runWeatherDisplayBlockThroughFilters(weatherPeriodDisplayBlocks: List<WeatherPeriodDisplayBlock>) =
        withContext(Dispatchers.Default) {
            val currentFilterGroup = if(selectedFilterGroup.value == null) inCreationFilterGroup.value else selectedFilterGroup.value

            // Add scope here to make sure all the child coroutines finish before we continue.
            coroutineScope {
                for (weatherPeriodDisplayBlock in weatherPeriodDisplayBlocks) {
                    launch {
                        weatherPeriodDisplayBlock.resetFiltered()
                        currentFilterGroup?.runWeatherDisplayBlockThroughFilters(
                            weatherPeriodDisplayBlock
                        )
                    }
                }
            }

            _filterStatus.value = FilterStatus.DONE
        }

    fun setWeatherFilterStatusToInProgress() {
        _filterStatus.value = FilterStatus.IN_PROGRESS
    }
}