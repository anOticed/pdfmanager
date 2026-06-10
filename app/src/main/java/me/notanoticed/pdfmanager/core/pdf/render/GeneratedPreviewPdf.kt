package me.notanoticed.pdfmanager.core.pdf.render

import android.net.Uri

data class GeneratedPreviewPdf(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val pagesCount: Int,
    val storagePath: String
)
