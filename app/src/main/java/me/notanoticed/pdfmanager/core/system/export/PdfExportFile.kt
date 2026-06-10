package me.notanoticed.pdfmanager.core.system.export

import java.io.File

class PreparedPdfFile(
    val file: File,
    private val cleanupAction: () -> Unit = {}
) {
    fun cleanup() {
        runCatching(cleanupAction)
    }

    companion object {
        fun existing(file: File): PreparedPdfFile = PreparedPdfFile(file = file)
    }
}

internal inline fun <T> PreparedPdfFile.useFile(block: (File) -> T): T {
    return try {
        block(file)
    } finally {
        cleanup()
    }
}
