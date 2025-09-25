//package com.example.weatherbyagendaandroid.worker
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Context
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationManagerCompat
//import androidx.lifecycle.ProcessLifecycleOwner
//import androidx.work.CoroutineWorker
//import androidx.work.WorkerParameters
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//class AgendaItemWeatherCheckerWorker(private val appContext: Context, workerParams: WorkerParameters):
//    CoroutineWorker(appContext, workerParams) {
//
//    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
//        try {
//            // 1. Load agenda items (replace with DB or repository)
//            val agendaItems = loadAgendaItems()
//
//            for (item in agendaItems) {
//                if (!isForecastAvailable(item)) continue
//
//                // 2. Fetch weather data for item’s time window
//                val weatherData = fetchWeatherData(item)
//
//                // 3. Check if weather matches conditions
//                val matches = checkConditions(item, weatherData)
//
//                if (matches) {
//                    if (isAppInForeground()) {
//                        showInAppNotification(item)
//                    } else {
//                        showSystemNotification(
//                            "Weather Alert",
//                            "Conditions match for agenda item at ${item.startTime}"
//                        )
//                    }
//                }
//            }
//
//            Result.success()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Result.retry()
//        }
//    }
//
//    private fun loadAgendaItems(): List<AgendaItem> {
//        // TODO: Load from DB / preferences / repository
//        return emptyList()
//    }
//
//    private fun isForecastAvailable(item: AgendaItem): Boolean {
//        val now = System.currentTimeMillis()
//        val daysAhead = (item.startTime - now) / TimeUnit.DAYS.toMillis(1)
//        return daysAhead <= 7 // adjust if NOAA changes range
//    }
//
//    private suspend fun fetchWeatherData(item: AgendaItem): Any {
//        // TODO: Call api.weather.gov and parse response
//        return Any()
//    }
//
//    private fun checkConditions(item: AgendaItem, weatherData: Any): Boolean {
//        // TODO: Apply your condition logic
//        return false
//    }
//
//    private fun isAppInForeground(): Boolean {
//        val state = ProcessLifecycleOwner.get().lifecycle.currentState
//        return state.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)
//    }
//
//    private fun showInAppNotification(item: AgendaItem) {
//        // TODO: Post event via LiveData/Flow/EventBus
//        // Your Activity/Fragment observes and shows Snackbar/Dialog/etc.
//    }
//
//    private fun showSystemNotification(title: String, message: String) {
//        val channelId = "weather_channel"
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            val channel = NotificationChannel(
//                channelId,
//                "Weather Notifications",
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            val manager = appContext.getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(channel)
//        }
//
//        val notification = NotificationCompat.Builder(appContext, channelId)
//            .setContentTitle(title)
//            .setContentText(message)
//            .setSmallIcon(android.R.drawable.ic_dialog_info)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .build()
//
//        with(NotificationManagerCompat.from(appContext)) {
//            notify(System.currentTimeMillis().toInt(), notification)
//        }
//    }
//}