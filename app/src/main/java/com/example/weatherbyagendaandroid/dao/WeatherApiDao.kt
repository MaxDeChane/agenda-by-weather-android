package com.example.weatherbyagendaandroid.dao

import com.example.weatherbyagendaandroid.dao.domain.GridPointsResponse
import com.example.weatherbyagendaandroid.dao.domain.WeatherApiResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface WeatherApiDao {
    @GET("points/{latitude},{longitude}")
    suspend fun getGridPoints(@Path("latitude") latitude: Double, @Path("longitude") longitude: Double): GridPointsResponse

    @GET
    suspend fun getForecast(@Url url: String): WeatherApiResponse
}