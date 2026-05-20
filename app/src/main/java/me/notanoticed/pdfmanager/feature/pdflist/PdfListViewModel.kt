/**
 * ViewModel for the PDF list tab.
 *
 * Owns the loaded PDF list, selection mode, and the state for file option overlays.
 * It also exposes one-shot navigation/events (pendingEvent) consumed by App-level code.
 */

package me.notanoticed.pdfmanager.feature.pdflist

import android.content.Context
import me.notanoticed.pdfmanager.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import me.notanoticed.pdfmanager.core.pdf.PdfRepository
import me.notanoticed.pdfmanager.core.pdf.PdfDocumentActions
import me.notanoticed.pdfmanager.core.pdf.PdfPasswordActionResult
import me.notanoticed.pdfmanager.core.pdf.model.PdfDocumentMetadata
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.notanoticed.pdfmanager.core.toast.ToastBindable

class PdfListViewModel : ViewModel(), ToastBindable {
    enum class PasswordDialogMode {
        SET,
        REMOVE
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

    private fun showPluralToast(
        context: Context,
        messageRes: Int,
        quantity: Int,
        vararg args: Any
    ) {
        showToast(
            context.resources.getQuantityString(messageRes, quantity, *args)
        )
    }

    /* -------------------- LOADING / DATA -------------------- */
    var isLoading by mutableStateOf(false)
        private set

    private var loadJob: Job? = null
    private var loadRevision = 0L

    var pdfFiles by mutableStateOf<List<PdfFile>>(emptyList())
        private set

    var visiblePdfFiles by mutableStateOf<List<PdfFile>>(emptyList())
        private set

    fun loadAll(context: Context) {
        loadJob?.cancel()
        val revision = ++loadRevision

        loadJob = viewModelScope.launch {
            isLoading = true
            try {
                val initial = PdfRepository.loadAllPdfs(
                    context = context,
                    renderMissingVisuals = false
                )

                if (revision != loadRevision) return@launch
                applyPdfFiles(initial)
                isLoading = false

                initial.forEach { pdf ->
                    if (revision != loadRevision) return@launch
                    if (hasCompleteVisual(pdf)) return@forEach

                    val updated = try {
                        PdfRepository.enrichPdfVisual(context, pdf)
                    } catch (_: Exception) {
                        null
                    } ?: return@forEach

                    if (revision != loadRevision) return@launch
                    applyVisualUpdate(updated)
                }
            } catch (_: Exception) {
                /* ignore */
            } finally {
                if (revision == loadRevision) {
                    isLoading = false
                }
            }
        }
    }

    private fun applyPdfFiles(newFiles: List<PdfFile>) {
        val selectedUris = selectedPdfFiles.map { it.uri }.toHashSet()

        pdfFiles = newFiles

        selectedPdfFiles = if (selectedUris.isEmpty()) {
            emptySet()
        } else {
            newFiles.filter { it.uri in selectedUris }.toSet()
        }

        recomputeVisiblePdfFiles()
    }

    private fun hasCompleteVisual(pdf: PdfFile): Boolean {
        val hasThumbnail = !pdf.isLocked &&
            pdf.thumbnailBitmap != null &&
            !pdf.thumbnailBitmap.isRecycled

        val hasKnownPages = pdf.pagesCount > 0
        return pdf.isLocked || (hasThumbnail && hasKnownPages)
    }

    private fun applyVisualUpdate(updated: PdfFile) {
        val old = pdfFiles.firstOrNull { it.uri == updated.uri } ?: return
        if (!isVisualChanged(old, updated)) return

        pdfFiles = pdfFiles.map { item ->
            if (item.uri == updated.uri) updated else item
        }

        visiblePdfFiles = visiblePdfFiles.map { item ->
            if (item.uri == updated.uri) updated else item
        }

        if (selectedPdfFiles.any { it.uri == updated.uri }) {
            selectedPdfFiles = selectedPdfFiles
                .filterNot { it.uri == updated.uri }
                .toSet() + updated
        }
    }

    private fun isVisualChanged(old: PdfFile, new: PdfFile): Boolean {
        val oldThumb = old.thumbnailBitmap?.takeUnless { it.isRecycled }
        val newThumb = new.thumbnailBitmap?.takeUnless { it.isRecycled }

        return old.isLocked != new.isLocked ||
            old.pagesCount != new.pagesCount ||
            oldThumb !== newThumb
    }
    /* -------------------------------------------------------- */



