package me.notanoticed.pdfmanager.core.pdf.render

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import java.io.File
import kotlin.math.min
import kotlin.math.roundToInt

private const val PAGE_EDITOR_THUMBNAIL_MAX_WIDTH_PX = 120
private const val PAGE_EDITOR_THUMBNAIL_MAX_HEIGHT_PX = 168

fun renderPdfPageThumbnail(
    file: File,
    pageIndex: Int,
    maxWidthPx: Int = PAGE_EDITOR_THUMBNAIL_MAX_WIDTH_PX,
    maxHeightPx: Int = PAGE_EDITOR_THUMBNAIL_MAX_HEIGHT_PX
): Bitmap? {
    if (!file.exists()) return null

    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
        PdfRenderer(descriptor).use { renderer ->
            if (pageIndex !in 0 until renderer.pageCount) return null

            renderer.openPage(pageIndex).use { page ->
                val sourceWidth = page.width.coerceAtLeast(1)
                val sourceHeight = page.height.coerceAtLeast(1)
                val scale = min(
                    maxWidthPx.toFloat() / sourceWidth.toFloat(),
                    maxHeightPx.toFloat() / sourceHeight.toFloat()
                ).coerceAtMost(1f)

                val outWidth = (sourceWidth * scale).roundToInt().coerceAtLeast(1)
                val outHeight = (sourceHeight * scale).roundToInt().coerceAtLeast(1)
                val bitmap = createBitmap(outWidth, outHeight)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                return bitmap
            }
        }
    }
}
