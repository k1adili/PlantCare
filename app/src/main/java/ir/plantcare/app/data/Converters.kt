package ir.plantcare.app.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromCareType(type: CareType): String = type.name

    @TypeConverter
    fun toCareType(value: String): CareType = CareType.valueOf(value)
}
