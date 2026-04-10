/**
 * PDF metadata + thumbnail repository.
 *
 * - fast load path for list screens
 * - background enrichment path for missing visuals
 * - low-quality thumbnails
 */

package me.notanoticed.pdfmanager.core.pdf

import android.content.ContentUris
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min
import kotlin.math.roundToInt
import androidx.core.graphics.createBitmap
import androidx.core.content.edit

private const val THUMBNAIL_MAX_WIDTH_PX = 96
private const val THUMBNAIL_MAX_HEIGHT_PX = 136
private const val THUMBNAIL_JPEG_QUALITY = 28

private const val THUMBNAIL_CACHE_DIR = "pdf_thumbnail_cache_v1"
private const val VISUAL_META_PREFS = "pdf_visual_meta_v1"
private const val PREF_PAGE_COUNT_PREFIX = "pc_"
private const val PREF_LOCKED_PREFIX = "lk_"

private val previewRenderSemaphore = Semaphore(permits = 1)

private data class PdfDescriptor(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val storagePath: String,
    val createdEpochSeconds: Long,
    val lastModifiedEpochSeconds: Long
)

private data class CachedVisualMeta(
    val pagesCount: Int,
    val isLocked: Boolean
)

private data class PdfVisualData(
    val pagesCount: Int,
    val thumbnailBitmap: Bitmap?,
    val isLocked: Boolean
)

object PdfRepository {
    private val pdfCache = ConcurrentHashMap<Uri, PdfFile>()

    fun getCachedPdf(uri: Uri): PdfFile? = pdfCache[uri]

    fun setCachedPdf(uri: Uri, pdfFile: PdfFile) {
        pdfCache[uri] = pdfFile
    }


    /* -------------------- METADATA LOADER -------------------- */
    suspend fun loadPdfMetadata(context: Context, uri: Uri): PdfFile =
        withContext(Dispatchers.IO) {
            val descriptor = queryPdfDescriptor(context, uri)
            buildPdfFile(
                context = context,
                descriptor = descriptor,
                renderMissingVisuals = true
            )
        }
    /* --------------------------------------------------------- */


    /* -------------------- VISUAL ENRICH BY EXISTING MODEL -------------------- */
    suspend fun enrichPdfVisual(
        context: Context,
        pdf: PdfFile
    ): PdfFile =
        withContext(Dispatchers.IO) {
            val descriptor = PdfDescriptor(
                uri = pdf.uri,
                name = pdf.name,
                sizeBytes = pdf.sizeBytes,
                storagePath = pdf.storagePath,
                createdEpochSeconds = pdf.createdEpochSeconds,
                lastModifiedEpochSeconds = pdf.lastModifiedEpochSeconds
            )
            buildPdfFile(
                context = context,
                descriptor = descriptor,
                renderMissingVisuals = true
            )
        }
    /* ------------------------------------------------------------------------ */


