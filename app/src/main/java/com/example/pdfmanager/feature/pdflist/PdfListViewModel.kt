package com.example.pdfmanager.feature.pdflist

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
import com.example.pdfmanager.core.pdf.PdfRepository
import com.example.pdfmanager.core.pdf.model.PdfFile
import kotlinx.coroutines.launch

class PdfListViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set

    var errorText by mutableStateOf<String?>(null)
        private set

    var pdfFiles by mutableStateOf<List<PdfFile>>(emptyList())
        private set

    var optionsPanelVisible by mutableStateOf(false)
    var optionsPanelPdf: PdfFile? by mutableStateOf(null)


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

    fun openOptions(pdf: PdfFile) {
        optionsPanelVisible = true
        optionsPanelPdf = pdf
    }

    fun closeOptions() {
        optionsPanelVisible = false
        optionsPanelPdf = null
    }

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
}