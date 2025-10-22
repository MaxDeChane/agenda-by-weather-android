package com.example.weatherbyagendaandroid

import WeatherByAgendaAndroidTheme
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.enums.LoadingStatusEnum
import com.example.weatherbyagendaandroid.helpers.LocationHelper
import com.example.weatherbyagendaandroid.presentation.composable.UpdateSettingsPermissionRequestView
import com.example.weatherbyagendaandroid.presentation.composable.UpdateUserPermissionRequestView
import com.example.weatherbyagendaandroid.presentation.composable.WeatherGeneralHourlyPeriodsView
import com.example.weatherbyagendaandroid.presentation.composable.WeatherTopBar
import com.example.weatherbyagendaandroid.presentation.composable.menu.MenuView
import com.example.weatherbyagendaandroid.notification.createFromChannelId
import com.example.weatherbyagendaandroid.presentation.model.AgendaViewModel
import com.example.weatherbyagendaandroid.presentation.model.LocationViewModel
import com.example.weatherbyagendaandroid.presentation.model.MenuViewModel
import com.example.weatherbyagendaandroid.presentation.model.PermissionsViewModel
import com.example.weatherbyagendaandroid.presentation.model.WeatherFilterViewModel
import com.example.weatherbyagendaandroid.presentation.model.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        createFromChannelId(this, "agenda_item_notifications")
        super.onCreate(savedInstanceState)
        val agendaItemId = intent.getIntExtra("agendaItemId", -1)
        enableEdgeToEdge()
        setContent {
            if(agendaItemId == -1) {
                WeatherScreen()
            } else {
                SetupViewModelsFromIntent(agendaItemId)
            }

        }
    }
}

@Composable
fun WeatherScreen(startedFromIntent: Boolean = false, menuViewModel: MenuViewModel = viewModel(),
                  permissionsViewModel: PermissionsViewModel = viewModel()) {
    val locationPermissionStatus by permissionsViewModel.systemLocationGranted.collectAsStateWithLifecycle()

    when (locationPermissionStatus) {
        LocationHelper.LocationPermissionStatus.PermissionCheckInProgress -> Text("Loading")
        LocationHelper.LocationPermissionStatus.AllPermissionsGranted -> {
            WeatherByAgendaAndroidTheme {
                Scaffold(
                    modifier = Modifier.padding(WindowInsets.safeDrawing.asPaddingValues()),
                    topBar = { WeatherTopBar() }
                ) { innerPadding ->
                    val showMenu by menuViewModel.showMenu.collectAsState()

                    Box(Modifier.fillMaxSize()) {

                        WeatherGeneralHourlyPeriodsView(innerPadding, startedFromIntent)

                        if (showMenu) {
                            MenuView(innerPadding)
                        }
                    }
                }
            }
        }
        is LocationHelper.LocationPermissionStatus.BackgroundLocationDenied -> UpdateUserPermissionRequestView()
        is LocationHelper.LocationPermissionStatus.CoarseLocationDenied -> UpdateUserPermissionRequestView()
        is LocationHelper.LocationPermissionStatus.ResolvableError -> UpdateSettingsPermissionRequestView()
    }
}

@Composable
fun SetupViewModelsFromIntent(agendaItemId: Int, agendaViewModel: AgendaViewModel = viewModel(),
                              locationViewModel: LocationViewModel = viewModel(),
                              weatherFilterViewModel: WeatherFilterViewModel = viewModel(),
                              weatherViewModel: WeatherViewModel = viewModel()) {
    var loadingStatus by remember { mutableStateOf(LoadingStatusEnum.LOADING) }

    val agendaItems by agendaViewModel.agendaItems.collectAsStateWithLifecycle()
    val agendaItemsLoadingStatus by agendaViewModel.loadingStatus.collectAsStateWithLifecycle()
    val savedLocations by locationViewModel.savedLocations.collectAsStateWithLifecycle()
    val savedLocationsLoadingStatus by locationViewModel.loadingStatus.collectAsStateWithLifecycle()
    val weatherFilterGroupsLoadingStatus by weatherFilterViewModel.loadingStatus.collectAsStateWithLifecycle()

    LaunchedEffect(agendaItemsLoadingStatus.name + savedLocationsLoadingStatus.name + agendaItemId) {
        if(agendaItemId != -1 && agendaItemsLoadingStatus == LoadingStatusEnum.DONE &&
            savedLocationsLoadingStatus == LoadingStatusEnum.DONE && weatherFilterGroupsLoadingStatus == LoadingStatusEnum.DONE) {

            val agendaItem = agendaItems.items[agendaItemId]
            if(agendaItem == null) {
                Log.e("SetupViewModelsFromIntent", "Unable to retrieve agenda item with id $agendaItemId." +
                        " This should not happen and needs to be investigated.")

                return@LaunchedEffect
            }

            if(agendaItem.locationId != -1) {
                val savedLocation = savedLocations.locations[agendaItem.locationId]

                if(savedLocation == null) {

                } else {
                    // Set the selected location so it shows up if menu is opened and
                    // update the weather view model.
                    locationViewModel.selectLocation(agendaItem.locationId)
                    weatherViewModel.updateWeatherInfo(
                        savedLocation.latitude,
                        savedLocation.longitude
                    ).invokeOnCompletion {
                        weatherFilterViewModel.selectWeatherFilterGroup(agendaItem.weatherFilterGroupId)
                        loadingStatus = LoadingStatusEnum.DONE
                    }
                }
            }
        }
    }

    if(loadingStatus == LoadingStatusEnum.LOADING) {
        Box(
            modifier = Modifier
                .fillMaxSize() // take up the entire available space
                .background(MaterialTheme.colorScheme.primary) // your background color
        ) {
            Text("Loading Weather Data...", Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.displayLarge)
        }
    } else if(loadingStatus == LoadingStatusEnum.DONE) {
        WeatherScreen(true)
    }
}