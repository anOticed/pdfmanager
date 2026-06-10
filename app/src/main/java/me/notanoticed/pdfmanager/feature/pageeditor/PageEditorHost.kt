package me.notanoticed.pdfmanager.feature.pageeditor

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
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile

class PageEditorNav internal constructor(
    private val openRequest: (PageEditorRequest) -> Unit,
    private val closeRequest: () -> Unit
) {
    fun open(pdf: PdfFile) {
        openRequest(PageEditorRequest(pdf = pdf))
    }

    fun close() {
        closeRequest()
    }
}

data class PageEditorRequest(
    val pdf: PdfFile
)

val LocalPageEditorNav = staticCompositionLocalOf<PageEditorNav> {
    error("PageEditorNav not provided")
}

@Composable
fun ProvidePageEditor(
    viewModel: PageEditorViewModel,
    content: @Composable () -> Unit
) {
    var request by remember { mutableStateOf<PageEditorRequest?>(null) }
    var visible by remember { mutableStateOf(false) }

    val nav = remember(viewModel) {
        PageEditorNav(
            openRequest = {
                request = it
                visible = true
            },
            closeRequest = {
                visible = false
                request = null
                viewModel.closeSession()
            }
        )
    }

    CompositionLocalProvider(LocalPageEditorNav provides nav) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()

            AnimatedVisibility(
                visible = visible,
                enter = slideInHorizontally(
                    animationSpec = tween(220),
                    initialOffsetX = { it }
                ) + fadeIn(tween(160)),
                exit = slideOutHorizontally(
                    animationSpec = tween(220),
                    targetOffsetX = { it }
                ) + fadeOut(tween(160)),
                modifier = Modifier.align(Alignment.Center)
            ) {
                val safeRequest = request
                if (safeRequest != null) {
                    PageEditorScreen(
                        request = safeRequest,
                        viewModel = viewModel,
                        onBack = nav::close
                    )
                }
            }
        }
    }
}
