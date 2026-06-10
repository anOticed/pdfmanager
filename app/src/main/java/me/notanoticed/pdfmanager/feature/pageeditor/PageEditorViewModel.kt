package me.notanoticed.pdfmanager.feature.pageeditor

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.pdf.edit.PageEditorSession
import me.notanoticed.pdfmanager.core.pdf.edit.PageEditorSessionStore
import me.notanoticed.pdfmanager.core.pdf.util.PdfFileNamePolicy
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.pdf.write.PageEditorWriter
import me.notanoticed.pdfmanager.core.system.export.PdfExportException
import me.notanoticed.pdfmanager.core.system.export.PreparedPdfFile
import me.notanoticed.pdfmanager.core.system.toast.ToastBindable
import me.notanoticed.pdfmanager.core.system.export.PdfOutputRequest
import java.io.File

class PageEditorViewModel : ViewModel(), ToastBindable {
    var sourcePdf by mutableStateOf<PdfFile?>(null)
        private set

    var pages by mutableStateOf<List<PageEditorPage>>(emptyList())
        private set

    var currentPageIndex by mutableIntStateOf(0)
        private set

    var previewPdf by mutableStateOf<PdfFile?>(null)
        private set

    var previewVersion by mutableLongStateOf(0L)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isApplyingOperation by mutableStateOf(false)
        private set

    private var committedPages: List<PageEditorPage> = emptyList()
    private var session: PageEditorSession? = null
    private var openRevision = 0L
    private var draggingPageId: Long? = null
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

