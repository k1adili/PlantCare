package ir.plantcare.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CareType(val label: String) {
    WATERING("آبیاری"),
    FERTILIZING("کوددهی"),
    PRUNING("هرس"),
    REPOTTING("تعویض گلدان"),
    PEST_CONTROL("سم‌پاشی / دفع آفت"),
    OTHER("سایر")
}

@Entity(
    tableName = "care_logs",
    foreignKeys = [ForeignKey(
        entity = Plant::class,
        parentColumns = ["id"],
        childColumns = ["plantId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("plantId")]
)
data class CareLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val type: CareType,
    val dateMillis: Long,
    val note: String = ""
)
