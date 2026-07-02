package ir.plantcare.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val species: String = "",
    val photoFileName: String? = null,
    val wateringIntervalDays: Int = 3,
    val fertilizingIntervalDays: Int = 20,
    val lastWateringMillis: Long? = null,
    val lastFertilizingMillis: Long? = null,
    val notes: String = "",
    val createdMillis: Long = System.currentTimeMillis()
) {
    fun nextWateringMillis(): Long? =
        lastWateringMillis?.let { it + wateringIntervalDays.toLong() * 24 * 3600 * 1000 }

    fun nextFertilizingMillis(): Long? =
        lastFertilizingMillis?.let { it + fertilizingIntervalDays.toLong() * 24 * 3600 * 1000 }
}
