package ir.plantcare.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CareLogDao {
    @Query("SELECT * FROM care_logs WHERE plantId = :plantId ORDER BY dateMillis DESC")
    fun getForPlant(plantId: Long): Flow<List<CareLog>>

    @Query("SELECT * FROM care_logs")
    suspend fun getAllOnce(): List<CareLog>

    @Query("SELECT * FROM care_logs WHERE plantId = :plantId AND type = :type ORDER BY dateMillis DESC LIMIT 1")
    suspend fun getLatestForType(plantId: Long, type: CareType): CareLog?

    @Insert
    suspend fun insert(log: CareLog): Long

    @Update
    suspend fun update(log: CareLog)

    @Delete
    suspend fun delete(log: CareLog)

    @Query("DELETE FROM care_logs")
    suspend fun deleteAll()
}
