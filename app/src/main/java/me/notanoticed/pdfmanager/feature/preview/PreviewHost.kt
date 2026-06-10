package me.notanoticed.pdfmanager.feature.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.notanoticed.pdfmanager.core.pdf.util.PagesPerSheetOption
import me.notanoticed.pdfmanager.core.pdf.edit.SplitPlan
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile

class PreviewNav internal constructor(
    private val openRequest: (PreviewRequest) -> Unit,
    private val closeRequest: () -> Unit,
    private val setVisible: (Boolean) -> Unit
) {
    fun openSingle(
        pdf: PdfFile,
        allowSearch: Boolean = true,
        titleOverride: String? = null
    ) {
        openRequest(
            PreviewRequest.Single(
                pdf = pdf,
                allowSearch = allowSearch,
                topBarTitle = titleOverride ?: pdf.name
            )
        )
        setVisible(true)
    }
    fun openSplit(
        pdf: PdfFile,
        plan: SplitPlan,
        pagesPerSheet: PagesPerSheetOption
    ) {
        openRequest(
            PreviewRequest.Split(
                pdf = pdf,
                plan = plan,
                pagesPerSheet = pagesPerSheet
            )
        )
        setVisible(true)
    }

    fun close() {
        closeRequest()
    }
}

val LocalPreviewNav = staticCompositionLocalOf<PreviewNav> { error("PreviewNav not provided") }

@Composable
fun ProvidePreview(content: @Composable () -> Unit) {
    var request by remember { mutableStateOf<PreviewRequest?>(null) }
    var visible by remember { mutableStateOf(false) }

    val nav = remember {
        PreviewNav(
            openRequest = { request = it },
            closeRequest = {
                visible = false
                request = null
            },
            setVisible = { visible = it }
        )
    }

    CompositionLocalProvider(LocalPreviewNav provides nav) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()

            AnimatedVisibility(
                visible = visible,
                enter = slideInHorizontally(
                    animationSpec = tween(200),
                    initialOffsetX = { it }
                ) + fadeIn(tween(140)),
                exit = slideOutHorizontally(
                    animationSpec = tween(200),
                    targetOffsetX = { it }
                ) + fadeOut(tween(140)),
                modifier = Modifier.align(Alignment.Center)
            ) {
                val safeRequest = request
                if (safeRequest != null) {
                    PreviewScaffold(
                        request = safeRequest,
                        onBack = { nav.close() }
                    )
                }
            }
        }
    }
}
