package com.example.weatherbyagendaandroid.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.weatherbyagendaandroid.dao.entites.City

@Dao
interface CityDao {
    @Query("SELECT * FROM us_cities WHERE name = :cityName AND state = :state LIMIT 1")
    suspend fun searchCityState(cityName: String, state: String): City?

    // Search by city name (multiple results for ambiguous names like Springfield)
    @Query("SELECT * FROM us_cities WHERE name LIKE :cityName || '%'")
    suspend fun searchCity(cityName: String): List<City>
}