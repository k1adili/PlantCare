package ir.plantcare.app.util

import android.content.Context
import android.net.Uri
import ir.plantcare.app.data.AppDatabase
import ir.plantcare.app.data.CareLog
import ir.plantcare.app.data.CareType
import ir.plantcare.app.data.Plant
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {

    /** خروجی گرفتن از کل اطلاعات (گیاهان + تاریخچه + عکس‌ها) به صورت یک فایل zip. */
    suspend fun exportBackup(context: Context, destUri: Uri) {
        val db = AppDatabase.getInstance(context)
        val plants = db.plantDao().getAllOnce()
        val logs = db.careLogDao().getAllOnce()

        val plantsArray = JSONArray()
        plants.forEach { p ->
            plantsArray.put(JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("species", p.species)
                put("photoFileName", p.photoFileName ?: JSONObject.NULL)
                put("wateringIntervalDays", p.wateringIntervalDays)
                put("fertilizingIntervalDays", p.fertilizingIntervalDays)
                put("lastWateringMillis", p.lastWateringMillis ?: JSONObject.NULL)
                put("lastFertilizingMillis", p.lastFertilizingMillis ?: JSONObject.NULL)
                put("notes", p.notes)
                put("createdMillis", p.createdMillis)
            })
        }

        val logsArray = JSONArray()
        logs.forEach { l ->
            logsArray.put(JSONObject().apply {
                put("id", l.id)
                put("plantId", l.plantId)
                put("type", l.type.name)
                put("dateMillis", l.dateMillis)
                put("note", l.note)
            })
        }

        val root = JSONObject().apply {
            put("version", 1)
            put("exportedAt", System.currentTimeMillis())
            put("plants", plantsArray)
            put("careLogs", logsArray)
        }

        val photosDir = File(context.filesDir, "photos")

        context.contentResolver.openOutputStream(destUri)?.use { rawOut ->
            ZipOutputStream(rawOut).use { zip ->
                zip.putNextEntry(ZipEntry("data.json"))
                zip.write(root.toString().toByteArray(Charsets.UTF_8))
                zip.closeEntry()

                if (photosDir.exists()) {
                    photosDir.listFiles()?.forEach { file ->
                        zip.putNextEntry(ZipEntry("photos/${file.name}"))
                        file.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }
            }
        }
    }

    /** بازیابی از یک فایل بکاپ zip. تمام اطلاعات فعلی پاک و جایگزین می‌شود. */
    suspend fun importBackup(context: Context, sourceUri: Uri) {
        val db = AppDatabase.getInstance(context)
        val photosDir = File(context.filesDir, "photos").apply { mkdirs() }

        var jsonText: String? = null
        val extractedPhotos = mutableMapOf<String, ByteArray>()

        context.contentResolver.openInputStream(sourceUri)?.use { rawIn ->
            ZipInputStream(rawIn).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val name = entry.name
                    val bytes = zip.readBytes()
                    if (name == "data.json") {
                        jsonText = String(bytes, Charsets.UTF_8)
                    } else if (name.startsWith("photos/")) {
                        extractedPhotos[name.removePrefix("photos/")] = bytes
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }

        val text = jsonText ?: throw IllegalStateException("فایل بکاپ نامعتبر است")
        val root = JSONObject(text)

        // پاک‌سازی اطلاعات فعلی
        db.careLogDao().deleteAll()
        db.plantDao().deleteAll()
        photosDir.listFiles()?.forEach { it.delete() }

        // نوشتن عکس‌ها
        extractedPhotos.forEach { (name, bytes) ->
            File(photosDir, name).writeBytes(bytes)
        }

        val oldToNewPlantId = mutableMapOf<Long, Long>()
        val plantsArray = root.getJSONArray("plants")
        for (i in 0 until plantsArray.length()) {
            val o = plantsArray.getJSONObject(i)
            val plant = Plant(
                name = o.getString("name"),
                species = o.optString("species", ""),
                photoFileName = if (o.isNull("photoFileName")) null else o.getString("photoFileName"),
                wateringIntervalDays = o.optInt("wateringIntervalDays", 3),
                fertilizingIntervalDays = o.optInt("fertilizingIntervalDays", 20),
                lastWateringMillis = if (o.isNull("lastWateringMillis")) null else o.getLong("lastWateringMillis"),
                lastFertilizingMillis = if (o.isNull("lastFertilizingMillis")) null else o.getLong("lastFertilizingMillis"),
                notes = o.optString("notes", ""),
                createdMillis = o.optLong("createdMillis", System.currentTimeMillis())
            )
            val newId = db.plantDao().insert(plant)
            oldToNewPlantId[o.getLong("id")] = newId
        }

        val logsArray = root.getJSONArray("careLogs")
        for (i in 0 until logsArray.length()) {
            val o = logsArray.getJSONObject(i)
            val oldPlantId = o.getLong("plantId")
            val newPlantId = oldToNewPlantId[oldPlantId] ?: continue
            val log = CareLog(
                plantId = newPlantId,
                type = CareType.valueOf(o.getString("type")),
                dateMillis = o.getLong("dateMillis"),
                note = o.optString("note", "")
            )
            db.careLogDao().insert(log)
        }
    }
}
