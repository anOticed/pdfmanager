package me.notanoticed.pdfmanager.core.pdf.util

import android.content.Context
import me.notanoticed.pdfmanager.R

fun formatFileSize(
    context: Context,
    bytes: Long,
    unknownWhenNonPositive: Boolean = false
): String {
    val kb = 1000.0
    val mb = kb * 1000
    val gb = mb * 1000

    if (bytes <= 0L) {
        return if (unknownWhenNonPositive) {
            context.getString(R.string.file_size_unknown)
        } else {
            context.getString(R.string.file_size_bytes_zero)
        }
    }

    return when {
        bytes < kb -> context.getString(R.string.file_size_bytes_format, bytes)
        bytes < mb -> context.getString(R.string.file_size_kilobytes_format, bytes / kb)
        bytes < gb -> context.getString(R.string.file_size_megabytes_format, bytes / mb)
        else -> context.getString(R.string.file_size_gigabytes_format, bytes / gb)
    }
}
