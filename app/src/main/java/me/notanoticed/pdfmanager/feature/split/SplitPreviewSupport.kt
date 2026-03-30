/**
 * Utilities for preparing split previews from a ready split plan.
 */

package me.notanoticed.pdfmanager.feature.split

import android.content.Context
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import me.notanoticed.pdfmanager.core.pdf.PagesPerSheetOption
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
    return when (plan.method) {
        SplitMethodType.PAGE_RANGES -> {
            buildPageRangesPreviewPdf(
                context = context,
                sourcePdf = sourcePdf,
                plan = plan,
                pagesPerSheet = pagesPerSheet
            )
        }

        SplitMethodType.SINGLE_PAGE_PER_FILE,
        SplitMethodType.EVERY_N_PAGES -> sourcePdf
    }
}

private fun buildPageRangesPreviewPdf(
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
                PDDocument().use { outputDocument ->
                    plan.chunks
                        .flatMap { it.pages }
                        .forEach { pageNumber ->
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
        pagesCount = plan.totalPagesCovered,
        storagePath = previewUri.toString(),
        lastModifiedEpochSeconds = now,
        createdEpochSeconds = now,
        isLocked = false
    )
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
