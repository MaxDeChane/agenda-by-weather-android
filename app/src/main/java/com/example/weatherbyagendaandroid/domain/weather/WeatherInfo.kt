package com.example.weatherbyagendaandroid.domain.weather

import com.example.weatherbyagendaandroid.dao.domain.WeatherPeriod
import com.example.weatherbyagendaandroid.dao.domain.WeatherProperties
import com.example.weatherbyagendaandroid.presentation.domain.WeatherPeriodDisplayBlock

data class WeatherInfo(val displayName: String, val gridId: String, val currentTimeWeatherPeriod: WeatherPeriod?,
                       val generalForecast: WeatherProperties?, val hourlyForecast: WeatherProperties?,
                       val weatherPeriodDisplayBlocks: List<WeatherPeriodDisplayBlock>) {

}
