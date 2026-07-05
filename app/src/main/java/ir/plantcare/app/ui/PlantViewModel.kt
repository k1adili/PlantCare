package ir.plantcare.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.plantcare.app.data.AppDatabase
import ir.plantcare.app.data.CareLog
import ir.plantcare.app.data.CareType
import ir.plantcare.app.data.Plant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PlantViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val plantDao = db.plantDao()
    private val careLogDao = db.careLogDao()

    val plants: Flow<List<Plant>> = plantDao.getAll()

    fun plantById(id: Long): Flow<Plant?> = plantDao.getById(id)
    fun logsForPlant(plantId: Long): Flow<List<CareLog>> = careLogDao.getForPlant(plantId)

    fun addOrUpdatePlant(plant: Plant, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = if (plant.id == 0L) plantDao.insert(plant) else {
                plantDao.update(plant); plant.id
            }
            onDone(id)
        }
    }

    fun deletePlant(plant: Plant) {
        viewModelScope.launch {
            ir.plantcare.app.util.ImageUtils.deleteImage(getApplication(), plant.photoFileName)
            plantDao.delete(plant)
        }
    }

    fun logCare(plant: Plant, type: CareType, dateMillis: Long, note: String = "") {
        viewModelScope.launch {
            careLogDao.insert(CareLog(plantId = plant.id, type = type, dateMillis = dateMillis, note = note))
            refreshLastDates(plant)
        }
    }

    fun updateLog(plant: Plant, log: CareLog, type: CareType, dateMillis: Long, note: String) {
        viewModelScope.launch {
            careLogDao.update(log.copy(type = type, dateMillis = dateMillis, note = note))
            refreshLastDates(plant)
        }
    }

    fun deleteLog(plant: Plant, log: CareLog) {
        viewModelScope.launch {
            careLogDao.delete(log)
            refreshLastDates(plant)
        }
    }

    /** بعد از ثبت/ویرایش/حذف یک رویداد، تاریخ آخرین آبیاری و کوددهی گیاه را
     * بر اساس جدیدترین رویداد ثبت‌شده از هر نوع، دوباره محاسبه می‌کند. */
    private suspend fun refreshLastDates(plant: Plant) {
        val latestWatering = careLogDao.getLatestForType(plant.id, CareType.WATERING)?.dateMillis
        val latestFertilizing = careLogDao.getLatestForType(plant.id, CareType.FERTILIZING)?.dateMillis
        val current = plantDao.getByIdOnce(plant.id) ?: plant
        val updated = current.copy(
            lastWateringMillis = latestWatering,
            lastFertilizingMillis = latestFertilizing
        )
        if (updated != current) plantDao.update(updated)
    }

    fun exportBackup(destUri: Uri, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                ir.plantcare.app.util.BackupManager.exportBackup(getApplication(), destUri)
                onDone(true, null)
            } catch (e: Exception) {
                onDone(false, e.message)
            }
        }
    }

    fun importBackup(sourceUri: Uri, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                ir.plantcare.app.util.BackupManager.importBackup(getApplication(), sourceUri)
                onDone(true, null)
            } catch (e: Exception) {
                onDone(false, e.message)
            }
        }
    }
}
