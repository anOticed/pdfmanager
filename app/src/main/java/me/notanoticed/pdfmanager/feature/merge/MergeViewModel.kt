/**
 * ViewModel for the Merge tab.
 *
 * Holds the list of selected PDFs, supports reordering/removal, and exposes the
 * "active" state used to switch between MergeScreen and MergeActiveScreen.
 */

package me.notanoticed.pdfmanager.feature.merge

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.notanoticed.pdfmanager.core.pdf.PdfRepository
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile

import me.notanoticed.pdfmanager.core.pickers.Pickers
import me.notanoticed.pdfmanager.core.toast.ToastBindable

class MergeViewModel : ViewModel(), ToastBindable {
    var pdfMergeFiles by mutableStateOf<List<PdfFile>>(emptyList())
    val isActive: Boolean get() = pdfMergeFiles.isNotEmpty()
    val total: Int get() = pdfMergeFiles.size

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
                    } catch(_: Exception) {
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

    fun mergePdfs() {
        /* TODO: implement mergePdfs() */
    }
}