/**
 * PDF preview renderer based on AndroidX PDF Viewer Fragment.
 */

package me.notanoticed.pdfmanager.feature.preview

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- ENTRY -------------------- */
@SuppressLint("NewApi")
@Composable
fun PdfPreview(
    pdf: PdfFile,
    modifier: Modifier = Modifier,
    searchToggleRequestNonce: Int = 0
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    if (activity == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Preview is unavailable in this context",
                color = Colors.Text.secondary,
                fontSize = 14.sp
            )
        }
        return
    }

    val fragmentManager = activity.supportFragmentManager
    val containerId = remember { View.generateViewId() }
    val fragmentTag = remember(pdf.uri) { "preview_pdf_viewer_${pdf.uri.hashCode()}" }
    var lastAppliedSearchToggleRequest by remember(fragmentTag) { mutableIntStateOf(0) }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { androidContext ->
            FragmentContainerView(androidContext).apply {
                id = containerId
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { container ->
            val fragment = (fragmentManager.findFragmentByTag(fragmentTag) as? AppPdfViewerFragment)
                ?: run {
                    if (fragmentManager.isStateSaved) return@AndroidView

                    val created = AppPdfViewerFragment()
                    fragmentManager.beginTransaction()
                        .replace(container.id, created, fragmentTag)
                        .commitNowAllowingStateLoss()
                    created
                }

            fragment.documentUri = pdf.uri
            if (searchToggleRequestNonce > lastAppliedSearchToggleRequest) {
                fragment.isTextSearchActive = !fragment.isTextSearchActive
                lastAppliedSearchToggleRequest = searchToggleRequestNonce
            }
        }
    )

    DisposableEffect(fragmentTag) {
        onDispose {
            val fragment = fragmentManager.findFragmentByTag(fragmentTag) ?: return@onDispose
            if (!fragmentManager.isStateSaved) {
                fragmentManager.beginTransaction()
                    .remove(fragment)
                    .commitNowAllowingStateLoss()
            }
        }
    }
}
/* ----------------------------------------------- */
