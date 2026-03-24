/**
 * Utilities for validating split configuration and preparing split previews.
 */

package me.notanoticed.pdfmanager.feature.split

import android.content.Context
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/* -------------------- VALIDATION -------------------- */
internal fun validateSplitConfiguration(
    totalPages: Int,
    configuration: SplitConfiguration
): String? {
    if (totalPages <= 0) {
        return "Selected PDF contains no pages"
    }

    return when (configuration.method) {
        SplitMethodType.PAGE_RANGES -> {
            runCatching {
                parseSelectedPages(
                    totalPages = totalPages,
                    rawRanges = configuration.pageRanges
                )
            }.exceptionOrNull()?.message
        }

        SplitMethodType.SINGLE_PAGE_PER_FILE -> null

        SplitMethodType.EVERY_N_PAGES -> {
            val pagesPerFile = configuration.pagesPerFile.trim()
            when {
                pagesPerFile.isEmpty() -> "Enter pages per file first"
                pagesPerFile.toIntOrNull() == null -> "Pages per file must be a valid number"
                pagesPerFile.toInt() <= 0 -> "Pages per file must be greater than 0"
                else -> null
            }
        }
    }
}
/* --------------------------------------------------- */


/* -------------------- PREVIEW -------------------- */
internal fun prepareSplitPreviewPdf(
    context: Context,
    sourcePdf: PdfFile,
    configuration: SplitConfiguration
): PdfFile {
    validateSplitConfiguration(
        totalPages = sourcePdf.pagesCount,
        configuration = configuration
    )?.let(::error)

    return when (configuration.method) {
        SplitMethodType.PAGE_RANGES -> {
            val selectedPages = parseSelectedPages(
                totalPages = sourcePdf.pagesCount,
                rawRanges = configuration.pageRanges
            )
            buildPageRangesPreviewPdf(
                context = context,
                sourcePdf = sourcePdf,
                selectedPages = selectedPages
            )
        }

        SplitMethodType.SINGLE_PAGE_PER_FILE,
        SplitMethodType.EVERY_N_PAGES -> sourcePdf
    }
}

private fun buildPageRangesPreviewPdf(
    context: Context,
    sourcePdf: PdfFile,
    selectedPages: List<Int>
): PdfFile {
    ensurePdfBoxInitialized(context)

    val previewDir = File(context.cacheDir, "split_preview_pdf")
    if (!previewDir.exists() && !previewDir.mkdirs()) {
        error("Failed to create split preview cache directory")
    }
    cleanupOldPreviewFiles(previewDir, keepCount = 6)

    val fileName = "split_preview_${System.currentTimeMillis()}.pdf"
    val outputFile = File(previewDir, fileName)

    try {
        val inputStream = context.contentResolver.openInputStream(sourcePdf.uri)
            ?: error("Failed to open the selected PDF")

        inputStream.use { stream ->
            PDDocument.load(stream).use { sourceDocument ->
                PDDocument().use { outputDocument ->
                    selectedPages.forEach { pageNumber ->
                        outputDocument.importPage(sourceDocument.getPage(pageNumber - 1))
                    }

                    if (outputDocument.numberOfPages <= 0) {
                        error("No pages were selected for preview")
                    }

                    outputDocument.save(outputFile)
                }
            }
        }
    } catch (error: Throwable) {
        runCatching { outputFile.delete() }
        throw error
    }

    val previewUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        outputFile
    )

    val now = System.currentTimeMillis() / 1000L
    return PdfFile(
        uri = previewUri,
        name = fileName,
        sizeBytes = outputFile.length().coerceAtLeast(0L),
        pagesCount = selectedPages.size,
        storagePath = previewUri.toString(),
        lastModifiedEpochSeconds = now,
        createdEpochSeconds = now,
        isLocked = false
    )
}
/* ------------------------------------------------ */


/* -------------------- PARSING -------------------- */
private fun parseSelectedPages(
    totalPages: Int,
    rawRanges: String
): List<Int> {
    val trimmed = rawRanges.trim()
    if (trimmed.isEmpty()) {
        error("Enter page ranges first")
    }

    val selectedPages = mutableListOf<Int>()
    val chunks = trimmed.split(",")

    chunks.forEach { rawChunk ->
        val chunk = rawChunk.trim()
        if (chunk.isEmpty()) {
            error("Page ranges contain an empty value")
        }

        if ("-" in chunk) {
            val parts = chunk.split("-", limit = 2).map(String::trim)
            if (parts.size != 2 || parts.any { it.isEmpty() }) {
                error("Invalid page range: $chunk")
            }

            val start = parts[0].toIntOrNull()
                ?: error("Invalid page number: ${parts[0]}")
            val end = parts[1].toIntOrNull()
                ?: error("Invalid page number: ${parts[1]}")

            if (start <= 0 || end <= 0) {
                error("Page numbers must start from 1")
            }
            if (start > end) {
                error("Invalid page range: $chunk")
            }
            if (end > totalPages) {
                error("Page range exceeds document length")
            }

            for (page in start..end) {
                selectedPages += page
            }
        } else {
            val page = chunk.toIntOrNull()
                ?: error("Invalid page number: $chunk")

            if (page <= 0) {
                error("Page numbers must start from 1")
            }
            if (page > totalPages) {
                error("Page range exceeds document length")
            }

            selectedPages += page
        }
    }

    if (selectedPages.isEmpty()) {
        error("Enter at least one page for preview")
    }

    return selectedPages
}
/* ------------------------------------------------ */


/* -------------------- HELPERS -------------------- */
private val isPdfBoxInitialized = AtomicBoolean(false)

private fun ensurePdfBoxInitialized(context: Context) {
    if (isPdfBoxInitialized.get()) return

    synchronized(isPdfBoxInitialized) {
        if (isPdfBoxInitialized.get()) return
        PDFBoxResourceLoader.init(context.applicationContext)
        isPdfBoxInitialized.set(true)
    }
}

private fun cleanupOldPreviewFiles(
    dir: File,
    keepCount: Int
) {
    if (!dir.exists() || !dir.isDirectory) return

    val staleFiles = dir.listFiles()
        ?.filter { it.isFile && it.name.startsWith("split_preview_") && it.name.endsWith(".pdf") }
        ?.sortedByDescending { it.lastModified() }
        ?.drop(keepCount)
        ?: return

    staleFiles.forEach { file ->
        runCatching { file.delete() }
    }
}
/* ------------------------------------------------ */
