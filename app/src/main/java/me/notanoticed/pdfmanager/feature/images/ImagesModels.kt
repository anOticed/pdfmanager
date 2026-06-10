package me.notanoticed.pdfmanager.feature.images

import android.net.Uri

data class ImageItem(
    val id: String,
    val uri: Uri,
    val name: String,
    val widthPx: Int,
    val heightPx: Int,
    val sizeBytes: Long
)
