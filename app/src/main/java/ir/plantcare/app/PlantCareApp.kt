package ir.plantcare.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ir.plantcare.app.data.AppDatabase
import ir.plantcare.app.util.NotificationHelper
import ir.plantcare.app.util.ReminderWorker
import java.util.concurrent.TimeUnit

class PlantCareApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        scheduleReminderWorker()
    }

    private fun scheduleReminderWorker() {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(12, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "plant_reminder_check",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
