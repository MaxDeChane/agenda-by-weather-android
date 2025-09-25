package com.example.weatherbyagendaandroid

import WeatherByAgendaAndroidTheme
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.helpers.LocationHelper
import com.example.weatherbyagendaandroid.presentation.composable.UpdateSettingsPermissionRequestView
import com.example.weatherbyagendaandroid.presentation.composable.UpdateUserPermissionRequestView
import com.example.weatherbyagendaandroid.presentation.composable.WeatherGeneralHourlyPeriodsView
import com.example.weatherbyagendaandroid.presentation.composable.WeatherTopBar
import com.example.weatherbyagendaandroid.presentation.composable.dialog.NotificationPermissionAlertView
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

    var showNotificationPermissionDialog by remember { mutableStateOf(true)}

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

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && showNotificationPermissionDialog) {
                            val context = LocalContext.current
                            val permission = android.Manifest.permission.POST_NOTIFICATIONS

                            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)) {
                                    if(!permissionsViewModel.dontShowRationalFlow.collectAsStateWithLifecycle(true).value) {
                                        NotificationPermissionAlertView({dontShowAgain ->
                                            if(dontShowAgain) {
                                                permissionsViewModel.updateDontShowRationalFlow(dontShowAgain)
                                            }
                                            showNotificationPermissionDialog = false
                                        }) {
                                            ActivityCompat.requestPermissions(context, arrayOf(permission), 0)
                                            showNotificationPermissionDialog = false
                                        }
                                    }
                                } else {
                                    ActivityCompat.requestPermissions(context, arrayOf(permission), 0)
                                }
                            }
                        }

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