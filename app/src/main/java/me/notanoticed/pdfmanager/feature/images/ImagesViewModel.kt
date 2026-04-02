/**
 * ViewModel for the Images tab.
 *
 * Stores selected images, supports reorder/remove/clear and handles:
 * - gallery picking
 * - camera capture
 * - preview generation (images -> temporary PDF)
 */

package me.notanoticed.pdfmanager.feature.images

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.notanoticed.pdfmanager.core.pdf.fitRectIntoSlot
import me.notanoticed.pdfmanager.core.pdf.pageCountForSheets
import me.notanoticed.pdfmanager.core.pdf.PagesPerSheetOption
import me.notanoticed.pdfmanager.core.pdf.PREVIEW_A4_HEIGHT_PX
import me.notanoticed.pdfmanager.core.pdf.PREVIEW_A4_WIDTH_PX
import me.notanoticed.pdfmanager.core.pdf.PdfRepository
import me.notanoticed.pdfmanager.core.pdf.buildSheetLayoutSlots
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.toast.ToastBindable
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/* -------------------- VIEW MODEL -------------------- */
data class ImageItem(
    val id: String,
    val uri: Uri,
    val name: String,
    val widthPx: Int,
    val heightPx: Int,
    val sizeBytes: Long
)

class ImagesViewModel : ViewModel(), ToastBindable {
    var selectedImages: List<ImageItem> by mutableStateOf(emptyList())
        private set

    val isActive: Boolean get() = selectedImages.isNotEmpty()
    val selectedCount: Int get() = selectedImages.size
    var pagesPerSheetOption by mutableStateOf(PagesPerSheetOption.ONE)
        private set

    var isPreparingPreview by mutableStateOf(false)
        private set

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

    fun addFromGallery(context: Context, uris: List<Uri>) {
        if (uris.isEmpty()) return

        viewModelScope.launch {
            val existing = selectedImages.map { it.uri }.toHashSet()
            val uniqueUris = uris.filter { existing.add(it) }

            if (uniqueUris.isEmpty()) {
                showToast("These images are already selected")
                return@launch
            }

            val items = uniqueUris.map { uri ->
                async(Dispatchers.IO) { buildImageItem(context, uri) }
            }.awaitAll().filterNotNull()

            if (items.isEmpty()) {
                showToast("Failed to add selected images")
                return@launch
            }

            selectedImages = selectedImages + items
        }
    }

    fun addCapturedPhoto(
        context: Context,
        uri: Uri
    ) {
        viewModelScope.launch {
            val item = withContext(Dispatchers.IO) {
                buildImageItem(context, uri)
            }

            if (item == null) {
                showToast("Failed to load captured image")
                return@launch
            }

            selectedImages = selectedImages + item
        }
    }

    fun onCameraPermissionDenied() {
        showToast("Camera permission is required to take a photo")
    }

    fun onCameraLaunchFailed() {
        showToast("Failed to open camera")
    }

    fun clear() {
        selectedImages = emptyList()
    }

    fun remove(image: ImageItem) {
        selectedImages -= image
    }

    fun move(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val list = selectedImages.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        selectedImages = list
    }

    fun updatePagesPerSheet(option: PagesPerSheetOption) {
        pagesPerSheetOption = option
    }

    fun openPreview(
        context: Context,
        onReady: (PdfFile) -> Unit
    ) {
        if (selectedImages.isEmpty()) {
            showToast("Add at least one image first")
            return
        }
        if (isPreparingPreview) return

        val snapshot = selectedImages
        val pagesPerSheetSnapshot = pagesPerSheetOption

        viewModelScope.launch {
            isPreparingPreview = true

            val preview = withContext(Dispatchers.IO) {
                buildPreviewPdf(
                    context = context,
                    images = snapshot,
                    pagesPerSheet = pagesPerSheetSnapshot
                )
            }

            if (preview == null) {
                isPreparingPreview = false
                showToast("Failed to generate preview")
                return@launch
            }

            val pdf = withContext(Dispatchers.IO) {
                runCatching {
                    PdfRepository.loadPdfMetadata(context, preview.uri)
                }.getOrElse {
                    val now = System.currentTimeMillis() / 1000L
                    PdfFile(
                        uri = preview.uri,
                        name = preview.name,
                        sizeBytes = preview.sizeBytes,
                        pagesCount = preview.pagesCount,
                        storagePath = preview.uri.toString(),
                        lastModifiedEpochSeconds = now,
                        createdEpochSeconds = now,
                        isLocked = false
                    )
                }
            }

            isPreparingPreview = false
            onReady(pdf)
        }
    }
}
/* ---------------------------------------------------- */


