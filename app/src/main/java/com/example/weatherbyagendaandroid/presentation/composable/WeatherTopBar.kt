package com.example.weatherbyagendaandroid.presentation.composable

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbyagendaandroid.MapActivity
import com.example.weatherbyagendaandroid.R
import com.example.weatherbyagendaandroid.presentation.model.MenuViewModel
import com.example.weatherbyagendaandroid.presentation.model.WeatherDataStateEnum
import com.example.weatherbyagendaandroid.presentation.model.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherTopBar(
    weatherViewModel: WeatherViewModel = viewModel(),
    menuViewModel: MenuViewModel = viewModel()
) {
    // these will be resolved before this composable is loaded
    val weatherLoadingStatus by weatherViewModel.weatherLoadingState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    TopAppBar(
        title = {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                if(weatherLoadingStatus == WeatherDataStateEnum.LOADING) {
                    Text(
                        text = "Loading",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    val cityState = weatherViewModel.weatherInfo?.displayName
                    val currentTimeWeatherPeriod = weatherViewModel.weatherInfo?.currentTimeWeatherPeriod
                    val icon: Int = currentTimeWeatherPeriod?.icon ?: R.drawable.ic_broken_image

                    Text(
                        text = cityState ?: "Error",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = currentTimeWeatherPeriod?.shortForecast ?: "Error Loading",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(end = 4.dp)
                        )
                        Text(
                            text = if(currentTimeWeatherPeriod != null) "${currentTimeWeatherPeriod.temperature} - ${currentTimeWeatherPeriod.shortForecast}"
                                else "Error Loading",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        actions = {
                Text(
                    text = "Map",
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable {
                            context.startActivity(Intent(context, MapActivity::class.java))
                        }
                )
            IconButton(onClick = menuViewModel::menuButtonClicked) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .drawBehind {
                val strokeWidth = 4.dp.toPx()
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            }
            .fillMaxWidth()
    )
}