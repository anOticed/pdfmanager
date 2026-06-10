package me.notanoticed.pdfmanager.feature.images

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import me.notanoticed.pdfmanager.R
import java.io.File

object ImageItemReader {
    fun buildImageItem(
        context: Context,
        uri: Uri
    ): ImageItem? {
        val resolver = context.contentResolver

        var name = "${context.getString(R.string.images_item_fallback_name)}_${System.currentTimeMillis()}.jpg"
        var size = 0L

        if (uri.scheme == "file") {
            val localFile = uri.path?.let(::File)
            if (localFile != null && localFile.exists() && localFile.isFile) {
                name = localFile.name.ifBlank { name }
                size = localFile.length().coerceAtLeast(0L)
            }
        } else {
            resolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }?.let { idx ->
                        if (!cursor.isNull(idx)) name = cursor.getString(idx) ?: name
                    }
                    cursor.getColumnIndex(OpenableColumns.SIZE).takeIf { it >= 0 }?.let { idx ->
                        if (!cursor.isNull(idx)) size = cursor.getLong(idx).coerceAtLeast(0L)
                    }
                }
            }
        }

        val bounds = readImageBounds(context, uri) ?: return null

        return ImageItem(
            id = uri.toString(),
            uri = uri,
            name = name,
            widthPx = bounds.first,
            heightPx = bounds.second,
            sizeBytes = size
        )
    }

    private fun readImageBounds(
        context: Context,
        uri: Uri
    ): Pair<Int, Int>? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        val width = options.outWidth
        val height = options.outHeight
        if (width <= 0 || height <= 0) return null

        return width to height
    }
}
