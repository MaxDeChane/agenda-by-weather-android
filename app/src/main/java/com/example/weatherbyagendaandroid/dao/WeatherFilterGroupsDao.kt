package com.example.weatherbyagendaandroid.dao

import android.content.Context
import com.example.weatherbyagendaandroid.presentation.domain.WeatherFilterGroups
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherFilterGroupsDao @Inject constructor(private val moshi: Moshi) {

    private val jsonAdapter = moshi.adapter(WeatherFilterGroups::class.java)

    suspend fun retrieveWeatherFilterGroups(context: Context): WeatherFilterGroups? = withContext(
        Dispatchers.IO) {
        val file = File(context.filesDir, "weatherFilterGroups")
        if(file.exists()) {
            jsonAdapter.fromJson(file.readText())
        } else {
            null
        }
    }

    suspend fun saveWeatherFilterGroups(context: Context, weatherFilterGroups: WeatherFilterGroups) = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "weatherFilterGroups")
        file.writeText(jsonAdapter.toJson(weatherFilterGroups))
    }
}