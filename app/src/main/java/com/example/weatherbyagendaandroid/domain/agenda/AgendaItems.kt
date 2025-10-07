package com.example.weatherbyagendaandroid.domain.agenda

import android.util.Log
import kotlin.random.Random

data class AgendaItems(val items: Map<Int, AgendaItem> = mapOf()) {

    private companion object {
        const val LOG_TAG = "AgendaItems"
    }

    fun hasItems(): Boolean {
        return items.isNotEmpty()
    }

    fun containsAgendaItem(id: Int): Boolean {
        return items.containsKey(id)
    }

    fun addAgendaItem(agendaItem: AgendaItem): AgendaItems {
        val mutableAgendaItems = items.toMutableMap()
        var agendaItemId = agendaItem.id

        if(agendaItemId == -1) {
            agendaItemId = Random.nextInt(0, 10000)

            while (mutableAgendaItems.contains(agendaItemId)) {
                agendaItemId = Random.nextInt(0, 10000)
            }
        }

        mutableAgendaItems[agendaItemId] = agendaItem.copy(id = agendaItemId)

        return this.copy(items = mutableAgendaItems)
    }

    fun deleteAgendaItem(id: Int): AgendaItems {
        if(items.containsKey(id)) {
            val mutableItems = items.toMutableMap()
            mutableItems.remove(id)

            return this.copy(items = mutableItems)
        }

        Log.e(LOG_TAG, "$id does not exist. This should not happen")
        return this
    }
}
