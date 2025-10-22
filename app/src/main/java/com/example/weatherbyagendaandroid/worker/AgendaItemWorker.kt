package com.example.weatherbyagendaandroid.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherbyagendaandroid.MainActivity
import com.example.weatherbyagendaandroid.R
import com.example.weatherbyagendaandroid.dao.SavedAgendaItemsDao
import com.example.weatherbyagendaandroid.dao.SavedLocationsDao
import com.example.weatherbyagendaandroid.dao.WeatherFilterGroupsDao
import com.example.weatherbyagendaandroid.dao.WeatherRepository
import com.example.weatherbyagendaandroid.dao.domain.WeatherProperties
import com.example.weatherbyagendaandroid.domain.agenda.AgendaItem
import com.example.weatherbyagendaandroid.notification.NotificationInfo
import com.example.weatherbyagendaandroid.presentation.domain.SavedLocation
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

@HiltWorker
class AgendaItemWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val agendaItemsDao: SavedAgendaItemsDao,
    private val locationsDao: SavedLocationsDao,
    private val weatherFilerGroupsDao: WeatherFilterGroupsDao,
    private val weatherRepository: WeatherRepository
) :
    CoroutineWorker(context, workerParams) {

    companion object {
        val LOG_TAG = "AgendaItemWeatherCheckerWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.i(LOG_TAG, "Agenda item work started at ${LocalDateTime.now()}")
        try {
            // 1. Load agenda items (replace with DB or repository)
            val agendaItems = agendaItemsDao.retrieveAgendaItems(context)

            if(agendaItems == null || agendaItems.items.isEmpty()) {
                Log.i(LOG_TAG, "Null was returned from the agenda item dao. Assuming" +
                        " there are no agendaItems to be worked on.")
                return@withContext Result.success()
            }

            val inTimeItemsByLocationId = groupInTimeAgendaItemsByLocationId(agendaItems.items.values)

            if(inTimeItemsByLocationId.isEmpty()) {
                Log.i(LOG_TAG, "No agenda items in the time range where weather would be available yet." +
                        " Just returning successful")

                return@withContext Result.success()
            }

            val locations = locationsDao.retrieveLocations(context)

            if(locations == null) {
                Log.e(LOG_TAG, "Unable to load locations which is an error since if gotten" +
                        " this far locations should exist to match with the agendaItem.")

                return@withContext Result.retry()
            }

            val weatherFilterGroups = weatherFilerGroupsDao.retrieveWeatherFilterGroups(context)

            if(weatherFilterGroups == null) {
                Log.e(LOG_TAG, "Unable to load weather filter groups which is an error since if gotten" +
                        " this far filter groups should exist to match with the agendaItem.")

                return@withContext Result.retry()
            }


            val mutex = Mutex()
            val notificationInfos = mutableListOf<NotificationInfo>()
            coroutineScope {
                for(entry in inTimeItemsByLocationId) {
                    launch {
                        val location: SavedLocation = locations.retrieveLocation(entry.key)
                        val hourlyWeatherProperties = retrieveWeatherData(location)

                        for(agendaItem in entry.value) {
                            launch {
                                val hourlyPeriodsInTimeRange = hourlyWeatherProperties
                                    .retrievePeriodsInTimeRange(agendaItem.startTime, agendaItem.endTime)

                                if(hourlyPeriodsInTimeRange.isNotEmpty()) {
                                    if(agendaItem.weatherFilterGroupId != -1) {
                                        val weatherFilterGroup = weatherFilterGroups.retrieveWeatherFilterGroup(agendaItem.weatherFilterGroupId)
                                        if(weatherFilterGroup != null) {
                                            val firstMatchingWeatherPeriod = weatherFilterGroup.findFirstMatchingWeatherPeriod(hourlyPeriodsInTimeRange)

                                            if(firstMatchingWeatherPeriod != null) {
                                                val intent = Intent(context, MainActivity::class.java).apply {
                                                    putExtra("agendaItemId", agendaItem.id)
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                val notificationInfo = NotificationInfo(agendaItem.id, agendaItem.name, "${firstMatchingWeatherPeriod.startTime.toLocalDate()} ${firstMatchingWeatherPeriod.shortForecast}.",
                                                    PendingIntent.getActivity(context, agendaItem.id, intent, PendingIntent.FLAG_IMMUTABLE))
                                                mutex.withLock { notificationInfos.add(notificationInfo) }
                                            }
                                        } else {
                                            Log.e(LOG_TAG, "Weather filter group attached to the agenda item not found. This is a big problem!")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            showSystemNotifications(notificationInfos)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun groupInTimeAgendaItemsByLocationId(agendaItems: Collection<AgendaItem>): Map<Int, List<AgendaItem>> {
        return agendaItems.filter { isAgendaItemStartTime7DaysOrLessAway(it) }
            .groupBy { it.locationId }
    }

    private fun isAgendaItemStartTime7DaysOrLessAway(item: AgendaItem): Boolean {
        val now = System.currentTimeMillis()
        val startTimeInMillis = item.startTime.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli()
        val daysAhead = (startTimeInMillis - now) / TimeUnit.DAYS.toMillis(1)
        return daysAhead <= 7
    }

    private suspend fun retrieveWeatherData(savedLocation: SavedLocation): WeatherProperties {
        val gridPointsResponse = weatherRepository.retrieveGridPoints(savedLocation.latitude, savedLocation.longitude)
        return weatherRepository.retrieveWeatherProperties(gridPointsResponse.properties.forecastHourlyUrl)
    }

    private fun isAppInForeground(): Boolean {
        val state = ProcessLifecycleOwner.get().lifecycle.currentState
        return state.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)
    }

    private fun showInAppNotification(item: AgendaItem) {
        // TODO: Post event via LiveData/Flow/EventBus
        // Your Activity/Fragment observes and shows Snackbar/Dialog/etc.
    }

    private fun showSystemNotifications(notificationInfos: List<NotificationInfo>) {
        val channelId = "agenda_item_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(LOG_TAG, "Permission has not been granted for notifications. Store " +
                        " them for later.")

                // TODO: add code to show user the notification info on startup.
                return
            }
        }

        with(NotificationManagerCompat.from(context)) {
            for(notificationInfo in notificationInfos) {
                val notification = NotificationCompat.Builder(context, channelId)
                    .setContentTitle(notificationInfo.title)
                    .setContentText(notificationInfo.content)
                    .setSmallIcon(R.drawable.ic_application)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(notificationInfo.intent)
                    .setAutoCancel(true)
                    .build()
                notify(notificationInfo.id, notification)
            }
        }
    }
}