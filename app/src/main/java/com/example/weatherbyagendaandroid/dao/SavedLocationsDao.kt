package com.example.weatherbyagendaandroid.dao

import android.content.Context
import com.example.weatherbyagendaandroid.presentation.domain.SavedLocations
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedLocationsDao @Inject constructor(private val moshi: Moshi) {

    private val jsonAdapter = moshi.adapter(SavedLocations::class.java)

    suspend fun retrieveLocations(context: Context): SavedLocations? = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "savedLocations")
        if(file.exists()) {
            jsonAdapter.fromJson(file.readText())
        } else {
            null
        }
    }

    suspend fun saveLocations(context: Context, savedLocations: SavedLocations) = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "savedLocations")
        file.writeText(jsonAdapter.toJson(savedLocations))
    }
}