    /* -------------------- SORTING & SEARCH -------------------- */
    private var searchMode by mutableStateOf(false)
    val isSearchMode: Boolean get() = searchMode

    var searchQuery by mutableStateOf("")
        private set

    var sortType by mutableStateOf(PdfSortType.DATE)
        private set

    var sortOrder by mutableStateOf(PdfSortOrder.DESCENDING)
        private set

    private fun recomputeVisiblePdfFiles() {
        val query = searchQuery.trim()
        val sorted = when (sortType) {
            PdfSortType.NAME -> pdfFiles.sortedBy { it.name.lowercase() }
            PdfSortType.FILE_SIZE -> pdfFiles.sortedBy { it.sizeBytes }
            PdfSortType.DATE -> pdfFiles.sortedBy { it.lastModifiedEpochSeconds }
        }

        val ordered = when (sortOrder) {
            PdfSortOrder.ASCENDING -> sorted
            PdfSortOrder.DESCENDING -> sorted.asReversed()
        }

        visiblePdfFiles = if (query.isEmpty()) {
            ordered
        } else {
            ordered.filter { pdf -> pdf.name.contains(query, ignoreCase = true) }
        }
    }

    fun openSearch() {
        searchMode = true
        recomputeVisiblePdfFiles()
    }

    fun closeSearch() {
        searchMode = false
        searchQuery = ""
        recomputeVisiblePdfFiles()
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        recomputeVisiblePdfFiles()
    }

    fun updateSortType(type: PdfSortType) {
        sortType = type
        recomputeVisiblePdfFiles()
    }

    fun updateSortOrder(order: PdfSortOrder) {
        sortOrder = order
        recomputeVisiblePdfFiles()
    }
    /* ---------------------------------------------------------- */



    /* -------------------- OPTIONS PANEL -------------------- */
    var optionsPanelVisible by mutableStateOf(false)
    var optionsPanelPdf: PdfFile? by mutableStateOf(null)

    fun openOptions(pdf: PdfFile) {
        optionsPanelVisible = true
        optionsPanelPdf = pdf
    }

    fun closeOptions() {
        optionsPanelVisible = false
        optionsPanelPdf = null
    }

    fun onFileOptionSelected(action: PdfFileOptionAction, pdf: PdfFile) {
        pendingEvent = when (action) {
            PdfFileOptionAction.RENAME -> PdfListEvent.OpenRenameDialog(pdf)
            PdfFileOptionAction.EDIT_METADATA -> PdfListEvent.OpenMetadataDialog(pdf)
            PdfFileOptionAction.MERGE -> PdfListEvent.OpenMerge(listOf(pdf))
            PdfFileOptionAction.SPLIT -> PdfListEvent.OpenSplit(pdf)
            PdfFileOptionAction.COMPRESS -> PdfListEvent.OpenCompressDialog(pdf)
            PdfFileOptionAction.REORDER_PAGES -> PdfListEvent.OpenPageEditor(pdf)
            PdfFileOptionAction.SET_PASSWORD -> PdfListEvent.OpenSetPasswordDialog(pdf)
            PdfFileOptionAction.REMOVE_PASSWORD -> PdfListEvent.OpenRemovePasswordDialog(pdf)
            PdfFileOptionAction.PRINT -> PdfListEvent.PrintPdf(pdf)
            PdfFileOptionAction.SHARE -> PdfListEvent.SharePdf(pdf)
            PdfFileOptionAction.DETAILS -> PdfListEvent.OpenDetails(pdf)
            PdfFileOptionAction.DELETE -> PdfListEvent.OpenDeleteDialog(listOf(pdf))
            else -> null
        }
    }
    /* ------------------------------------------------------- */



    /* -------------------- FILE ACTION DIALOGS -------------------- */
    var renameDialogVisible by mutableStateOf(false)
        private set

    var renameDialogPdf: PdfFile? by mutableStateOf(null)
        private set

    var renameInput by mutableStateOf("")
        private set

    var deleteDialogVisible by mutableStateOf(false)
        private set

