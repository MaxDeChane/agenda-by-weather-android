package com.example.weatherbyagendaandroid.dao

import com.example.weatherbyagendaandroid.dao.domain.GridPointsResponse
import com.example.weatherbyagendaandroid.dao.domain.WeatherProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(private val weatherApiDao: WeatherApiDao) {

    suspend fun retrieveGridPoints(latitude: Double, longitude: Double): GridPointsResponse = withContext(
        Dispatchers.IO) {
        weatherApiDao.getGridPoints(latitude, longitude)
    }

    suspend fun retrieveWeatherProperties(url: String): WeatherProperties = withContext(Dispatchers.IO) {
        val weatherApiResponse = weatherApiDao.getForecast(url)

        weatherApiResponse.properties
    }
}