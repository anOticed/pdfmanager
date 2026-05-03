/**
 * Shared PDF model used across the UI.
 *
 * PdfFile represents a single document selected from MediaStore or SAF and contains
 * metadata needed by the UI (name, size, page count, timestamps, storage path).
 */

package me.notanoticed.pdfmanager.core.pdf.model

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.pdf.formatFileSize
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
    val thumbnailBitmap: Bitmap? = null,
    val isLocked: Boolean
) {
    fun metaLine(context: Context): String {
        val sizeText = size(context)
        if (isLocked || pagesCount <= 0) return sizeText

        val pagesText = context.resources.getQuantityString(
            R.plurals.pdf_page_count,
            pagesCount,
            pagesCount
        )
        return "$pagesText | $sizeText"
    }

    fun createdDate(): String = formatDate(createdEpochSeconds)

    fun lastModifiedDate(): String = formatDate(lastModifiedEpochSeconds)

    fun size(context: Context): String = formatFileSize(context, sizeBytes)

    private fun formatDate(epochSeconds: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        return sdf.format(Date(epochSeconds * 1000L))
    }
}
/* ------------------------------------------------- */
