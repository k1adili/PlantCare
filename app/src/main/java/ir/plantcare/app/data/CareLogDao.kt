package ir.plantcare.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CareLogDao {
    @Query("SELECT * FROM care_logs WHERE plantId = :plantId ORDER BY dateMillis DESC")
    fun getForPlant(plantId: Long): Flow<List<CareLog>>

    @Query("SELECT * FROM care_logs")
    suspend fun getAllOnce(): List<CareLog>

    @Insert
    suspend fun insert(log: CareLog): Long

    @Delete
    suspend fun delete(log: CareLog)

    @Query("DELETE FROM care_logs")
    suspend fun deleteAll()
}
