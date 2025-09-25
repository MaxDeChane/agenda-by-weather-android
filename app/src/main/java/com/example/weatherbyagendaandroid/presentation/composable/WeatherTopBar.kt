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
import androidx.compose.runtime.collectAsState
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
import com.example.weatherbyagendaandroid.presentation.model.WeatherViewModel
import dagger.hilt.android.qualifiers.ApplicationContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherTopBar(
    weatherViewModel: WeatherViewModel = viewModel(),
    menuViewModel: MenuViewModel = viewModel()
) {
    val weatherPeriod by weatherViewModel.currentTimeWeatherPeriod.collectAsStateWithLifecycle()
    val weatherGridPoints by weatherViewModel.weatherGridPoints.collectAsStateWithLifecycle()

    // these will be resolved before this composable is loaded
    val relativeLocation = weatherGridPoints?.properties?.relativeLocation?.properties
    val icon: Int = if(weatherPeriod != null) weatherPeriod!!.icon else R.drawable.ic_broken_image

    val context = LocalContext.current

    TopAppBar(
        title = {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (relativeLocation == null) "Loading" else "${relativeLocation.city}, ${relativeLocation.state}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (weatherPeriod == null) {
                    Text(
                        text = "Loading",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Normal
                        ),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = weatherPeriod?.shortForecast,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(end = 4.dp)
                        )
                        Text(
                            text = "${weatherPeriod?.temperature} - ${weatherPeriod?.shortForecast}",
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