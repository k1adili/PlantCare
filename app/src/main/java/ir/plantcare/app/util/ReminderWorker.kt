package ir.plantcare.app.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ir.plantcare.app.data.AppDatabase

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val plants = db.plantDao().getAllOnce()
        val now = System.currentTimeMillis()

        plants.forEach { plant ->
            val nextWatering = plant.nextWateringMillis()
            val nextFertilizing = plant.nextFertilizingMillis()

            if (nextWatering != null && nextWatering <= now) {
                NotificationHelper.notify(
                    applicationContext,
                    id = (plant.id * 10 + 1).toInt(),
                    title = "وقت آبیاری ${plant.name} است 🌱",
                    text = "چند روزیه به ${plant.name} آب نداده‌اید."
                )
            }
            if (nextFertilizing != null && nextFertilizing <= now) {
                NotificationHelper.notify(
                    applicationContext,
                    id = (plant.id * 10 + 2).toInt(),
                    title = "وقت کوددهی ${plant.name} است 🌿",
                    text = "زمان کوددهی بعدی ${plant.name} فرا رسیده."
                )
            }
        }
        return Result.success()
    }
}
