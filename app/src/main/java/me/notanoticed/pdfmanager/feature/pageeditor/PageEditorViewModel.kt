package me.notanoticed.pdfmanager.feature.pageeditor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.pdf.cleanupGeneratedPdfFiles
import me.notanoticed.pdfmanager.core.pdf.copyFileToUri
import me.notanoticed.pdfmanager.core.pdf.ensurePdfBoxInitialized
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.toast.ToastBindable
import me.notanoticed.pdfmanager.feature.export.PdfOutputRequest
import java.io.File
import kotlin.math.min
import kotlin.math.roundToInt

private const val PAGE_EDITOR_THUMBNAIL_MAX_WIDTH_PX = 120
private const val PAGE_EDITOR_THUMBNAIL_MAX_HEIGHT_PX = 168

data class PageEditorPage(
    val id: Long,
    val thumbnailPageIndex: Int
)

private data class PageEditorSession(
    val directory: File,
    val currentFile: File,
    val generatedFiles: List<File>
) {
    fun withCurrent(newCurrentFile: File): PageEditorSession {
        val updatedFiles = (generatedFiles + newCurrentFile)
            .distinctBy { it.absolutePath }
        return copy(
            currentFile = newCurrentFile,
            generatedFiles = updatedFiles
        )
    }
}

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
                    ensurePdfBoxInitialized(context)

                    val sessionPrefix = "page_editor_session_${System.currentTimeMillis()}"
                    val outputDir = File(context.cacheDir, PAGE_EDITOR_DIRECTORY_NAME).apply { mkdirs() }
                    cleanupGeneratedPdfFiles(
                        dir = outputDir,
                        prefix = PAGE_EDITOR_FILE_PREFIX,
                        keepCount = 8
                    )

                    val initialFile = createSessionFile(
                        directory = outputDir,
                        sessionPrefix = sessionPrefix
                    )
                    copyUriToFile(context, pdf.uri, initialFile)

                    val pageCount = readPdfPageCount(initialFile)
                    val session = PageEditorSession(
                        directory = outputDir,
                        currentFile = initialFile,
                        generatedFiles = listOf(initialFile)
                    )
                    val previewPdf = buildPreviewPdf(
                        context = context,
                        sourceName = pdf.name,
                        currentFile = session.currentFile,
                        pageCount = pageCount
                    )

                    Triple(session, pageCount, previewPdf)
                }
            }

            if (revision != openRevision) return@launch

            result.onSuccess { (newSession, pageCount, newPreviewPdf) ->
                val initialPages = List(pageCount) { index ->
                    PageEditorPage(
                        id = index.toLong(),
                        thumbnailPageIndex = index
                    )
                }
                session = newSession
                pages = initialPages
                committedPages = initialPages
                currentPageIndex = 0
                previewPdf = newPreviewPdf
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
            activeSession.generatedFiles
                .distinctBy { it.absolutePath }
                .forEach { file ->
                    runCatching { file.delete() }
                }
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
                applyPdfPageMove(
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
                applyPdfPageDelete(
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
                processingMessage = context.getString(R.string.page_editor_processing_message)
            ) { exportContext, destinationUri, _ ->
                val currentSession = session ?: error(exportContext.getString(R.string.page_editor_open_failed))
                copyFileToUri(
                    context = exportContext,
                    sourceFile = currentSession.currentFile,
                    destinationUri = destinationUri
                )
                exportContext.getString(R.string.page_editor_export_success)
            }
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
                    val outputFile = createSessionFile(
                        directory = activeSession.directory,
                        sessionPrefix = PAGE_EDITOR_FILE_PREFIX + System.currentTimeMillis()
                    )
                    applyChange(activeSession.currentFile, outputFile)
                    val nextSession = activeSession.withCurrent(outputFile)
                    val nextPreviewPdf = buildPreviewPdf(
                        context = context,
                        sourceName = sourceName,
                        currentFile = nextSession.currentFile,
                        pageCount = targetPages.size
                    )
                    nextSession to nextPreviewPdf
                }.onFailure {
                    activeSession.directory
                        .listFiles()
                        ?.filter { file ->
                            file.name.startsWith(PAGE_EDITOR_FILE_PREFIX) &&
                                file.absolutePath !in activeSession.generatedFiles.map { it.absolutePath }
                        }
                        ?.forEach { file -> runCatching { file.delete() } }
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
                previewPdf = buildPreviewPdf(
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

    private fun buildPreviewPdf(
        context: Context,
        sourceName: String,
        currentFile: File,
        pageCount: Int
    ): PdfFile {
        val now = System.currentTimeMillis() / 1000L
        return PdfFile(
            uri = buildFileProviderUri(context, currentFile),
            name = sourceName,
            sizeBytes = currentFile.length().coerceAtLeast(0L),
            pagesCount = pageCount.coerceAtLeast(0),
            storagePath = currentFile.absolutePath,
            lastModifiedEpochSeconds = now,
            createdEpochSeconds = now,
            thumbnailBitmap = null,
            isLocked = false
        )
    }

    private fun buildSuggestedOutputName(
        context: Context,
        sourcePdf: PdfFile
    ): String {
        val baseName = me.notanoticed.pdfmanager.core.pdf.PdfDocumentActions.normalizeBaseName(
            rawName = sourcePdf.name,
            fallbackName = context.getString(R.string.page_editor_output_fallback_name)
        )
        return context.getString(R.string.page_editor_output_name_format, baseName)
    }

    companion object {
        private const val PAGE_EDITOR_DIRECTORY_NAME = "page_editor_pdf"
        private const val PAGE_EDITOR_FILE_PREFIX = "page_editor_session_"

        private fun createSessionFile(
            directory: File,
            sessionPrefix: String
        ): File {
            directory.mkdirs()
            return File(
                directory,
                "${sessionPrefix}_${System.nanoTime()}.pdf"
            )
        }
    }
}

private fun buildFileProviderUri(
    context: Context,
    file: File
): Uri {
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

private fun readPdfPageCount(file: File): Int {
    PDDocument.load(file).use { document ->
        return document.numberOfPages.coerceAtLeast(0)
    }
}

private fun copyUriToFile(
    context: Context,
    sourceUri: Uri,
    outputFile: File
) {
    context.contentResolver.openInputStream(sourceUri)?.use { input ->
        outputFile.outputStream().use { output ->
            input.copyTo(output)
            output.flush()
        }
    } ?: error("Failed to open source PDF")
}

private fun applyPdfPageDelete(
    sourceFile: File,
    outputFile: File,
    pageIndex: Int
) {
    PDDocument.load(sourceFile).use { document ->
        require(document.numberOfPages > 1) { "Cannot delete the last remaining page" }
        document.removePage(pageIndex)
        document.save(outputFile)
    }
}

private fun applyPdfPageMove(
    sourceFile: File,
    outputFile: File,
    fromIndex: Int,
    toIndex: Int
) {
    PDDocument.load(sourceFile).use { document ->
        if (fromIndex == toIndex) {
            document.save(outputFile)
            return
        }

        val pageTree = document.pages
        val movedPage = document.getPage(fromIndex)
        pageTree.remove(movedPage)

        when {
            toIndex <= 0 -> {
                pageTree.insertBefore(movedPage, document.getPage(0))
            }

            toIndex >= document.numberOfPages -> {
                document.addPage(movedPage)
            }

            else -> {
                pageTree.insertBefore(movedPage, document.getPage(toIndex))
            }
        }

        document.save(outputFile)
    }
}

internal fun renderPdfPageThumbnail(
    file: File,
    pageIndex: Int,
    maxWidthPx: Int = PAGE_EDITOR_THUMBNAIL_MAX_WIDTH_PX,
    maxHeightPx: Int = PAGE_EDITOR_THUMBNAIL_MAX_HEIGHT_PX
): Bitmap? {
    if (!file.exists()) return null

    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
        PdfRenderer(descriptor).use { renderer ->
            if (pageIndex !in 0 until renderer.pageCount) return null

            renderer.openPage(pageIndex).use { page ->
                val sourceWidth = page.width.coerceAtLeast(1)
                val sourceHeight = page.height.coerceAtLeast(1)
                val scale = min(
                    maxWidthPx.toFloat() / sourceWidth.toFloat(),
                    maxHeightPx.toFloat() / sourceHeight.toFloat()
                ).coerceAtMost(1f)

                val outWidth = (sourceWidth * scale).roundToInt().coerceAtLeast(1)
                val outHeight = (sourceHeight * scale).roundToInt().coerceAtLeast(1)
                val bitmap = createBitmap(outWidth, outHeight)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                return bitmap
            }
        }
    }
}
