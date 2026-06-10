package me.notanoticed.pdfmanager.core.pdf.write

import android.content.Context
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.feature.compress.CompressionPreset
import me.notanoticed.pdfmanager.core.pdf.util.PdfFileNamePolicy
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.system.export.PreparedPdfFile
import me.notanoticed.pdfmanager.core.system.files.DestinationWriter
import me.notanoticed.pdfmanager.core.system.files.TempFileStore

internal object CompressPdfWriter {
    fun prepareCompressedPdfFile(
        context: Context,
        pdf: PdfFile,
        preset: CompressionPreset
    ): PreparedPdfFile {
        val originalTempFile = TempFileStore.createTempPdfFile(
            context = context,
            directoryName = "compressed_pdf_output",
            filePrefix = "original_pdf_"
        )
        val compressedTempFile = TempFileStore.createTempPdfFile(
            context = context,
            directoryName = "compressed_pdf_output",
            filePrefix = "compressed_pdf_"
        )

        return try {
            DestinationWriter.copyUriToFile(
                context = context,
                sourceUri = pdf.uri,
                outputFile = originalTempFile
            )

            compressPdf(
                context = context,
                sourceUri = pdf.uri,
                outputFile = compressedTempFile,
                preset = preset
            )

            val fileToExport = if (compressedTempFile.length() < originalTempFile.length()) {
                compressedTempFile
            } else {
                originalTempFile
            }

            PreparedPdfFile(fileToExport) {
                runCatching { originalTempFile.delete() }
                runCatching { compressedTempFile.delete() }
            }
        } catch (error: Throwable) {
            runCatching { originalTempFile.delete() }
            runCatching { compressedTempFile.delete() }
            throw error
        }
    }

    fun buildSuggestedFileName(
        context: Context,
        pdf: PdfFile
    ): String {
        val baseName = PdfFileNamePolicy.normalizeBaseName(
            rawName = pdf.name,
            fallbackName = context.getString(R.string.compress_output_fallback_name)
        )

        return "${baseName}_compressed.pdf"
    }
}
