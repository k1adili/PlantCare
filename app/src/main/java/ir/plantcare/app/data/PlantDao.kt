package ir.plantcare.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Query("SELECT * FROM plants ORDER BY name ASC")
    fun getAll(): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE id = :id")
    fun getById(id: Long): Flow<Plant?>

    @Query("SELECT * FROM plants WHERE id = :id")
    suspend fun getByIdOnce(id: Long): Plant?

    @Query("SELECT * FROM plants")
    suspend fun getAllOnce(): List<Plant>

    @Insert
    suspend fun insert(plant: Plant): Long

    @Update
    suspend fun update(plant: Plant)

    @Delete
    suspend fun delete(plant: Plant)

    @Query("DELETE FROM plants")
    suspend fun deleteAll()
}
