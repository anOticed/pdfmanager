package me.notanoticed.pdfmanager.feature.preview

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
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

private data class RenderedPage(
    val key: String,
    val bitmap: Bitmap
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
                            key = "${pdf.uri}#$pageIndex"
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
        val targetWidthPx = with(density) { maxWidth.toPx().toInt().coerceAtLeast(1) }

        val rendered = remember { mutableStateListOf<RenderedPage>() }
        val ready = remember { mutableStateOf(false) }

        DisposableEffect(Unit) {
            onDispose {
                rendered.forEach { rp -> runCatching { if (!rp.bitmap.isRecycled) rp.bitmap.recycle() } }
                rendered.clear()
            }
        }

        LaunchedEffect(pdfs, targetWidthPx) {
            ready.value = false

            rendered.forEach { rp -> runCatching { if (!rp.bitmap.isRecycled) rp.bitmap.recycle() } }
            rendered.clear()

            withContext(Dispatchers.IO) {
                for (pdf in pdfs) {
                    val pfd: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(pdf.uri, "r") ?: continue

                    val renderer = PdfRenderer(pfd)
                    try {
                        val pageCount = renderer.pageCount.coerceAtLeast(0)

                        for (pageIndex in 0 until pageCount) {
                            val page = renderer.openPage(pageIndex)
                            try {
                                val scale = targetWidthPx.toFloat() / page.width.toFloat()
                                val targetHeightPx = (page.height * scale).toInt().coerceAtLeast(1)

                                val bitmap = Bitmap.createBitmap(
                                    targetWidthPx,
                                    targetHeightPx,
                                    Bitmap.Config.ARGB_8888
                                )
                                bitmap.eraseColor(Color.White.toArgb())
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                                val key = "${pdf.uri}#$pageIndex"
                                withContext(Dispatchers.Main) {
                                    rendered.add(RenderedPage(key = key, bitmap = bitmap))
                                }
                            } catch (_: Exception) {
                                /* ignore */
                            } finally {
                                runCatching { page.close() }
                            }
                        }
                    } catch (_: Exception) {
                        /* ignore */
                    } finally {
                        runCatching { renderer.close() }
                        runCatching { pfd.close() }
                    }
                }
            }

            ready.value = true
        }

        if (!ready.value) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Colors.Primary.blue)
            }
            return@BoxWithConstraints
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = rendered, key = { it.key }) { rp ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clipToBounds()
                        .background(Colors.Surface.card)
                ) {
                    Image(
                        bitmap = rp.bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
/* ----------------------------------------------------- */