package com.example.weatherbyagendaandroid

import WeatherByAgendaAndroidTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.helpers.LocationHelper
import com.example.weatherbyagendaandroid.presentation.composable.UpdateSettingsPermissionRequestView
import com.example.weatherbyagendaandroid.presentation.composable.UpdateUserPermissionRequestView
import com.example.weatherbyagendaandroid.presentation.composable.WeatherGeneralHourlyPeriodsView
import com.example.weatherbyagendaandroid.presentation.composable.WeatherTopBar
import com.example.weatherbyagendaandroid.presentation.composable.menu.MenuView
import com.example.weatherbyagendaandroid.presentation.model.MenuViewModel
import com.example.weatherbyagendaandroid.presentation.model.PermissionsViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherScreen()
        }
    }
}

@Composable
fun WeatherScreen(menuViewModel: MenuViewModel = viewModel(),
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

                        WeatherGeneralHourlyPeriodsView(innerPadding)

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