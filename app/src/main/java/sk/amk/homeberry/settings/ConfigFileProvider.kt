package sk.amk.homeberry.settings

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import sk.amk.homeberry.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Andrej Martinák <andrej.martinak@gmail.com>
 */
class ConfigFileProvider : FileProvider() {

    companion object {

        private const val EXPORT_FILE_PREFIX = "HomeBerry_export_"
        private const val EXPORT_FILE_EXTENSION = ".json"

        fun createFileUri(context: Context, content: String): Uri {
            val dir = File("${context.getExternalFilesDir(null)}")
            val file = File(dir, createExportFileName())
            file.writeText(content)

            return getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
        }

        private fun createExportFileName(): String {
            return "$EXPORT_FILE_PREFIX${createTimestampString()}$EXPORT_FILE_EXTENSION"
        }

        private fun createTimestampString(): String {
            val calendar = Calendar.getInstance()
            val format = SimpleDateFormat("yyyyMMdd")
            return format.format(calendar.time)
        }
    }
}
