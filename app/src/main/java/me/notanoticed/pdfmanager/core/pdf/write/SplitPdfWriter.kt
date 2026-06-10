package me.notanoticed.pdfmanager.core.pdf.write

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.pdf.edit.SplitPlan
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.pdf.render.GeneratedPreviewPdf
import me.notanoticed.pdfmanager.core.pdf.render.buildGeneratedPreviewPdf
import me.notanoticed.pdfmanager.core.pdf.util.PagesPerSheetOption
import me.notanoticed.pdfmanager.core.pdf.util.PdfFileNamePolicy
import me.notanoticed.pdfmanager.core.pdf.util.PdfPageSource
import me.notanoticed.pdfmanager.core.pdf.util.buildPdfFromPageGroups
import me.notanoticed.pdfmanager.core.pdf.util.ensurePdfBoxInitialized
import me.notanoticed.pdfmanager.core.system.export.PdfExportException
import me.notanoticed.pdfmanager.core.system.files.DestinationWriter
import me.notanoticed.pdfmanager.core.system.files.TempFileStore
import java.io.File

internal object SplitPdfWriter {
    fun buildPreviewPdf(
        context: Context,
        sourcePdf: PdfFile,
        plan: SplitPlan,
        pagesPerSheet: PagesPerSheetOption
    ): GeneratedPreviewPdf {
        ensurePdfBoxInitialized(context)

        val fileName = "split_preview_${pagesPerSheet.pagesPerSheet}_pages_${System.currentTimeMillis()}.pdf"
        return buildGeneratedPreviewPdf(
            context = context,
            directoryName = "split_preview_pdf",
            fileName = fileName,
            cleanupPrefix = "split_preview_"
        ) { outputFile ->
            writeSplitPlanToFile(
                context = context,
                sourcePdf = sourcePdf,
                plan = plan,
                pagesPerSheet = pagesPerSheet,
                outputFile = outputFile
            )
        }
    }

    fun exportPdf(
        context: Context,
        sourcePdf: PdfFile,
        plan: SplitPlan,
        pagesPerSheet: PagesPerSheetOption,
        folderUri: Uri,
        baseName: String
    ): Int {
        ensurePdfBoxInitialized(context)

        var createdCount = 0
        val createdUris = mutableListOf<Uri>()

        try {
            plan.chunks.forEachIndexed { index, chunk ->
                val tempFile = TempFileStore.createTempPdfFile(
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

                    val outputName = context.getString(
                        R.string.split_output_part_file_name,
                        baseName,
                        index + 1
                    )

                    val outputUri = DestinationWriter.writePdfDocumentInTree(
                        context = context,
                        treeUri = folderUri,
                        displayName = outputName,
                        sourceFile = tempFile
                    )

                    createdUris += outputUri
                    createdCount += 1
                } finally {
                    runCatching { tempFile.delete() }
                }
            }
        } catch (error: Throwable) {
            createdUris.forEach { createdUri ->
                DestinationWriter.deleteUri(context, createdUri)
            }
            throw error
        }

        return createdCount
    }

    fun buildSuggestedBaseName(
        context: Context,
        sourcePdf: PdfFile
    ): String {
        return PdfFileNamePolicy.normalizeBaseName(
            rawName = sourcePdf.name,
            fallbackName = context.getString(R.string.split_output_fallback_name)
        )
    }

    private fun writeSplitPlanToFile(
        context: Context,
        sourcePdf: PdfFile,
        plan: SplitPlan,
        pagesPerSheet: PagesPerSheetOption,
        outputFile: File
    ): Int {
        val inputStream = context.contentResolver.openInputStream(sourcePdf.uri)
            ?: throw PdfExportException(context.getString(R.string.split_error_open_selected_pdf))

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
}
