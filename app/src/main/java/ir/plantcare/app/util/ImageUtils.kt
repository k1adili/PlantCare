package ir.plantcare.app.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

object ImageUtils {

    private fun photosDir(context: Context): File {
        val dir = File(context.filesDir, "photos")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** کپی کردن عکس انتخاب‌شده/گرفته‌شده به حافظه داخلی اپ و برگرداندن فقط نام فایل. */
    fun saveImageFromUri(context: Context, sourceUri: Uri): String? {
        return try {
            val fileName = "plant_${UUID.randomUUID()}.jpg"
            val destFile = File(photosDir(context), fileName)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            fileName
        } catch (e: Exception) {
            null
        }
    }

    fun fileFor(context: Context, fileName: String?): File? {
        if (fileName == null) return null
        val f = File(photosDir(context), fileName)
        return if (f.exists()) f else null
    }

    fun deleteImage(context: Context, fileName: String?) {
        if (fileName == null) return
        File(photosDir(context), fileName).let { if (it.exists()) it.delete() }
    }

    /** یک فایل جدید و خالی برای عکس دوربین بساز و Uri آن (از طریق FileProvider) را برگردان. */
    fun createCameraOutputUri(context: Context): Pair<Uri, String> {
        val fileName = "plant_${UUID.randomUUID()}.jpg"
        val destFile = File(photosDir(context), fileName)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context, "ir.plantcare.app.fileprovider", destFile
        )
        return uri to fileName
    }
}
