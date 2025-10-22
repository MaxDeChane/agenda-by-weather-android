package com.example.weatherbyagendaandroid.notification

import android.app.PendingIntent

data class NotificationInfo(val id: Int, val title: String, val content: String, val intent: PendingIntent? = null) {

}
