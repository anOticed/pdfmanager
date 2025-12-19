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

const val SPLIT_METHOD_RANGES = 0
const val SPLIT_METHOD_ONE_PAGE_PER_FILE = 1
const val SPLIT_METHOD_EVERY_N_PAGES = 2

class SplitViewModel : ViewModel(), ToastBindable {
    var selectedSplitPdf: PdfFile? by mutableStateOf(null)
        private set

    var selectedSplitMethodId by mutableStateOf(SPLIT_METHOD_RANGES) // 0: ranges, 1: one page per file, 2: every N pages
    var splitRangesText by mutableStateOf("")
    var splitPagesPerFileText by mutableStateOf("")

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
    }

    fun closeSelectedSplitPdf() {
        selectedSplitPdf = null
        selectSplitMethod(SPLIT_METHOD_RANGES)
    }

    fun selectSplitMethod(method: Int) {
        selectedSplitMethodId = method
    }

    fun updateSplitRangesText(text: String) {
        splitRangesText = text
    }

    fun updateSplitPagesPerFileText(text: String) {
        splitPagesPerFileText = text
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
}