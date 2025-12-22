package me.notanoticed.pdfmanager.feature.preview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.ui.theme.Colors
import kotlin.collections.emptyList

/* -------------------- PDF PREVIEW -------------------- */
private data class PageRef(
    val pdf: PdfFile,
    val pageIndex: Int,
    val key: String
)

@Composable
fun PdfPreview(
    pdfs: List<PdfFile>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val pages by remember(pdfs) {
        derivedStateOf {
            pdfs.flatMap { pdf ->
                val count = pdf.pagesCount.coerceAtLeast(0)

                if (count == 0) {
                    emptyList()
                }
                else {
                    (0 until count).map { pageIndex ->
                        PageRef(
                            pdf = pdf,
                            pageIndex = pageIndex,
                            key = "${pdf.name}#$pageIndex"
                        )
                    }
                }
            }
        }
    }

    if (pdfs.isNotEmpty() && pages.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No pages to preview",
                color = Colors.Text.secondary,
                fontSize = 14.sp
            )
        }

        return
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val targetWidthPx = with(density) { (maxWidth - 32.dp).toPx().toInt().coerceAtLeast(1) }

        val cache = rememberPdfRendererCache(context)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = pages,
                key = { it.key }
            ) { page ->
                PdfPageCard(
                    cache = cache,
                    page = page,
                    targetWidthPx = targetWidthPx
                )
            }
        }
    }
}

@Composable
private fun PdfPageCard(
    cache: PdfRendererCache,
    page: PageRef,
    targetWidthPx: Int
) {
    val bitmapState = produceState<Bitmap?>(initialValue = null, page.key, targetWidthPx) {
        value = withContext(Dispatchers.IO) {
            cache.renderPage(
                pdf = page.pdf,
                pageIndex = page.pageIndex,
                targetWidthPx = targetWidthPx
            )
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Colors.Surface.card,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        val bmp = bitmapState.value

        if (bmp == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 22.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Colors.Primary.blue)
            }
        } else {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Colors.Surface.card)
            )
        }
    }
}
private class PdfRendererCache(private val context: Context) {
    private data class Entry(
        val pfd: ParcelFileDescriptor,
        val renderer: PdfRenderer,
        val mutex: Mutex = Mutex()
    )

    private val entries = mutableMapOf<String, Entry>()

    suspend fun renderPage(
        pdf: PdfFile,
        pageIndex: Int,
        targetWidthPx: Int
    ): Bitmap? {
        val key = pdf.uri.toString()
        val entry = getOrCreateEntry(key, pdf) ?: return null

        return entry.mutex.withLock {
            if (pageIndex !in 0 until entry.renderer.pageCount) {
                return@withLock null
            }

            val page = entry.renderer.openPage(pageIndex)

            try {
                val scale = targetWidthPx.toFloat() / page.width.toFloat()
                val targetHeightPx = (page.height * scale).toInt().coerceAtLeast(1)

                val bitmap = Bitmap.createBitmap(targetWidthPx, targetHeightPx, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.White.toArgb())

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            }
            catch (_: Exception) {
                null
            }
            finally {
                runCatching { page.close() }
            }
        }
    }

    private fun getOrCreateEntry(key: String, pdf: PdfFile): Entry? {
        entries[key]?.let { return it }

        return try {
            val pfd = context.contentResolver.openFileDescriptor(pdf.uri, "r") ?: return null
            val renderer = PdfRenderer(pfd)
            Entry(pfd = pfd, renderer = renderer).also { entries[key] = it }
        }
        catch (_: Exception) {
            null
        }
    }

    fun closeAll() {
        entries.values.forEach { e ->
            runCatching { e.renderer.close() }
            runCatching { e.pfd.close() }
        }
        entries.clear()
    }
}

@Composable
private fun rememberPdfRendererCache(context: Context): PdfRendererCache {
    val appContext = remember(context) { context.applicationContext }
    val cache = remember(appContext) { PdfRendererCache(appContext) }

    DisposableEffect(cache) {
        onDispose { cache.closeAll() }
    }

    return cache
}
/* ----------------------------------------------------- */