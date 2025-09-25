package com.example.weatherbyagendaandroid.domain.agenda

import java.time.LocalDateTime

data class AgendaItem(val name: String, val startTime: LocalDateTime, val endTime: LocalDateTime,
                      val locationName: String, val weatherFilterGroupName: String)
