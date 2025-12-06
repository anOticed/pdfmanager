package me.notanoticed.pdfmanager.core.pdf

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlin.io.use
import kotlin.use

object PdfRepository {
    private val pdfCache = mutableMapOf<Uri, PdfFile>()

    fun getCachedPdf(uri: Uri): PdfFile? {
        return pdfCache[uri]
    }

    fun setCachedPdf(uri: Uri, pdfFile: PdfFile) {
        pdfCache[uri] = pdfFile
    }



    /* -------------------- METADATA LOADER -------------------- */
    suspend fun loadPdfMetadata(context: Context, uri: Uri): PdfFile =
        withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver

            var name = "document.pdf"
            var size = 0L
            var createdEpochSeconds = 0L

            val projection = arrayOf(
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.DATE_ADDED
            )

            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }?.let { index ->
                        name = cursor.getString(index) ?: name
                    }

                    cursor.getColumnIndex(OpenableColumns.SIZE).takeIf { it >= 0 }?.let { index ->
                        size = if (!cursor.isNull(index)) cursor.getLong(index) else 0L
                    }

                    cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED).takeIf { it >= 0 }?.let { index ->
                        if (!cursor.isNull(index)) createdEpochSeconds = (cursor.getLong(index) / 1000L).coerceAtLeast(0L)
                    }
                    if (createdEpochSeconds == 0L) {
                        cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED).takeIf { it >= 0 }?.let { index ->
                            if (!cursor.isNull(index)) createdEpochSeconds = cursor.getLong(index).coerceAtLeast(0L)
                        }
                    }
                    if (createdEpochSeconds == 0L) {
                        cursor.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED).takeIf { it >= 0 }?.let { index ->
                            if (!cursor.isNull(index)) createdEpochSeconds = cursor.getLong(index).coerceAtLeast(0L)
                        }
                    }
                }
            }

            if (createdEpochSeconds == 0L) createdEpochSeconds = System.currentTimeMillis() / 1000L

            var pagesCount = 0
            var locked = false
            try {
                contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        pagesCount = renderer.pageCount
                    }
                }
            } catch (_: Exception) {
                locked = true
            }

            PdfFile(
                uri = uri,
                name = name,
                sizeBytes = size,
                pagesCount = pagesCount,
                createdEpochSeconds = createdEpochSeconds,
                isLocked = locked
            )
        }
    /* --------------------------------------------------------- */



    /* -------------------- DATA / QUERY -------------------- */
    suspend fun loadAllPdfs(context: Context): List<PdfFile> =
        withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver

            val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MIME_TYPE
            )

            val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?"
            val selectionArgs = arrayOf("application/pdf")
            val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

            val tasks = mutableListOf<Deferred<PdfFile>>()

            contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val uri: Uri = ContentUris.withAppendedId(collection, id)
                    val name = cursor.getString(nameColumn)
                    val size = cursor.getLong(sizeColumn)
                    val createdEpoch = cursor.getLong(dateColumn)

                    tasks += async {
                        var pdfFile = getCachedPdf(uri)

                        if (pdfFile == null) {
                            var pagesCount = -1
                            var locked = false
                            var bitmap: Bitmap? = null

                            try {
                                contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                                    PdfRenderer(descriptor).use { renderer ->
                                        pagesCount = renderer.pageCount

                                        /* TODO: Render first page to bitmap */
//                                        if (pagesCount > 0) {
//                                            renderer.openPage(0).use { page ->
//                                                val pageWidth = page.width
//                                                val pageHeight = page.height
//
//                                                val scale = 100f / pageWidth.toFloat()
//                                                val width = (pageWidth * scale).toInt()
//                                                val height = (pageHeight * scale).toInt()
//
//                                                val pageBitmap = Bitmap.createBitmap(
//                                                    width,
//                                                    height,
//                                                    Bitmap.Config.ARGB_8888
//                                                )
//
//                                                pageBitmap.eraseColor(Color.White.toArgb())
//                                                page.render(
//                                                    pageBitmap,
//                                                    null,
//                                                    null,
//                                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
//                                                )
//                                                bitmap = pageBitmap
//                                            }
//                                        }
                                    }
                                }
                            } catch (_: Exception) {
                                locked = true
                            }

                            pdfFile = PdfFile(
                                uri = uri,
                                name = name,
                                sizeBytes = size,
                                pagesCount = pagesCount,
                                createdEpochSeconds = createdEpoch,
                                bitmap = bitmap,
                                isLocked = locked
                            )
                            setCachedPdf(uri, pdfFile)
                        }

                        pdfFile
                    }
                }
            }

            tasks.awaitAll()
        }
    /* ------------------------------------------------------ */
}