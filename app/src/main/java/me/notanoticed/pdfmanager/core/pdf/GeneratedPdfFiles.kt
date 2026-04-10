package me.notanoticed.pdfmanager.core.pdf

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

private val isPdfBoxInitialized = AtomicBoolean(false)

fun ensurePdfBoxInitialized(context: Context) {
    if (isPdfBoxInitialized.get()) return

    synchronized(isPdfBoxInitialized) {
        if (isPdfBoxInitialized.get()) return
        PDFBoxResourceLoader.init(context.applicationContext)
        isPdfBoxInitialized.set(true)
    }
}

fun prepareGeneratedPdfFile(
    context: Context,
    directoryName: String,
    fileName: String,
    cleanupPrefix: String,
    keepCount: Int = 6
): File {
    val outputDir = File(context.cacheDir, directoryName)
    if (!outputDir.exists() && !outputDir.mkdirs()) {
        error("Failed to create output directory")
    }

    cleanupGeneratedPdfFiles(
        dir = outputDir,
        prefix = cleanupPrefix,
        keepCount = keepCount
    )

    return File(outputDir, fileName)
}

fun cleanupGeneratedPdfFiles(
    dir: File,
    prefix: String,
    keepCount: Int,
    suffix: String = ".pdf"
) {
    if (!dir.exists() || !dir.isDirectory) return

    val staleFiles = dir.listFiles()
        ?.filter { it.isFile && it.name.startsWith(prefix) && it.name.endsWith(suffix) }
        ?.sortedByDescending { it.lastModified() }
        ?.drop(keepCount)
        ?: return

    staleFiles.forEach { file ->
        runCatching { file.delete() }
    }
}
