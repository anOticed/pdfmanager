package me.notanoticed.pdfmanager.feature.pdflist

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.notanoticed.pdfmanager.core.pdf.PdfRepository
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import kotlinx.coroutines.launch

class PdfListViewModel : ViewModel() {

    /* -------------------- LOADING / DATA -------------------- */
    var isLoading by mutableStateOf(false)
        private set

    var errorText by mutableStateOf<String?>(null)
        private set

    var pdfFiles by mutableStateOf<List<PdfFile>>(emptyList())
        private set

    fun loadAll(context: Context) {
        isLoading = true
        errorText = null

        viewModelScope.launch {
            isLoading = true
            try {
                val files = PdfRepository.loadAllPdfs(context)
                pdfFiles = files
                errorText = null
            } catch (_: SecurityException) {
                errorText = "No permission to access storage"
            } catch (exception: Exception) {
                errorText = exception.message ?: "Failed to load PDFs"
            } finally {
                isLoading = false
            }
        }
    }
    /* -------------------------------------------------------- */



    /* -------------------- PERMISSIONS -------------------- */
    fun requestAllFilesAccess(
        context: Context,
        manageAllFilesLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
    ) {
        if (!Environment.isExternalStorageManager()
        ) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            manageAllFilesLauncher.launch(intent)
        }
    }
    /* ------------------------------------------------------------- */



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
            else -> Unit
        }
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

    fun isSelected(pdf: PdfFile): Boolean {
        return(selectedPdfFiles.contains(pdf))
    }

    fun onItemLongPress(pdf: PdfFile) {
        selectionMode = true
        selectedPdfFiles = selectedPdfFiles + pdf
    }

    fun onItemClick(pdf: PdfFile) {
        if (!isSelectionMode) return

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

    fun mergeSelected() {
        if (selectedPdfFiles.isEmpty()) return

        pendingEvent = PdfListEvent.OpenMerge(selectedPdfFiles.toList())
    }
    fun shareSelected() { /* TODO */ }
    fun deleteSelectedPdfs() { /* TODO */ }
    /* --------------------------------------------------- */
}