/* -------------------- HELPERS -------------------- */
private data class PreviewPdfResult(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val pagesCount: Int
)

private fun buildImageItem(
    context: Context,
    uri: Uri
): ImageItem? {
    val resolver = context.contentResolver

    var name = "image_${System.currentTimeMillis()}.jpg"
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

private fun buildPreviewPdf(
    context: Context,
    images: List<ImageItem>,
    pagesPerSheet: PagesPerSheetOption
): PreviewPdfResult? {
    if (images.isEmpty()) return null

    val previewDir = File(context.cacheDir, "images_preview_pdf").apply { mkdirs() }
    cleanupOldPreviewFiles(previewDir, keepCount = 6)

    val fileName = "images_preview_${pagesPerSheet.pagesPerSheet}_pages_${System.currentTimeMillis()}.pdf"
    val outputFile = File(previewDir, fileName)
    val pdf = PdfDocument()

    val success = runCatching {
        val pageWidth = PREVIEW_A4_WIDTH_PX
        val pageHeight = PREVIEW_A4_HEIGHT_PX
        val sheetSlots = buildSheetLayoutSlots(
            sheetWidth = pageWidth.toFloat(),
            sheetHeight = pageHeight.toFloat(),
            pagesPerSheet = pagesPerSheet
        )

        images.chunked(pagesPerSheet.pagesPerSheet).forEachIndexed { index, sheetImages ->
            val page = pdf.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            )

            try {
                page.canvas.drawColor(Color.WHITE)

                sheetImages.forEachIndexed { slotIndex, image ->
                    val slot = sheetSlots[slotIndex]
                    val bitmap = decodeBitmapForPdfPage(
                        context = context,
                        uri = image.uri,
                        targetWidth = slot.width.roundToInt(),
                        targetHeight = slot.height.roundToInt()
                    )

                    if (bitmap != null) {
                        val dstRect = fitRectIntoSlot(
                            slot = slot,
                            srcWidth = bitmap.width,
                            srcHeight = bitmap.height
                        )
                        page.canvas.drawBitmap(bitmap, null, dstRect, null)
                        bitmap.recycle()
                    }
                }
            } finally {
                pdf.finishPage(page)
            }
        }

        FileOutputStream(outputFile).use { stream ->
            pdf.writeTo(stream)
        }
        true
    }.getOrElse { false }

    runCatching { pdf.close() }

    if (!success) {
        runCatching { outputFile.delete() }
        return null
    }

    val uri = runCatching {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outputFile
        )
    }.getOrNull() ?: return null

    return PreviewPdfResult(
        uri = uri,
        name = fileName,
        sizeBytes = outputFile.length().coerceAtLeast(0L),
        pagesCount = pageCountForSheets(images.size, pagesPerSheet)
    )
}

private fun decodeBitmapForPdfPage(
    context: Context,
    uri: Uri,
    targetWidth: Int,
    targetHeight: Int
): Bitmap? {
    val bounds = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }

    context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, bounds)
    }

    val sourceWidth = bounds.outWidth
    val sourceHeight = bounds.outHeight
    if (sourceWidth <= 0 || sourceHeight <= 0) return null

    val sourceLongEdge = max(sourceWidth, sourceHeight)
    val targetLongEdge = max(targetWidth, targetHeight).coerceAtLeast(1)
    var sampleSize = 1
    while (sourceLongEdge / sampleSize > targetLongEdge * 2) {
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }

    return context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
    }
}

private fun cleanupOldPreviewFiles(
    dir: File,
    keepCount: Int
) {
    if (!dir.exists() || !dir.isDirectory) return

    val stale = dir.listFiles()
        ?.filter { it.isFile && it.name.startsWith("images_preview_") && it.name.endsWith(".pdf") }
        ?.sortedByDescending { it.lastModified() }
        ?.drop(keepCount)
        ?: return

    stale.forEach { file ->
        runCatching { file.delete() }
    }
}
/* ------------------------------------------------- */
