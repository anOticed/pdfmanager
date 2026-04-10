/**
 * Utilities for preparing split previews from a ready split plan.
 */

package me.notanoticed.pdfmanager.feature.split

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.pdmodel.PDDocument
import me.notanoticed.pdfmanager.core.pdf.PdfPageSource
import me.notanoticed.pdfmanager.core.pdf.PagesPerSheetOption
import me.notanoticed.pdfmanager.core.pdf.buildPdfFromPageGroups
import me.notanoticed.pdfmanager.core.pdf.copyFileToUri
import me.notanoticed.pdfmanager.core.pdf.createPdfDocumentInTree
import me.notanoticed.pdfmanager.core.pdf.createTempPdfFile
import me.notanoticed.pdfmanager.core.pdf.ensurePdfBoxInitialized
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.pdf.prepareGeneratedPdfFile
import java.io.File

/* -------------------- PREVIEW -------------------- */
internal fun prepareSplitPreviewPdf(
    context: Context,
    sourcePdf: PdfFile,
    plan: SplitPlan,
    pagesPerSheet: PagesPerSheetOption
): PdfFile {
    ensurePdfBoxInitialized(context)

    val fileName = "split_preview_${pagesPerSheet.pagesPerSheet}_pages_${System.currentTimeMillis()}.pdf"
    val outputFile = prepareGeneratedPdfFile(
        context = context,
        directoryName = "split_preview_pdf",
        fileName = fileName,
        cleanupPrefix = "split_preview_"
    )

    try {
        val outputPages = writeSplitPlanToFile(
            context = context,
            sourcePdf = sourcePdf,
            plan = plan,
            pagesPerSheet = pagesPerSheet,
            outputFile = outputFile
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
    } catch (error: Throwable) {
        runCatching { outputFile.delete() }
        throw error
    }
}

internal fun exportSplitPlanToFolder(
    context: Context,
    sourcePdf: PdfFile,
    plan: SplitPlan,
    pagesPerSheet: PagesPerSheetOption,
    folderUri: Uri,
    baseName: String
): Int {
    ensurePdfBoxInitialized(context)

    var createdCount = 0

    plan.chunks.forEachIndexed { index, chunk ->
        val tempFile = createTempPdfFile(
            context = context,
            directoryName = "split_output_pdf",
            filePrefix = "split_export_"
        )

        try {
            writeSplitPlanToFile(
                context = context,
                sourcePdf = sourcePdf,
                plan = SplitPlan(method = plan.method, chunks = listOf(chunk)),
                pagesPerSheet = pagesPerSheet,
                outputFile = tempFile
            )

            val outputName = buildSplitOutputFileName(
                baseName = baseName,
                fileIndex = index + 1
            )

            val outputUri = createPdfDocumentInTree(
                context = context,
                treeUri = folderUri,
                displayName = outputName
            )

            copyFileToUri(
                context = context,
                sourceFile = tempFile,
                destinationUri = outputUri
            )

            createdCount += 1
        } finally {
            runCatching { tempFile.delete() }
        }
    }

    return createdCount
}
/* ------------------------------------------------ */

private fun writeSplitPlanToFile(
    context: Context,
    sourcePdf: PdfFile,
    plan: SplitPlan,
    pagesPerSheet: PagesPerSheetOption,
    outputFile: File
): Int {
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

            return buildPdfFromPageGroups(
                outputFile = outputFile,
                pageGroups = pageGroups,
                pagesPerSheet = pagesPerSheet
            )
        }
    }
}

private fun buildSplitOutputFileName(
    baseName: String,
    fileIndex: Int
): String {
    val normalizedIndex = fileIndex.toString().padStart(2, '0')
    return "${baseName}_part_$normalizedIndex.pdf"
}
