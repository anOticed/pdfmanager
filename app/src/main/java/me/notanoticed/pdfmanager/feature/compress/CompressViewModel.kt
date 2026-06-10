package me.notanoticed.pdfmanager.feature.compress

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.pdf.write.CompressPdfWriter
import me.notanoticed.pdfmanager.core.system.export.PdfOutputRequest

class CompressViewModel : ViewModel() {
    var selectedPdf by mutableStateOf<PdfFile?>(null)
        private set

    var selectedPreset by mutableStateOf(CompressionPreset.MEDIUM)
        private set

    fun open(pdf: PdfFile) {
        selectedPdf = pdf
        selectedPreset = CompressionPreset.MEDIUM
    }

    fun close() {
        selectedPdf = null
    }

    fun selectPreset(preset: CompressionPreset) {
        selectedPreset = preset
    }

    fun requestExport(
        context: Context,
        onRequest: (PdfOutputRequest) -> Unit
    ) {
        val pdf = selectedPdf ?: return
        val preset = selectedPreset
        val suggestedName = CompressPdfWriter.buildSuggestedFileName(
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
                processingMessage = context.getString(R.string.compress_processing_message),
                onCompleted = ::close,
                successMessage = { it.getString(R.string.compress_export_success) },
                prepareFile = { requestContext, _ ->
                    CompressPdfWriter.prepareCompressedPdfFile(
                        context = requestContext,
                        pdf = pdf,
                        preset = preset
                    )
                }
            )
        )
    }
}