    /* -------------------- DATA / QUERY -------------------- */
    suspend fun loadAllPdfs(
        context: Context,
        renderMissingVisuals: Boolean = false
    ): List<PdfFile> =
        withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.RELATIVE_PATH,
                MediaStore.Files.FileColumns.MIME_TYPE
            )

            val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?"
            val selectionArgs = arrayOf("application/pdf")
            val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

            val result = ArrayList<PdfFile>(256)

            contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val dataColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                val relativePathColumn = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)

                while (cursor.moveToNext()) {
                    coroutineContext.ensureActive()

                    val id = cursor.getLong(idColumn)
                    val uri: Uri = ContentUris.withAppendedId(collection, id)
                    val name = cursor.getString(nameColumn)
                    val size = cursor.getLong(sizeColumn)
                    val createdEpoch = cursor.getLong(dateColumn)
                    val lastModifiedEpoch = cursor.getLong(dateModifiedColumn)

                    val dataPath = if (dataColumn >= 0 && !cursor.isNull(dataColumn)) {
                        cursor.getString(dataColumn)
                    } else null

                    val relativePath = if (relativePathColumn >= 0 && !cursor.isNull(relativePathColumn)) {
                        cursor.getString(relativePathColumn)
                    } else null

                    val storagePath = when {
                        !dataPath.isNullOrBlank() -> dataPath
                        !relativePath.isNullOrBlank() -> {
                            val root = Environment.getExternalStorageDirectory().absolutePath.trimEnd('/')
                            val rel = relativePath.trimStart('/')
                            "$root/$rel$name"
                        }
                        else -> uri.toString()
                    }

                    val resolvedSize = if (size > 0L) {
                        size
                    } else {
                        resolvePdfSize(
                            contentResolver = contentResolver,
                            uri = uri,
                            storagePath = dataPath ?: storagePath
                        )
                    }

                    val descriptor = PdfDescriptor(
                        uri = uri,
                        name = name,
                        sizeBytes = resolvedSize,
                        storagePath = storagePath,
                        createdEpochSeconds = createdEpoch,
                        lastModifiedEpochSeconds = if (lastModifiedEpoch > 0L) lastModifiedEpoch else createdEpoch
                    )

                    result += buildPdfFile(
                        context = context,
                        descriptor = descriptor,
                        renderMissingVisuals = renderMissingVisuals
                    )
                }
            }

            result
        }
    /* ------------------------------------------------------ */


    private suspend fun buildPdfFile(
        context: Context,
        descriptor: PdfDescriptor,
        renderMissingVisuals: Boolean
    ): PdfFile {
        val uri = descriptor.uri
        val lastModified = descriptor.lastModifiedEpochSeconds
        val size = descriptor.sizeBytes

        val cached = getCachedPdf(uri)
        if (cached != null &&
            cached.sizeBytes == size &&
            cached.lastModifiedEpochSeconds == lastModified
        ) {
            val hasValidThumbnail = !cached.isLocked &&
                cached.thumbnailBitmap != null &&
                !cached.thumbnailBitmap.isRecycled
            val hasKnownPages = cached.pagesCount > 0

            if (cached.isLocked || hasValidThumbnail || (!renderMissingVisuals && hasKnownPages)) {
                return cached.copy(
                    name = descriptor.name,
                    storagePath = descriptor.storagePath,
                    createdEpochSeconds = descriptor.createdEpochSeconds,
                    lastModifiedEpochSeconds = descriptor.lastModifiedEpochSeconds,
                    sizeBytes = descriptor.sizeBytes
                )
            }
        }

        val cacheKey = buildVisualCacheKey(
            uri = uri,
            sizeBytes = size,
            lastModifiedEpochSeconds = lastModified
        )

        val meta = readCachedVisualMeta(context, cacheKey)
        if (meta != null) {
            if (meta.isLocked) {
                val lockedFile = PdfFile(
                    uri = uri,
                    name = descriptor.name,
                    sizeBytes = size,
                    pagesCount = 0,
                    storagePath = descriptor.storagePath,
                    createdEpochSeconds = descriptor.createdEpochSeconds,
                    lastModifiedEpochSeconds = descriptor.lastModifiedEpochSeconds,
                    thumbnailBitmap = null,
                    isLocked = true
                )
                setCachedPdf(uri, lockedFile)
                return lockedFile
            }

            val cachedThumbnail = readThumbnailFromDisk(context, cacheKey)
            if (cachedThumbnail != null) {
                val fromDisk = PdfFile(
                    uri = uri,
                    name = descriptor.name,
                    sizeBytes = size,
                    pagesCount = meta.pagesCount.coerceAtLeast(0),
                    storagePath = descriptor.storagePath,
                    createdEpochSeconds = descriptor.createdEpochSeconds,
                    lastModifiedEpochSeconds = descriptor.lastModifiedEpochSeconds,
                    thumbnailBitmap = cachedThumbnail,
                    isLocked = false
                )
                setCachedPdf(uri, fromDisk)
                return fromDisk
            }

            if (!renderMissingVisuals) {
                val noBitmapYet = PdfFile(
                    uri = uri,
                    name = descriptor.name,
                    sizeBytes = size,
                    pagesCount = meta.pagesCount.coerceAtLeast(0),
                    storagePath = descriptor.storagePath,
                    createdEpochSeconds = descriptor.createdEpochSeconds,
                    lastModifiedEpochSeconds = descriptor.lastModifiedEpochSeconds,
                    thumbnailBitmap = null,
                    isLocked = false
                )
                setCachedPdf(uri, noBitmapYet)
                return noBitmapYet
            }
        } else if (!renderMissingVisuals) {
            val lightweight = PdfFile(
                uri = uri,
                name = descriptor.name,
                sizeBytes = size,
                pagesCount = 0,
                storagePath = descriptor.storagePath,
                createdEpochSeconds = descriptor.createdEpochSeconds,
                lastModifiedEpochSeconds = descriptor.lastModifiedEpochSeconds,
                thumbnailBitmap = null,
                isLocked = false
            )
            setCachedPdf(uri, lightweight)
            return lightweight
        }

        val visual = readPdfVisualData(context, uri)
        val normalized = PdfFile(
            uri = uri,
            name = descriptor.name,
            sizeBytes = size,
            pagesCount = if (visual.isLocked) 0 else visual.pagesCount.coerceAtLeast(0),
            storagePath = descriptor.storagePath,
            createdEpochSeconds = descriptor.createdEpochSeconds,
            lastModifiedEpochSeconds = descriptor.lastModifiedEpochSeconds,
            thumbnailBitmap = if (visual.isLocked) null else visual.thumbnailBitmap,
            isLocked = visual.isLocked
        )

        setCachedPdf(uri, normalized)
        writeCachedVisualMeta(
            context = context,
            cacheKey = cacheKey,
            pagesCount = normalized.pagesCount,
            isLocked = normalized.isLocked
        )

        if (!normalized.isLocked) {
            normalized.thumbnailBitmap?.takeUnless { it.isRecycled }?.let { bitmap ->
                writeThumbnailToDisk(context, cacheKey, bitmap)
            }
        }

        return normalized
    }
}