    var deleteDialogPdfs by mutableStateOf<List<PdfFile>>(emptyList())
        private set

    var metadataDialogVisible by mutableStateOf(false)
        private set

    var metadataDialogPdf: PdfFile? by mutableStateOf(null)
        private set

    var metadataTitleInput by mutableStateOf("")
        private set

    var metadataAuthorInput by mutableStateOf("")
        private set

    var metadataSubjectInput by mutableStateOf("")
        private set

    var metadataKeywordsInput by mutableStateOf("")
        private set

    var passwordDialogMode: PasswordDialogMode? by mutableStateOf(null)
        private set

    var passwordDialogPdf: PdfFile? by mutableStateOf(null)
        private set

    var passwordPrimaryInput by mutableStateOf("")
        private set

    var passwordConfirmInput by mutableStateOf("")
        private set

    var isMetadataLoading by mutableStateOf(false)
        private set

    var isFileActionInProgress by mutableStateOf(false)
        private set

    private var originalMetadata = PdfDocumentMetadata()

    fun showRenameDialog(pdf: PdfFile) {
        renameDialogPdf = pdf
        renameInput = pdf.name
        renameDialogVisible = true
    }

    fun closeRenameDialog() {
        renameDialogVisible = false
        renameDialogPdf = null
        renameInput = ""
    }

    fun updateRenameInput(value: String) {
        renameInput = value
    }

    fun showMetadataDialog(context: Context, pdf: PdfFile) {
        metadataDialogPdf = pdf
        metadataDialogVisible = true
        isMetadataLoading = true
        isFileActionInProgress = false
        originalMetadata = PdfDocumentMetadata()
        metadataTitleInput = ""
        metadataAuthorInput = ""
        metadataSubjectInput = ""
        metadataKeywordsInput = ""

        viewModelScope.launch {
            val metadata = withContext(Dispatchers.IO) {
                runCatching {
                    PdfDocumentActions.readPdfMetadata(
                        context = context,
                        pdfUri = pdf.uri
                    )
                }.getOrNull()
            }

            isMetadataLoading = false

            if (metadata == null) {
                closeMetadataDialog()
                showToast(context, R.string.pdflist_metadata_load_failed)
                return@launch
            }

            originalMetadata = metadata.normalized()
            metadataTitleInput = metadata.title
            metadataAuthorInput = metadata.author
            metadataSubjectInput = metadata.subject
            metadataKeywordsInput = metadata.keywords
        }
    }

    fun closeMetadataDialog() {
        metadataDialogVisible = false
        metadataDialogPdf = null
        metadataTitleInput = ""
        metadataAuthorInput = ""
        metadataSubjectInput = ""
        metadataKeywordsInput = ""
        isMetadataLoading = false
        originalMetadata = PdfDocumentMetadata()
    }

    fun updateMetadataTitle(value: String) {
        metadataTitleInput = value
    }

    fun updateMetadataAuthor(value: String) {
        metadataAuthorInput = value
    }

    fun updateMetadataSubject(value: String) {
        metadataSubjectInput = value
    }

    fun updateMetadataKeywords(value: String) {
        metadataKeywordsInput = value
    }

    fun showSetPasswordDialog(pdf: PdfFile) {
        passwordDialogMode = PasswordDialogMode.SET
        passwordDialogPdf = pdf
        passwordPrimaryInput = ""
        passwordConfirmInput = ""
        isFileActionInProgress = false
    }

    fun showRemovePasswordDialog(pdf: PdfFile) {
        passwordDialogMode = PasswordDialogMode.REMOVE
        passwordDialogPdf = pdf
        passwordPrimaryInput = ""
        passwordConfirmInput = ""
        isFileActionInProgress = false
    }

    fun closePasswordDialog() {
        passwordDialogMode = null
        passwordDialogPdf = null
        passwordPrimaryInput = ""
        passwordConfirmInput = ""
    }

    fun updatePasswordPrimaryInput(value: String) {
        passwordPrimaryInput = value
    }

    fun updatePasswordConfirmInput(value: String) {
        passwordConfirmInput = value
    }

