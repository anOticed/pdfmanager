package me.notanoticed.pdfmanager.feature.merge

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MergeViewModel : ViewModel() {
    var pdfMergeFiles by mutableStateOf<List<MergeFile>>(emptyList())
    val isActive: Boolean get() = pdfMergeFiles.isNotEmpty()

    val sampleMergeFiles = listOf(
        MergeFile(1, "ucv07.pdf", "1 page • 0.2 MB"),
        MergeFile(2, "db2025_5.pdf", "39 pages • 0.4 MB"),
        MergeFile(3, "ucv07.pdf", "1 page • 0.2 MB"),
        MergeFile(4, "ucv07.pdf", "1 page • 0.2 MB"),
        MergeFile(4, "ucv07.pdf", "1 page • 0.2 MB"),
        MergeFile(4, "ucv07.pdf", "1 page • 0.2 MB"),
        MergeFile(4, "ucv07.pdf", "1 page • 0.2 MB"),
        MergeFile(5, "ucv07.pdf", "1 page • 0.2 MB")
    )

    fun setMergeFiles(files: List<MergeFile>) {
        pdfMergeFiles = files
    }

    fun clear() {
        pdfMergeFiles = emptyList()
    }

    fun move(fromIndex: Int, toIndex:Int) {
        /* TODO: implement move() */
    }

    fun mergePdfs() {
        /* TODO: implement mergePdfs() */
    }
}