private suspend fun queryPdfDescriptor(
    context: Context,
    uri: Uri
): PdfDescriptor =
    withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver

        var name = "document.pdf"
        var size = 0L
        var createdEpochSeconds = 0L
        var lastModifiedEpochSeconds = 0L
        val storagePath = uri.toString()

        val projection = arrayOf(
            OpenableColumns.DISPLAY_NAME,
            OpenableColumns.SIZE,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.DATE_MODIFIED,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        )

        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }?.let { index ->
                    name = cursor.getString(index) ?: name
                }

                cursor.getColumnIndex(OpenableColumns.SIZE).takeIf { it >= 0 }?.let { index ->
                    size = if (!cursor.isNull(index)) cursor.getLong(index) else 0L
                }

                cursor.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED).takeIf { it >= 0 }?.let { idx ->
                    if (!cursor.isNull(idx)) createdEpochSeconds = cursor.getLong(idx).coerceAtLeast(0L)
                }

                cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED).takeIf { it >= 0 }?.let { idx ->
                    if (!cursor.isNull(idx)) {
                        lastModifiedEpochSeconds = (cursor.getLong(idx) / 1000L).coerceAtLeast(0L)
                    }
                }

                if (lastModifiedEpochSeconds == 0L) {
                    cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED).takeIf { it >= 0 }?.let { idx ->
                        if (!cursor.isNull(idx)) {
                            lastModifiedEpochSeconds = cursor.getLong(idx).coerceAtLeast(0L)
                        }
                    }
                }
            }
        }

        if (size <= 0L) {
            size = resolvePdfSize(
                contentResolver = contentResolver,
                uri = uri,
                storagePath = null
            )
        }

        val now = System.currentTimeMillis() / 1000L
        if (createdEpochSeconds == 0L) {
            createdEpochSeconds = if (lastModifiedEpochSeconds != 0L) lastModifiedEpochSeconds else now
        }
        if (lastModifiedEpochSeconds == 0L) {
            lastModifiedEpochSeconds = createdEpochSeconds
        }

        PdfDescriptor(
            uri = uri,
            name = name,
            sizeBytes = size,
            storagePath = storagePath,
            createdEpochSeconds = createdEpochSeconds,
            lastModifiedEpochSeconds = lastModifiedEpochSeconds
        )
    }

private fun resolvePdfSize(
    contentResolver: ContentResolver,
    uri: Uri,
    storagePath: String?
): Long {
    val assetDescriptorSize = runCatching {
        contentResolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
            descriptor.length.coerceAtLeast(0L)
        } ?: 0L
    }.getOrDefault(0L)

    if (assetDescriptorSize > 0L) return assetDescriptorSize

    val fileDescriptorSize = runCatching {
        contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            descriptor.statSize.coerceAtLeast(0L)
        } ?: 0L
    }.getOrDefault(0L)

    if (fileDescriptorSize > 0L) return fileDescriptorSize

    if (!storagePath.isNullOrBlank()) {
        val fileSize = runCatching {
            File(storagePath)
                .takeIf { it.exists() && it.isFile }
                ?.length()
                ?.coerceAtLeast(0L)
                ?: 0L
        }.getOrDefault(0L)

        if (fileSize > 0L) return fileSize
    }

    return 0L
}

