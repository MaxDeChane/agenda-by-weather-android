package com.example.weatherbyagendaandroid.presentation.composable

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.helpers.LocationHelper
import com.example.weatherbyagendaandroid.presentation.model.PermissionsViewModel

@Composable
fun UpdateSettingsPermissionRequestView(permissionsViewModel: PermissionsViewModel = viewModel()) {
    val locationPermissionStatus by permissionsViewModel.systemLocationGranted.collectAsStateWithLifecycle()
    val resolutionIntent = (locationPermissionStatus as LocationHelper.LocationPermissionStatus.ResolvableError).intentSender
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Set the location permission status back to in progress to
            // Check and update any other permissions needed.
            permissionsViewModel.recheckLocationPermissions()
            // Trigger recomposition
            (context as? Activity)?.recreate()
        } else {
            // User canceled
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(resolutionIntent) {
        resolutionIntent.let {
            launcher.launch(IntentSenderRequest.Builder(it).build())
        }
    }
}

@Composable
fun UpdateUserPermissionRequestView(permissionsViewModel: PermissionsViewModel = viewModel()) {
    val locationPermissionStatus by permissionsViewModel.systemLocationGranted.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permission ->

        if (permission) {
            // Set the location permission status back to in progress to
            // Check and update any other permissions needed.
            permissionsViewModel.recheckLocationPermissions()
        } else {
            Toast.makeText(
                context,
                "Location permissions are required to use the app.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val permission = if(locationPermissionStatus is LocationHelper.LocationPermissionStatus.CoarseLocationDenied) {
        (locationPermissionStatus as LocationHelper.LocationPermissionStatus.CoarseLocationDenied).locationPermission
    } else {
        (locationPermissionStatus as LocationHelper.LocationPermissionStatus.BackgroundLocationDenied).locationPermission
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("This app needs your location to provide accurate weather updates.")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            permissionLauncher.launch(permission)
        }) {
            Text("Grant Permissions")
        }
    }
}
