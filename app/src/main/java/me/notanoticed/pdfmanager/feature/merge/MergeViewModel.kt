/**
 * ViewModel for the Merge tab.
 *
 * Stores selected PDFs, supports reorder/removal and provides
 * temporary merged-preview generation (PDF -> single preview PDF).
 */

package me.notanoticed.pdfmanager.feature.merge

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.multipdf.LayerUtility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.notanoticed.pdfmanager.core.pdf.PdfDocumentActions
import me.notanoticed.pdfmanager.core.pdf.PdfPageSource
import me.notanoticed.pdfmanager.core.pdf.PagesPerSheetOption
import me.notanoticed.pdfmanager.core.pdf.PdfRepository
import me.notanoticed.pdfmanager.core.pdf.appendPdfSheet
import me.notanoticed.pdfmanager.core.pdf.copyFileToUri
import me.notanoticed.pdfmanager.core.pdf.createTempPdfFile
import me.notanoticed.pdfmanager.core.pdf.ensurePdfBoxInitialized
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.pdf.prepareGeneratedPdfFile
import me.notanoticed.pdfmanager.core.pickers.Pickers
import me.notanoticed.pdfmanager.core.toast.ToastBindable
import me.notanoticed.pdfmanager.feature.export.PdfOutputRequest
import java.io.File

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

    fun addMergeFiles(pdfFiles: List<PdfFile>) {
        if (pdfFiles.any { it.isLocked }) {
            when (pdfFiles.size) {
                1 -> showToast("This PDF is password-protected and can't be selected")
                else -> showToast("Some PDFs are password-protected and can't be selected")
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

    fun pickMergePdfs(context: Context, pickers: Pickers) {
        pickers.pickPdfs { uris ->
            viewModelScope.launch {
                val mergeFiles = uris.mapNotNull { uri ->
                    try {
                        PdfRepository.loadPdfMetadata(context, uri)
                    } catch (_: Exception) {
                        null
                    }
                }

                addMergeFiles(mergeFiles)
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
            showToast("Add at least one PDF first")
            return
        }
        if (isPreparingPreview) return

        val snapshot = pdfMergeFiles
        val pagesPerSheetSnapshot = pagesPerSheetOption

        viewModelScope.launch {
            isPreparingPreview = true

            val previewResult = withContext(Dispatchers.IO) {
                runCatching {
                    buildMergedPreviewPdf(
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
                    ?: "Unknown error"
                showToast("Failed to prepare merge preview: $reason")
                return@launch
            }

            val previewPdf = withContext(Dispatchers.IO) {
                runCatching {
                    PdfRepository.loadPdfMetadata(context, preview.uri)
                }.getOrElse {
                    val now = System.currentTimeMillis() / 1000L
                    PdfFile(
                        uri = preview.uri,
                        name = preview.name,
                        sizeBytes = preview.sizeBytes,
                        pagesCount = preview.pagesCount,
                        storagePath = preview.uri.toString(),
                        lastModifiedEpochSeconds = now,
                        createdEpochSeconds = now,
                        isLocked = false
                    )
                }
            }

            isPreparingPreview = false
            onReady(previewPdf)
        }
    }

    fun requestMergeExport(
        onRequest: (PdfOutputRequest) -> Unit
    ) {
        if (pdfMergeFiles.isEmpty()) {
            showToast("Add at least one PDF first")
            return
        }

        val snapshot = pdfMergeFiles
        val pagesPerSheetSnapshot = pagesPerSheetOption
        val suggestedName = buildSuggestedMergedFileName(snapshot)

        onRequest(
            PdfOutputRequest.SaveFile(
                dialogTitle = "Save merged PDF",
                inputLabel = "File name",
                inputHint = "Choose the final file name. You'll pick the save location in the next step.",
                confirmLabel = "Choose Location",
                suggestedName = suggestedName,
                processingMessage = "Processing your file, please wait..."
            ) { context, destinationUri, _ ->
                exportMergedPdf(
                    context = context,
                    pdfs = snapshot,
                    pagesPerSheet = pagesPerSheetSnapshot,
                    destinationUri = destinationUri
                )
                "Merged PDF saved successfully"
            }
        )
    }
}

private data class PreviewPdfResult(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val pagesCount: Int
)

private fun buildMergedPreviewPdf(
    context: Context,
    pdfs: List<PdfFile>,
    pagesPerSheet: PagesPerSheetOption
): PreviewPdfResult {
    require(pdfs.isNotEmpty()) { "No source PDFs selected" }

    ensurePdfBoxInitialized(context)

    val fileName = "merge_preview_${pagesPerSheet.pagesPerSheet}_pages_${System.currentTimeMillis()}.pdf"
    val outputFile = prepareGeneratedPdfFile(
        context = context,
        directoryName = "merge_preview_pdf",
        fileName = fileName,
        cleanupPrefix = "merge_preview_"
    )
    try {
        val outputPages = writeMergedPdfToFile(
            context = context,
            pdfs = pdfs,
            pagesPerSheet = pagesPerSheet,
            outputFile = outputFile
        )

        val previewUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outputFile
        )

        return PreviewPdfResult(
            uri = previewUri,
            name = fileName,
            sizeBytes = outputFile.length().coerceAtLeast(0L),
            pagesCount = outputPages
        )
    } catch (error: Throwable) {
        Log.e("MergePreview", "Failed to build merged preview PDF", error)
        runCatching { outputFile.delete() }
        throw error
    }
}

private fun exportMergedPdf(
    context: Context,
    pdfs: List<PdfFile>,
    pagesPerSheet: PagesPerSheetOption,
    destinationUri: Uri
) {
    val tempFile = createTempPdfFile(
        context = context,
        directoryName = "merge_output_pdf",
        filePrefix = "merge_export_"
    )

    try {
        writeMergedPdfToFile(
            context = context,
            pdfs = pdfs,
            pagesPerSheet = pagesPerSheet,
            outputFile = tempFile
        )
        copyFileToUri(
            context = context,
            sourceFile = tempFile,
            destinationUri = destinationUri
        )
    } finally {
        runCatching { tempFile.delete() }
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
                    ?: throw IllegalStateException("Failed to open source PDF: ${inputPdf.uri}")

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
                error("Selected PDFs contain no pages")
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

private fun buildSuggestedMergedFileName(
    pdfs: List<PdfFile>
): String {
    val firstName = pdfs.firstOrNull()?.name?.let { name ->
        PdfDocumentActions.normalizeBaseName(
            rawName = name,
            fallbackName = "merged"
        )
    }.orEmpty()

    return if (pdfs.size == 1 && firstName.isNotBlank()) {
        "${firstName}_merged.pdf"
    } else {
        "merged_${System.currentTimeMillis()}.pdf"
    }
}
