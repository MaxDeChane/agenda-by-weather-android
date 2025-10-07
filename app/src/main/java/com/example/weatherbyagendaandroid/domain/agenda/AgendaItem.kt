package com.example.weatherbyagendaandroid.domain.agenda

import java.time.LocalDateTime

data class AgendaItem(val id: Int = -1, val name: String, val startTime: LocalDateTime, val endTime: LocalDateTime,
                      val locationId: Int, val weatherFilterGroupId: Int)