    fun open(
        context: Context,
        pdf: PdfFile
    ) {
        clearSession(incrementRevision = false)
        val revision = ++openRevision
        isLoading = true
        sourcePdf = pdf

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    PageEditorSessionStore.openSession(context, pdf)
                }
            }

            if (revision != openRevision) return@launch

            result.onSuccess { openResult ->
                val initialPages = List(openResult.pageCount) { index ->
                    PageEditorPage(
                        id = index.toLong(),
                        thumbnailPageIndex = index
                    )
                }
                session = openResult.session
                pages = initialPages
                committedPages = initialPages
                currentPageIndex = 0
                previewPdf = openResult.previewPdf
                previewVersion = 1L
                isLoading = false
                isApplyingOperation = false
                draggingPageId = null
            }.onFailure { error ->
                clearSession(incrementRevision = false)
                isLoading = false
                showToast(
                    error.message?.takeIf { it.isNotBlank() }
                        ?: context.getString(R.string.page_editor_open_failed)
                )
            }
        }
    }

    fun closeSession() {
        clearSession(incrementRevision = true)
    }

    private fun clearSession(incrementRevision: Boolean) {
        if (incrementRevision) {
            openRevision += 1
        }
        session?.let { activeSession ->
            PageEditorSessionStore.deleteSession(activeSession)
        }

        sourcePdf = null
        pages = emptyList()
        committedPages = emptyList()
        currentPageIndex = 0
        previewPdf = null
        previewVersion = 0L
        isLoading = false
        isApplyingOperation = false
        session = null
        draggingPageId = null
    }

    fun selectPage(index: Int) {
        if (index !in pages.indices) return
        currentPageIndex = index
    }

    fun movePageInUi(fromIndex: Int, toIndex: Int) {
        if (isApplyingOperation) return
        if (fromIndex == toIndex) return
        if (fromIndex !in pages.indices || toIndex !in pages.indices) return

        if (draggingPageId == null) {
            draggingPageId = pages[fromIndex].id
        }

        val list = pages.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        pages = list

        currentPageIndex = list.indexOfFirst { it.id == item.id }
            .takeIf { it >= 0 }
            ?: currentPageIndex
    }

    fun commitReorder(context: Context) {
        if (isApplyingOperation) return

        val movedPageId = draggingPageId
        draggingPageId = null

        if (movedPageId == null) return

        val fromIndex = committedPages.indexOfFirst { it.id == movedPageId }
        val toIndex = pages.indexOfFirst { it.id == movedPageId }
        if (fromIndex < 0 || toIndex < 0 || fromIndex == toIndex) {
            pages = committedPages
            currentPageIndex = currentPageIndex.coerceIn(0, (committedPages.lastIndex).coerceAtLeast(0))
            return
        }

        val previousPages = committedPages
        val previousCurrentPageIndex = previousPages.indexOfFirst { it.id == movedPageId }
            .takeIf { it >= 0 }
            ?: currentPageIndex

        applyOperation(
            context = context,
            previousPages = previousPages,
            previousCurrentPageIndex = previousCurrentPageIndex,
            applyChange = { sourceFile, outputFile ->
                PageEditorWriter.applyPageMove(
                    sourceFile = sourceFile,
                    outputFile = outputFile,
                    fromIndex = fromIndex,
                    toIndex = toIndex
                )
            }
        )
    }

    fun deleteCurrentPage(context: Context) {
        if (isApplyingOperation) return
        if (pages.size <= 1) {
            showToast(context, R.string.page_editor_delete_last_page_blocked)
            return
        }

        val deleteIndex = currentPageIndex.coerceIn(0, pages.lastIndex)
        val previousPages = committedPages
        val previousCurrentPageIndex = currentPageIndex

        val updatedPages = pages.toMutableList().apply { removeAt(deleteIndex) }
        pages = updatedPages
        currentPageIndex = deleteIndex.coerceAtMost(updatedPages.lastIndex)

        applyOperation(
            context = context,
            previousPages = previousPages,
            previousCurrentPageIndex = previousCurrentPageIndex,
            applyChange = { sourceFile, outputFile ->
                PageEditorWriter.applyPageDelete(
                    sourceFile = sourceFile,
                    outputFile = outputFile,
                    pageIndex = deleteIndex
                )
            }
        )
    }

    fun requestSave(
        context: Context,
        onRequest: (PdfOutputRequest) -> Unit
    ) {
        val activeSourcePdf = sourcePdf ?: return
        session ?: return

        onRequest(
            PdfOutputRequest.SaveFile(
                dialogTitle = context.getString(R.string.page_editor_save_dialog_title),
                inputLabel = context.getString(R.string.pdflist_file_name_label),
                inputHint = context.getString(R.string.page_editor_save_dialog_hint),
                confirmLabel = context.getString(R.string.output_choose_location),
                suggestedName = buildSuggestedOutputName(
                    context = context,
                    sourcePdf = activeSourcePdf
                ),
                processingMessage = context.getString(R.string.page_editor_processing_message),
                successMessage = { it.getString(R.string.page_editor_export_success) },
                prepareFile = { exportContext, _ ->
                    val currentSession = session
                        ?: throw PdfExportException(exportContext.getString(R.string.page_editor_open_failed))
                    PreparedPdfFile.existing(currentSession.currentFile)
                }
            )
        )
    }

    private fun applyOperation(
        context: Context,
        previousPages: List<PageEditorPage>,
        previousCurrentPageIndex: Int,
        applyChange: (sourceFile: File, outputFile: File) -> Unit
    ) {
        val activeSession = session ?: return
        val sourceName = sourcePdf?.name.orEmpty()
        val targetPages = pages
        val targetCurrentPageIndex = currentPageIndex

        isApplyingOperation = true

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val outputFile = PageEditorSessionStore.createNextSessionFile(activeSession)
                    applyChange(activeSession.currentFile, outputFile)
                    val nextSession = activeSession.withCurrent(outputFile)
                    val nextPreviewPdf = PageEditorSessionStore.buildPreviewPdf(
                        context = context,
                        sourceName = sourceName,
                        currentFile = nextSession.currentFile,
                        pageCount = targetPages.size
                    )
                    nextSession to nextPreviewPdf
                }.onFailure {
                    PageEditorSessionStore.cleanupUntrackedGeneratedFiles(activeSession)
                }
            }

            result.onSuccess { (nextSession, nextPreviewPdf) ->
                session = nextSession
                val normalizedPages = targetPages.mapIndexed { index, page ->
                    page.copy(thumbnailPageIndex = index)
                }
                committedPages = normalizedPages
                pages = normalizedPages
                currentPageIndex = targetCurrentPageIndex.coerceIn(0, (normalizedPages.lastIndex).coerceAtLeast(0))
                previewPdf = nextPreviewPdf
                previewVersion += 1L
                isApplyingOperation = false
            }.onFailure { error ->
                pages = previousPages
                committedPages = previousPages
                currentPageIndex = previousCurrentPageIndex.coerceIn(
                    0,
                    (previousPages.lastIndex).coerceAtLeast(0)
                )
                previewPdf = PageEditorSessionStore.buildPreviewPdf(
                    context = context,
                    sourceName = sourceName,
                    currentFile = activeSession.currentFile,
                    pageCount = previousPages.size
                )
                isApplyingOperation = false
                showToast(
                    error.message?.takeIf { it.isNotBlank() }
                        ?: context.getString(R.string.page_editor_apply_failed)
                )
            }
        }
    }

    private fun buildSuggestedOutputName(
        context: Context,
        sourcePdf: PdfFile
    ): String {
        val baseName = PdfFileNamePolicy.normalizeBaseName(
            rawName = sourcePdf.name,
            fallbackName = context.getString(R.string.page_editor_output_fallback_name)
        )
        return context.getString(R.string.page_editor_output_name_format, baseName)
    }
}
