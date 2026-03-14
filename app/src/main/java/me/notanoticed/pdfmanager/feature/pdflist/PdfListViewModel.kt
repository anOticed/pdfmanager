/**
 * ViewModel for the PDF list tab.
 *
 * Owns the loaded PDF list, selection mode, and the state for file option overlays.
 * It also exposes one-shot navigation/events (pendingEvent) consumed by App-level code.
 */

package me.notanoticed.pdfmanager.feature.pdflist

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.notanoticed.pdfmanager.core.pdf.PdfRepository
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch



class PdfListViewModel : ViewModel() {

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
        when(action) {
            PdfFileOptionAction.MERGE -> pendingEvent = PdfListEvent.OpenMerge(listOf(pdf))
            PdfFileOptionAction.SPLIT -> pendingEvent = PdfListEvent.OpenSplit(pdf)
            PdfFileOptionAction.DETAILS -> pendingEvent = PdfListEvent.OpenDetails(pdf)
            else -> Unit
        }
    }
    /* ------------------------------------------------------- */



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
    fun shareSelected() { /* TODO */ }
    fun deleteSelectedPdfs() { /* TODO */ }
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
