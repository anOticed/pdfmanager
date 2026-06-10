package me.notanoticed.pdfmanager.core.pdf.write

import android.content.Context
import com.tom_roush.pdfbox.multipdf.LayerUtility
import com.tom_roush.pdfbox.pdmodel.PDDocument
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.pdf.util.PagesPerSheetOption
import me.notanoticed.pdfmanager.core.pdf.util.PdfFileNamePolicy
import me.notanoticed.pdfmanager.core.pdf.util.PdfPageSource
import me.notanoticed.pdfmanager.core.pdf.util.appendPdfSheet
import me.notanoticed.pdfmanager.core.pdf.util.ensurePdfBoxInitialized
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.pdf.render.buildGeneratedPreviewPdf
import me.notanoticed.pdfmanager.core.pdf.render.GeneratedPreviewPdf
import me.notanoticed.pdfmanager.core.system.export.PdfExportException
import me.notanoticed.pdfmanager.core.system.export.PreparedPdfFile
import me.notanoticed.pdfmanager.core.system.files.TempFileStore
import java.io.File

internal object MergePdfWriter {
    fun buildPreviewPdf(
        context: Context,
        pdfs: List<PdfFile>,
        pagesPerSheet: PagesPerSheetOption
    ): GeneratedPreviewPdf {
        require(pdfs.isNotEmpty()) { context.getString(R.string.merge_error_no_source_pdfs) }

        ensurePdfBoxInitialized(context)

        val fileName = "merge_preview_${pagesPerSheet.pagesPerSheet}_pages_${System.currentTimeMillis()}.pdf"
        return buildGeneratedPreviewPdf(
            context = context,
            directoryName = "merge_preview_pdf",
            fileName = fileName,
            cleanupPrefix = "merge_preview_"
        ) { outputFile ->
            writeMergedPdfToFile(
                context = context,
                pdfs = pdfs,
                pagesPerSheet = pagesPerSheet,
                outputFile = outputFile
            )
        }
    }

    fun prepareExportFile(
        context: Context,
        pdfs: List<PdfFile>,
        pagesPerSheet: PagesPerSheetOption
    ): PreparedPdfFile {
        val tempFile = TempFileStore.createTempPdfFile(
            context = context,
            directoryName = "merge_output_pdf",
            filePrefix = "merge_export_"
        )

        return try {
            writeMergedPdfToFile(
                context = context,
                pdfs = pdfs,
                pagesPerSheet = pagesPerSheet,
                outputFile = tempFile
            )
            PreparedPdfFile(tempFile) {
                runCatching { tempFile.delete() }
            }
        } catch (error: Throwable) {
            runCatching { tempFile.delete() }
            throw error
        }
    }

    fun buildSuggestedFileName(
        context: Context,
        pdfs: List<PdfFile>
    ): String {
        val firstName = pdfs.firstOrNull()?.name?.let { name ->
            PdfFileNamePolicy.normalizeBaseName(
                rawName = name,
                fallbackName = context.getString(R.string.merge_output_fallback_name)
            )
        }.orEmpty()

        return if (pdfs.size == 1 && firstName.isNotBlank()) {
            context.getString(R.string.merge_output_single_file_name, firstName)
        } else {
            "${context.getString(R.string.merge_output_fallback_name)}_${System.currentTimeMillis()}.pdf"
        }
    }

    private fun writeMergedPdfToFile(
        context: Context,
        pdfs: List<PdfFile>,
        pagesPerSheet: PagesPerSheetOption,
        outputFile: File
    ): Int {
        PDDocument().use { outputDocument ->
            val layerUtility = LayerUtility(outputDocument)
            val pendingPages = mutableListOf<PdfPageSource>()
            val retainedDocuments = linkedSetOf<PDDocument>()
            var outputPages = 0

            fun flushPending(currentDocument: PDDocument?) {
                if (pendingPages.isEmpty()) return

                appendPdfSheet(
                    outputDocument = outputDocument,
                    layerUtility = layerUtility,
                    sheetPages = pendingPages.toList(),
                    pagesPerSheet = pagesPerSheet
                )
                outputPages += 1
                pendingPages.clear()

                val documentsToKeep = buildSet {
                    if (currentDocument != null) add(currentDocument)
                }

                val iterator = retainedDocuments.iterator()
                while (iterator.hasNext()) {
                    val document = iterator.next()
                    if (document !in documentsToKeep) {
                        runCatching { document.close() }
                        iterator.remove()
                    }
                }
            }

            try {
                pdfs.forEach { inputPdf ->
                    val inputStream = context.contentResolver.openInputStream(inputPdf.uri)
                        ?: throw PdfExportException(context.getString(R.string.merge_error_open_source_pdf))

                    val sourceDocument = inputStream.use { stream ->
                        PDDocument.load(stream)
                    }
                    retainedDocuments += sourceDocument

                    for (pageIndex in 0 until sourceDocument.numberOfPages) {
                        pendingPages += PdfPageSource(
                            document = sourceDocument,
                            pageIndex = pageIndex
                        )

                        if (pendingPages.size == pagesPerSheet.pagesPerSheet) {
                            flushPending(currentDocument = sourceDocument)
                        }
                    }

                    val hasPendingPagesFromCurrent = pendingPages.any { it.document === sourceDocument }
                    if (!hasPendingPagesFromCurrent) {
                        runCatching { sourceDocument.close() }
                        retainedDocuments.remove(sourceDocument)
                    }
                }

                if (pendingPages.isEmpty() && outputPages == 0) {
                    throw PdfExportException(context.getString(R.string.merge_error_no_pages))
                }

                flushPending(currentDocument = null)
                outputDocument.save(outputFile)
                return outputPages
            } finally {
                retainedDocuments.forEach { source ->
                    runCatching { source.close() }
                }
            }
        }
    }
}
