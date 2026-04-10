package me.notanoticed.pdfmanager.core.pdf

import java.util.Locale

fun formatFileSize(
    bytes: Long,
    unknownWhenNonPositive: Boolean = false
): String {
    val kb = 1000.0
    val mb = kb * 1000
    val gb = mb * 1000

    if (bytes <= 0L) {
        return if (unknownWhenNonPositive) "Unknown size" else "0 B"
    }

    return when {
        bytes < kb -> "$bytes B"
        bytes < mb -> String.format(Locale.US, "%.1f KB", bytes / kb)
        bytes < gb -> String.format(Locale.US, "%.1f MB", bytes / mb)
        else -> String.format(Locale.US, "%.1f GB", bytes / gb)
    }
}
