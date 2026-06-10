package me.notanoticed.pdfmanager.feature.split

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.pdf.catalog.PdfCatalogRepository
import me.notanoticed.pdfmanager.core.pdf.util.PagesPerSheetOption
import me.notanoticed.pdfmanager.core.pdf.edit.SplitConfiguration
import me.notanoticed.pdfmanager.core.pdf.edit.SplitMethodType
import me.notanoticed.pdfmanager.core.pdf.edit.SplitPlan
import me.notanoticed.pdfmanager.core.pdf.edit.SplitPlanResult
import me.notanoticed.pdfmanager.core.pdf.edit.buildSplitPlan
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.pdf.write.SplitPdfWriter
import me.notanoticed.pdfmanager.core.system.export.PdfOutputRequest
import me.notanoticed.pdfmanager.core.pdf.edit.resolveMessage
import me.notanoticed.pdfmanager.core.system.pickers.FilePickers
import me.notanoticed.pdfmanager.core.system.toast.ToastBindable

class SplitViewModel : ViewModel(), ToastBindable {
    var selectedSplitPdf: PdfFile? by mutableStateOf(null)
        private set

    val isActive: Boolean get() = selectedSplitPdf != null

    var splitConfiguration by mutableStateOf(SplitConfiguration())
        private set
    var pagesPerSheetOption by mutableStateOf(PagesPerSheetOption.ONE)
        private set

    val selectedSplitMethod: SplitMethodType
        get() = splitConfiguration.method

    val splitRangesText: String
        get() = splitConfiguration.pageRanges

    val splitPagesPerFileText: String
        get() = splitConfiguration.pagesPerFile

    val splitPlanResult: SplitPlanResult?
        get() = selectedSplitPdf?.let { pdf ->
            buildSplitPlan(
                totalPages = pdf.pagesCount,
                configuration = splitConfiguration
            )
        }

    private var toast: ((String) -> Unit)? = null

    override fun bindToast(toast: (String) -> Unit) {
        this.toast = toast
    }

    override fun unbindToast() {
        toast = null
    }

    private fun showToast(message: String) {
        toast?.invoke(message)
    }

    private fun showToast(
        context: Context,
        messageRes: Int,
        vararg args: Any
    ) {
        showToast(context.getString(messageRes, *args))
    }

    fun updateSelectedSplitPdf(
        context: Context,
        pdfFile: PdfFile
    ) {
        if (pdfFile.isLocked) {
            showToast(context, R.string.split_locked_single)
            return
        }

        selectedSplitPdf = pdfFile
        resetSplitConfiguration()
    }

    fun closeSelectedSplitPdf() {
        selectedSplitPdf = null
        resetSplitConfiguration()
    }

    fun selectSplitMethod(method: SplitMethodType) {
        splitConfiguration = splitConfiguration.copy(method = method)
    }

    fun updateSplitRangesText(text: String) {
        splitConfiguration = splitConfiguration.copy(pageRanges = text)
    }

    fun updateSplitPagesPerFileText(text: String) {
        splitConfiguration = splitConfiguration.copy(pagesPerFile = text)
    }

    fun updatePagesPerSheet(option: PagesPerSheetOption) {
        pagesPerSheetOption = option
    }

    fun openPreview(onValid: (PdfFile, SplitPlan, PagesPerSheetOption) -> Unit) {
        val pdf = selectedSplitPdf ?: return
        when (val planResult = splitPlanResult) {
            is SplitPlanResult.Ready -> onValid(pdf, planResult.plan, pagesPerSheetOption)
            is SplitPlanResult.Error -> Unit
            null -> Unit
        }
    }

    fun requestSplitExport(
        context: Context,
        onRequest: (PdfOutputRequest) -> Unit
    ) {
        val pdf = selectedSplitPdf ?: return
        val plan = when (val result = splitPlanResult) {
            is SplitPlanResult.Ready -> result.plan
            is SplitPlanResult.Error -> {
                showToast(result.resolveMessage(context))
                return
            }
            null -> return
        }

        val pagesPerSheetSnapshot = pagesPerSheetOption
        val suggestedBaseName = SplitPdfWriter.buildSuggestedBaseName(
            context = context,
            sourcePdf = pdf
        )

        onRequest(
            PdfOutputRequest.SaveFolder(
                dialogTitle = context.getString(R.string.split_save_dialog_title),
                inputLabel = context.getString(R.string.split_base_file_name_label),
                inputHint = context.getString(R.string.split_save_dialog_hint),
                confirmLabel = context.getString(R.string.output_choose_folder),
                suggestedName = suggestedBaseName,
                processingMessage = context.getString(R.string.output_processing_files_message)
            ) { context, destinationUri, baseName ->
                val createdCount = SplitPdfWriter.exportPdf(
                    context = context,
                    sourcePdf = pdf,
                    plan = plan,
                    pagesPerSheet = pagesPerSheetSnapshot,
                    folderUri = destinationUri,
                    baseName = baseName
                )

                context.resources.getQuantityString(
                    R.plurals.split_export_success_count,
                    createdCount,
                    createdCount
                )
            }
        )
    }

    fun pickSplitPdf(context: Context, pickers: FilePickers) {
        pickers.pickPdf { uri ->
            viewModelScope.launch {
                val selectedPdf = PdfCatalogRepository.loadPdfMetadata(context, uri)
                updateSelectedSplitPdf(context, selectedPdf)
            }
        }
    }

    private fun resetSplitConfiguration() {
        splitConfiguration = SplitConfiguration()
    }
}
