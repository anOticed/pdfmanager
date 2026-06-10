package me.notanoticed.pdfmanager.feature.merge

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.pdf.catalog.PdfCatalogRepository
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.pdf.render.loadPreviewPdf
import me.notanoticed.pdfmanager.core.pdf.util.PagesPerSheetOption
import me.notanoticed.pdfmanager.core.pdf.write.MergePdfWriter
import me.notanoticed.pdfmanager.core.system.export.PdfOutputRequest
import me.notanoticed.pdfmanager.core.system.pickers.FilePickers
import me.notanoticed.pdfmanager.core.system.toast.ToastBindable

class MergeViewModel : ViewModel(), ToastBindable {
    var pdfMergeFiles by mutableStateOf<List<PdfFile>>(emptyList())
    val isActive: Boolean get() = pdfMergeFiles.isNotEmpty()
    val total: Int get() = pdfMergeFiles.size
    var pagesPerSheetOption by mutableStateOf(PagesPerSheetOption.ONE)
        private set

    var isPreparingPreview by mutableStateOf(false)
        private set

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

    fun addMergeFiles(
        context: Context,
        pdfFiles: List<PdfFile>
    ) {
        if (pdfFiles.any { it.isLocked }) {
            when (pdfFiles.size) {
                1 -> showToast(context, R.string.merge_locked_single)
                else -> showToast(context, R.string.merge_locked_multiple)
            }
            return
        }

        val existing = pdfMergeFiles.map { it.uri }.toHashSet()
        val unique = pdfFiles.filter { existing.add(it.uri) }

        if (unique.isNotEmpty()) {
            pdfMergeFiles = pdfMergeFiles + unique
        }
    }

    fun removeMergeFile(pdfFile: PdfFile) {
        pdfMergeFiles -= pdfFile
    }

    fun clear() {
        pdfMergeFiles = emptyList()
    }

    fun pickMergePdfs(context: Context, pickers: FilePickers) {
        pickers.pickPdfs { uris ->
            viewModelScope.launch {
                val mergeFiles = uris.mapNotNull { uri ->
                    try {
                        PdfCatalogRepository.loadPdfMetadata(context, uri)
                    } catch (_: Exception) {
                        null
                    }
                }

                addMergeFiles(context, mergeFiles)
            }
        }
    }

    fun move(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val list = pdfMergeFiles.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        pdfMergeFiles = list
    }

    fun updatePagesPerSheet(option: PagesPerSheetOption) {
        pagesPerSheetOption = option
    }

    fun openPreview(
        context: Context,
        onReady: (PdfFile) -> Unit
    ) {
        if (pdfMergeFiles.isEmpty()) {
            showToast(context, R.string.merge_add_pdf_first)
            return
        }
        if (isPreparingPreview) return

        val snapshot = pdfMergeFiles
        val pagesPerSheetSnapshot = pagesPerSheetOption

        viewModelScope.launch {
            isPreparingPreview = true

            val previewResult = withContext(Dispatchers.IO) {
                runCatching {
                    MergePdfWriter.buildPreviewPdf(
                        context = context,
                        pdfs = snapshot,
                        pagesPerSheet = pagesPerSheetSnapshot
                    )
                }
            }

            val preview = previewResult.getOrNull()
            if (preview == null) {
                isPreparingPreview = false
                val reason = previewResult.exceptionOrNull()?.message
                    ?.takeIf { it.isNotBlank() }
                    ?: context.getString(R.string.error_unknown)
                showToast(context, R.string.merge_preview_failed_format, reason)
                return@launch
            }

            val previewPdf = withContext(Dispatchers.IO) { loadPreviewPdf(context, preview) }

            isPreparingPreview = false
            onReady(previewPdf)
        }
    }

    fun requestMergeExport(
        context: Context,
        onRequest: (PdfOutputRequest) -> Unit
    ) {
        if (pdfMergeFiles.isEmpty()) {
            showToast(context, R.string.merge_add_pdf_first)
            return
        }

        val snapshot = pdfMergeFiles
        val pagesPerSheetSnapshot = pagesPerSheetOption
        val suggestedName = MergePdfWriter.buildSuggestedFileName(
            context = context,
            pdfs = snapshot
        )

        onRequest(
            PdfOutputRequest.SaveFile(
                dialogTitle = context.getString(R.string.merge_save_dialog_title),
                inputLabel = context.getString(R.string.pdflist_file_name_label),
                inputHint = context.getString(R.string.merge_save_dialog_hint),
                confirmLabel = context.getString(R.string.output_choose_location),
                suggestedName = suggestedName,
                processingMessage = context.getString(R.string.output_processing_message),
                successMessage = { it.getString(R.string.merge_export_success) },
                prepareFile = { requestContext, _ ->
                    MergePdfWriter.prepareExportFile(
                        context = requestContext,
                        pdfs = snapshot,
                        pagesPerSheet = pagesPerSheetSnapshot
                    )
                }
            )
        )
    }
}
