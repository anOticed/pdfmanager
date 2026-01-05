package me.notanoticed.pdfmanager.core.pdf.model

import android.graphics.Bitmap
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* -------------------- PDF FILE -------------------- */
data class PdfFile(
    var uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val pagesCount: Int,
    val storagePath: String,
    val lastModifiedEpochSeconds: Long,
    val createdEpochSeconds: Long,
    val bitmap: Bitmap? = null,
    val isLocked: Boolean
) {
    fun metaLine(): String {
        val sizeText = size()

        return if (isLocked) {
            sizeText
        } else {
            val pagesText = when(pagesCount) {
                1 -> "1 page"
                else -> "$pagesCount pages"
            }
            "$pagesText • $sizeText"
        }
    }

    fun createdDate(): String = formatDate(createdEpochSeconds)

    fun lastModifiedDate(): String = formatDate(lastModifiedEpochSeconds)

    fun size(): String = formatBytes(sizeBytes)

    private fun formatBytes(bytes: Long): String {
        val kb = 1000.0
        val mb = kb * 1000
        val gb = mb * 1000

        return when {
            bytes < kb -> "$bytes B"
            bytes < mb -> String.format(Locale.US, "%.1f KB", bytes / kb)
            bytes < gb -> String.format(Locale.US, "%.1f MB", bytes / mb)
            else -> String.format(Locale.US, "%.1f GB", bytes / gb)
        }
    }

    private fun formatDate(epochSeconds: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        return sdf.format(Date(epochSeconds * 1000L))
    }
}
/* ------------------------------------------------- */