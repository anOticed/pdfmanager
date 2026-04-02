/**
 * Utilities for preparing split previews from a ready split plan.
 */

package me.notanoticed.pdfmanager.feature.split

import android.content.Context
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import me.notanoticed.pdfmanager.core.pdf.PdfPageSource
import me.notanoticed.pdfmanager.core.pdf.PagesPerSheetOption
import me.notanoticed.pdfmanager.core.pdf.buildPdfFromPageGroups
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/* -------------------- PREVIEW -------------------- */
internal fun prepareSplitPreviewPdf(
    context: Context,
    sourcePdf: PdfFile,
    plan: SplitPlan,
    pagesPerSheet: PagesPerSheetOption
): PdfFile {
    ensurePdfBoxInitialized(context)

    val previewDir = File(context.cacheDir, "split_preview_pdf")
    if (!previewDir.exists() && !previewDir.mkdirs()) {
        error("Failed to create split preview cache directory")
    }
    cleanupOldPreviewFiles(previewDir, keepCount = 6)

    val fileName = "split_preview_${pagesPerSheet.pagesPerSheet}_pages_${System.currentTimeMillis()}.pdf"
    val outputFile = File(previewDir, fileName)

    try {
        val inputStream = context.contentResolver.openInputStream(sourcePdf.uri)
            ?: error("Failed to open the selected PDF")

        inputStream.use { stream ->
            PDDocument.load(stream).use { sourceDocument ->
                val pageGroups = plan.chunks.map { chunk ->
                    chunk.pages.map { pageNumber ->
                        PdfPageSource(
                            document = sourceDocument,
                            pageIndex = pageNumber - 1
                        )
                    }
                }

                val outputPages = buildPdfFromPageGroups(
                    outputFile = outputFile,
                    pageGroups = pageGroups,
                    pagesPerSheet = pagesPerSheet
                )

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
                    pagesCount = outputPages,
                    storagePath = previewUri.toString(),
                    lastModifiedEpochSeconds = now,
                    createdEpochSeconds = now,
                    isLocked = false
                )
            }
        }
    } catch (error: Throwable) {
        runCatching { outputFile.delete() }
        throw error
    }
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
