/**
 * ViewModel for the Split tab.
 *
 * Holds the selected PDF and the current split configuration.
 */

package me.notanoticed.pdfmanager.feature.split

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.notanoticed.pdfmanager.core.pdf.PdfRepository
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.pickers.Pickers
import kotlinx.coroutines.launch
import me.notanoticed.pdfmanager.core.toast.ToastBindable

class SplitViewModel : ViewModel(), ToastBindable {
    var selectedSplitPdf: PdfFile? by mutableStateOf(null)
        private set

    val isActive: Boolean get() = selectedSplitPdf != null

    var splitConfiguration by mutableStateOf(SplitConfiguration())
        private set

    val selectedSplitMethod: SplitMethodType
        get() = splitConfiguration.method

    val splitRangesText: String
        get() = splitConfiguration.pageRanges

    val splitPagesPerFileText: String
        get() = splitConfiguration.pagesPerFile

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

    fun updateSelectedSplitPdf(pdfFile: PdfFile) {
        if (pdfFile.isLocked) {
            showToast("This PDF is password-protected and can't be selected")
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

    fun splitPdf(context: Context) {
        /* TODO: implement splitPdf() */
    }

    fun pickSplitPdf(context: Context, pickers: Pickers) {
        pickers.pickPdf { uri ->
            viewModelScope.launch {
                val selectedPdf = PdfRepository.loadPdfMetadata(context, uri)
                updateSelectedSplitPdf(selectedPdf)
            }
        }
    }

    private fun resetSplitConfiguration() {
        splitConfiguration = SplitConfiguration()
    }
}