    fun confirmPasswordAction(context: Context) {
        val pdf = passwordDialogPdf ?: return
        val mode = passwordDialogMode ?: return
        if (isFileActionInProgress) return

        when (mode) {
            PasswordDialogMode.SET -> {
                val password = passwordPrimaryInput.trim()
                val confirmation = passwordConfirmInput.trim()

                if (password.isBlank()) {
                    showToast(context, R.string.pdflist_password_enter_first)
                    return
                }

                if (password != confirmation) {
                    showToast(context, R.string.pdflist_passwords_do_not_match)
                    return
                }

                viewModelScope.launch {
                    isFileActionInProgress = true

                    val updated = withContext(Dispatchers.IO) {
                        PdfDocumentActions.setPdfPassword(
                            context = context,
                            pdf = pdf,
                            password = password
                        )
                    }

                    isFileActionInProgress = false

                    if (!updated) {
                        showToast(context, R.string.pdflist_password_set_failed)
                        return@launch
                    }

                    closePasswordDialog()
                    showToast(context, R.string.pdflist_password_set_success)
                    loadAll(context)
                }
            }

            PasswordDialogMode.REMOVE -> {
                val password = passwordPrimaryInput.trim()

                if (password.isBlank()) {
                    showToast(context, R.string.pdflist_password_enter_current)
                    return
                }

                viewModelScope.launch {
                    isFileActionInProgress = true

                    val result = withContext(Dispatchers.IO) {
                        PdfDocumentActions.removePdfPassword(
                            context = context,
                            pdf = pdf,
                            currentPassword = password
                        )
                    }

                    isFileActionInProgress = false

                    when (result) {
                        PdfPasswordActionResult.SUCCESS -> {
                            closePasswordDialog()
                            showToast(context, R.string.pdflist_password_remove_success)
                            loadAll(context)
                        }

                        PdfPasswordActionResult.INVALID_PASSWORD -> {
                            showToast(context, R.string.pdflist_password_invalid)
                        }

                        PdfPasswordActionResult.FAILED -> {
                            showToast(context, R.string.pdflist_password_remove_failed)
                        }
                    }
                }
            }
        }
    }

    fun confirmMetadataUpdate(context: Context) {
        val pdf = metadataDialogPdf ?: return
        if (isMetadataLoading || isFileActionInProgress) return

        val updatedMetadata = PdfDocumentMetadata(
            title = metadataTitleInput,
            author = metadataAuthorInput,
            subject = metadataSubjectInput,
            keywords = metadataKeywordsInput
        ).normalized()

        if (updatedMetadata == originalMetadata) {
            closeMetadataDialog()
            return
        }

        viewModelScope.launch {
            isFileActionInProgress = true

            val updated = withContext(Dispatchers.IO) {
                PdfDocumentActions.updatePdfMetadata(
                    context = context,
                    pdf = pdf,
                    metadata = updatedMetadata
                )
            }

            isFileActionInProgress = false

            if (!updated) {
                showToast(context, R.string.pdflist_metadata_update_failed)
                return@launch
            }

            closeMetadataDialog()
            showToast(context, R.string.pdflist_metadata_update_success)
            loadAll(context)
        }
    }

    fun confirmRename(context: Context) {
        val pdf = renameDialogPdf ?: return
        if (isFileActionInProgress) return

        val targetName = PdfDocumentActions.normalizeDisplayName(
            rawName = renameInput,
            fallbackName = pdf.name
        )

        if (targetName == pdf.name) {
            closeRenameDialog()
            return
        }

        viewModelScope.launch {
            isFileActionInProgress = true

            val renamed = withContext(Dispatchers.IO) {
                PdfDocumentActions.renamePdf(context, pdf, targetName)
            }

            isFileActionInProgress = false

            if (!renamed) {
                showToast(context, R.string.pdflist_rename_failed)
                return@launch
            }

            closeRenameDialog()
            showToast(context, R.string.pdflist_rename_success)
            loadAll(context)
        }
    }

    fun showDeleteDialog(pdfs: List<PdfFile>) {
        if (pdfs.isEmpty()) return

        deleteDialogPdfs = pdfs
        deleteDialogVisible = true
    }

    fun closeDeleteDialog() {
        deleteDialogVisible = false
        deleteDialogPdfs = emptyList()
    }

