package com.example.weatherbyagendaandroid.dao

import android.content.Context
import com.example.weatherbyagendaandroid.domain.agenda.AgendaItems
import com.example.weatherbyagendaandroid.presentation.domain.SavedLocations
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedAgendaItemsDao @Inject constructor(moshi: Moshi) {

    private val jsonAdapter = moshi.adapter(AgendaItems::class.java)

    suspend fun retrieveAgendaItems(context: Context): AgendaItems? = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "agendaItems")
        if(file.exists()) {
            jsonAdapter.fromJson(file.readText())
        } else {
            null
        }
    }

    suspend fun saveAgendaItems(context: Context, agendaItems: AgendaItems) = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "agendaItems")
        file.writeText(jsonAdapter.toJson(agendaItems))
    }
}