private suspend fun readPdfVisualData(
    context: Context,
    uri: Uri
): PdfVisualData {
    return previewRenderSemaphore.withPermit {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            runCatching {
                PdfRenderer(descriptor).use { renderer ->
                    val count = renderer.pageCount.coerceAtLeast(0)
                    val thumbnail = if (count > 0) {
                        renderer.openPage(0).use { page ->
                            runCatching { renderThumbnail(page) }.getOrNull()
                        }
                    } else {
                        null
                    }

                    PdfVisualData(
                        pagesCount = count,
                        thumbnailBitmap = thumbnail,
                        isLocked = false
                    )
                }
            }.getOrElse {
                PdfVisualData(
                    pagesCount = 0,
                    thumbnailBitmap = null,
                    isLocked = true
                )
            }
        } ?: PdfVisualData(
            pagesCount = 0,
            thumbnailBitmap = null,
            isLocked = true
        )
    }
}

private fun renderThumbnail(page: PdfRenderer.Page): Bitmap {
    val sourceWidth = page.width.coerceAtLeast(1)
    val sourceHeight = page.height.coerceAtLeast(1)

    val scale = min(
        THUMBNAIL_MAX_WIDTH_PX.toFloat() / sourceWidth.toFloat(),
        THUMBNAIL_MAX_HEIGHT_PX.toFloat() / sourceHeight.toFloat()
    ).coerceAtMost(1f)

    val outWidth = (sourceWidth * scale).roundToInt().coerceAtLeast(1)
    val outHeight = (sourceHeight * scale).roundToInt().coerceAtLeast(1)

    val rendered = createBitmap(outWidth, outHeight)
    rendered.eraseColor(Color.WHITE)

    page.render(
        rendered,
        null,
        null,
        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
    )

    val reduced = rendered.copy(Bitmap.Config.RGB_565, false)
    if (reduced != null && reduced !== rendered) {
        rendered.recycle()
        return reduced
    }

    return rendered
}

private fun readCachedVisualMeta(
    context: Context,
    cacheKey: String
): CachedVisualMeta? {
    val prefs = context.applicationContext.getSharedPreferences(VISUAL_META_PREFS, Context.MODE_PRIVATE)
    val lockKey = PREF_LOCKED_PREFIX + cacheKey
    if (!prefs.contains(lockKey)) return null

    val pageKey = PREF_PAGE_COUNT_PREFIX + cacheKey
    return CachedVisualMeta(
        pagesCount = prefs.getInt(pageKey, 0).coerceAtLeast(0),
        isLocked = prefs.getBoolean(lockKey, false)
    )
}

private fun writeCachedVisualMeta(
    context: Context,
    cacheKey: String,
    pagesCount: Int,
    isLocked: Boolean
) {
    val prefs = context.applicationContext.getSharedPreferences(VISUAL_META_PREFS, Context.MODE_PRIVATE)
    prefs.edit {
        putInt(PREF_PAGE_COUNT_PREFIX + cacheKey, pagesCount.coerceAtLeast(0))
            .putBoolean(PREF_LOCKED_PREFIX + cacheKey, isLocked)
    }
}

private fun readThumbnailFromDisk(
    context: Context,
    cacheKey: String
): Bitmap? {
    val file = thumbnailFile(context, cacheKey)
    if (!file.exists()) return null

    return runCatching {
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565
            inDither = true
        }
        FileInputStream(file).use { input ->
            BitmapFactory.decodeStream(input, null, options)
        }
    }.getOrNull()
}

private fun writeThumbnailToDisk(
    context: Context,
    cacheKey: String,
    bitmap: Bitmap
) {
    runCatching {
        val file = thumbnailFile(context, cacheKey)
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_JPEG_QUALITY, output)
        }
    }
}

private fun thumbnailFile(
    context: Context,
    cacheKey: String
): File {
    val dir = File(context.cacheDir, THUMBNAIL_CACHE_DIR)
    if (!dir.exists()) {
        dir.mkdirs()
    }
    return File(dir, "$cacheKey.jpg")
}

private fun buildVisualCacheKey(
    uri: Uri,
    sizeBytes: Long,
    lastModifiedEpochSeconds: Long
): String {
    val raw = "${uri}|$sizeBytes|$lastModifiedEpochSeconds|$THUMBNAIL_MAX_WIDTH_PX|$THUMBNAIL_MAX_HEIGHT_PX|$THUMBNAIL_JPEG_QUALITY"
    val bytes = MessageDigest
        .getInstance("SHA-256")
        .digest(raw.toByteArray())
    return bytes.joinToString(separator = "") { b -> "%02x".format(b) }
}