    fun confirmDelete(context: Context) {
        val pdfs = deleteDialogPdfs
        if (pdfs.isEmpty()) return
        if (isFileActionInProgress) return

        viewModelScope.launch {
            isFileActionInProgress = true

            val deletedCount = withContext(Dispatchers.IO) {
                pdfs.count { pdf ->
                    PdfDocumentActions.deletePdf(context, pdf)
                }
            }

            isFileActionInProgress = false

            if (deletedCount <= 0) {
                showPluralToast(
                    context = context,
                    messageRes = R.plurals.pdflist_delete_failed,
                    quantity = pdfs.size
                )
                return@launch
            }

            closeDeleteDialog()
            if (detailsPanelPdf?.uri in pdfs.map { it.uri }.toSet()) {
                closeDetails()
            }

            if (deletedCount == pdfs.size) {
                showPluralToast(
                    context = context,
                    messageRes = R.plurals.pdflist_delete_success,
                    quantity = pdfs.size
                )
                exitSelectionMode()
            } else {
                showToast(
                    context,
                    R.string.pdflist_delete_partial_success,
                    deletedCount,
                    pdfs.size
                )
            }

            loadAll(context)
        }
    }
    /* ------------------------------------------------------------ */



    /* -------------------- DETAILS PANEL -------------------- */
    var detailsPanelVisible by mutableStateOf(false)
        private set

    var detailsPanelPdf: PdfFile? by mutableStateOf(null)
        private set

    fun openDetails(pdf: PdfFile) {
        detailsPanelVisible = true
        detailsPanelPdf = pdf
    }

    fun closeDetails() {
        detailsPanelVisible = false
        detailsPanelPdf = null
    }
    /* ------------------------------------------------------- */



    /* -------------------- EVENTS -------------------- */
    var pendingEvent: PdfListEvent? by mutableStateOf(null)
        private set

    fun clearPendingEvent() {
        pendingEvent = null
    }
    /* ------------------------------------------------ */



    /* -------------------- SELECTION -------------------- */
    var selectedPdfFiles by mutableStateOf<Set<PdfFile>>(mutableSetOf())
        private set

    private var selectionMode by mutableStateOf(false)
    val isSelectionMode: Boolean get() = selectionMode

    val selectionCount: Int get() = selectedPdfFiles.size
    val isAllSelected: Boolean
        get() = selectedPdfFiles.size == pdfFiles.size && pdfFiles.isNotEmpty()

    val canMergeSelected: Boolean
        get() = selectedPdfFiles.isNotEmpty() && selectedPdfFiles.none { it.isLocked }

    fun isSelected(pdf: PdfFile): Boolean {
        return(selectedPdfFiles.contains(pdf))
    }

    fun onItemLongPress(pdf: PdfFile) {
        selectionMode = true
        selectedPdfFiles = selectedPdfFiles + pdf
    }

    fun onItemClick(pdf: PdfFile) {
        if (!isSelectionMode) {
            pendingEvent = PdfListEvent.OpenPreview(pdf)
            return
        }

        selectedPdfFiles = if (selectedPdfFiles.contains(pdf)) {
            selectedPdfFiles - pdf
        } else {
            selectedPdfFiles + pdf
        }
    }

    fun toggleSelectAll() {
        selectedPdfFiles = if (isAllSelected) {
            emptySet()
        } else {
            pdfFiles.toSet()
        }
    }

    fun exitSelectionMode() {
        selectedPdfFiles = emptySet()
        selectionMode = false
    }

    fun enterSelectionMode() {
        closeSearch()
        selectionMode = true
    }

    fun mergeSelected() {
        if (!canMergeSelected) return

        pendingEvent = PdfListEvent.OpenMerge(selectedPdfFiles.toList())
    }

    fun shareSelected() {
        if (selectedPdfFiles.isEmpty()) return

        pendingEvent = PdfListEvent.SharePdfs(selectedPdfFiles.toList())
    }

    fun deleteSelectedPdfs() {
        if (selectedPdfFiles.isEmpty()) return

        pendingEvent = PdfListEvent.OpenDeleteDialog(selectedPdfFiles.toList())
    }
    /* --------------------------------------------------- */
}

enum class PdfSortType {
    NAME,
    FILE_SIZE,
    DATE
}

enum class PdfSortOrder {
    ASCENDING,
    DESCENDING
}
