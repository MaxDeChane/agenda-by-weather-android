package com.example.weatherbyagendaandroid.dao.entites

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "us_cities",
    indices = [
        Index(value = ["name"], name = "idx_city_name"),
        Index(value = ["state"], name = "idx_city_state")
    ])
data class City(
    @PrimaryKey
    val geonameid: Int?,
    val name: String?,
    val asciiname: String?,
    val latitude: Double?,
    val longitude: Double?,
    val state: String?,
    val population: Int?
)
