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
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.notanoticed.pdfmanager.core.pdf.PdfPageSource
import me.notanoticed.pdfmanager.core.pdf.PagesPerSheetOption
import me.notanoticed.pdfmanager.core.pdf.PdfRepository
import me.notanoticed.pdfmanager.core.pdf.buildPdfFromPageGroups
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.pickers.Pickers
import me.notanoticed.pdfmanager.core.toast.ToastBindable
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

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

    fun mergePdfs() {
        /* TODO: implement mergePdfs() */
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

    val previewDir = File(context.cacheDir, "merge_preview_pdf")
    if (!previewDir.exists() && !previewDir.mkdirs()) {
        error("Failed to create cache directory for preview")
    }
    cleanupOldPreviewFiles(previewDir, keepCount = 6)

    val fileName = "merge_preview_${pagesPerSheet.pagesPerSheet}_pages_${System.currentTimeMillis()}.pdf"
    val outputFile = File(previewDir, fileName)
    val openedSourceDocuments = mutableListOf<PDDocument>()

    try {
        val pageGroup = buildList {
            pdfs.forEach { inputPdf ->
                val inputStream = context.contentResolver.openInputStream(inputPdf.uri)
                    ?: throw IllegalStateException("Failed to open source PDF: ${inputPdf.uri}")

                val sourceDocument = inputStream.use { stream ->
                    PDDocument.load(stream)
                }
                openedSourceDocuments += sourceDocument

                for (pageIndex in 0 until sourceDocument.numberOfPages) {
                    add(
                        PdfPageSource(
                            document = sourceDocument,
                            pageIndex = pageIndex
                        )
                    )
                }
            }
        }

        if (pageGroup.isEmpty()) {
            error("Selected PDFs contain no pages")
        }

        val outputPages = buildPdfFromPageGroups(
            outputFile = outputFile,
            pageGroups = listOf(pageGroup),
            pagesPerSheet = pagesPerSheet
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
    } finally {
        openedSourceDocuments.forEach { source ->
            runCatching { source.close() }
        }
    }
}

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
        ?.filter { it.isFile && it.name.startsWith("merge_preview_") && it.name.endsWith(".pdf") }
        ?.sortedByDescending { it.lastModified() }
        ?.drop(keepCount)
        ?: return

    staleFiles.forEach { file ->
        runCatching { file.delete() }
    }
}
