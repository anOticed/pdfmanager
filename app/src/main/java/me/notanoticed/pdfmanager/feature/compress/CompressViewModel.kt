package me.notanoticed.pdfmanager.feature.compress

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.pdf.PdfDocumentActions
import me.notanoticed.pdfmanager.core.pdf.copyFileToUri
import me.notanoticed.pdfmanager.core.pdf.createTempPdfFile
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.feature.export.PdfOutputRequest
import java.io.File

class CompressViewModel : ViewModel() {
    var selectedPdf by mutableStateOf<PdfFile?>(null)
        private set

    var selectedPreset by mutableStateOf(PdfCompressionPreset.MEDIUM)
        private set

    fun open(pdf: PdfFile) {
        selectedPdf = pdf
        selectedPreset = PdfCompressionPreset.MEDIUM
    }

    fun close() {
        selectedPdf = null
    }

    fun selectPreset(preset: PdfCompressionPreset) {
        selectedPreset = preset
    }

    fun requestExport(
        context: Context,
        onRequest: (PdfOutputRequest) -> Unit
    ) {
        val pdf = selectedPdf ?: return
        val preset = selectedPreset
        val suggestedName = buildSuggestedCompressedFileName(
            context = context,
            pdf = pdf
        )

        onRequest(
            PdfOutputRequest.SaveFile(
                dialogTitle = context.getString(R.string.compress_save_dialog_title),
                inputLabel = context.getString(R.string.pdflist_file_name_label),
                inputHint = context.getString(R.string.compress_save_dialog_hint),
                confirmLabel = context.getString(R.string.output_choose_location),
                suggestedName = suggestedName,
                processingMessage = context.getString(R.string.compress_processing_message)
            ) { requestContext, destinationUri, _ ->
                val originalTempFile = createTempPdfFile(
                    context = requestContext,
                    directoryName = "compressed_pdf_output",
                    filePrefix = "original_pdf_"
                )
                val compressedTempFile = createTempPdfFile(
                    context = requestContext,
                    directoryName = "compressed_pdf_output",
                    filePrefix = "compressed_pdf_"
                )

                try {
                    copySourcePdfToFile(
                        context = requestContext,
                        sourcePdf = pdf,
                        outputFile = originalTempFile
                    )

                    compressPdf(
                        context = requestContext,
                        sourceUri = pdf.uri,
                        outputFile = compressedTempFile,
                        preset = preset
                    )

                    val fileToExport = if (compressedTempFile.length() < originalTempFile.length()) {
                        compressedTempFile
                    } else {
                        originalTempFile
                    }

                    copyFileToUri(
                        context = requestContext,
                        sourceFile = fileToExport,
                        destinationUri = destinationUri
                    )

                    close()
                    requestContext.getString(R.string.compress_export_success)
                } finally {
                    runCatching { originalTempFile.delete() }
                    runCatching { compressedTempFile.delete() }
                }
            }
        )
    }
}

private fun buildSuggestedCompressedFileName(
    context: Context,
    pdf: PdfFile
): String {
    val baseName = PdfDocumentActions.normalizeBaseName(
        rawName = pdf.name,
        fallbackName = context.getString(R.string.compress_output_fallback_name)
    )

    return "${baseName}_compressed.pdf"
}

private fun copySourcePdfToFile(
    context: Context,
    sourcePdf: PdfFile,
    outputFile: File
) {
    val inputStream = context.contentResolver.openInputStream(sourcePdf.uri)
        ?: error("Failed to open source PDF")

    inputStream.use { input ->
        outputFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}
