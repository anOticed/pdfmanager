/**
 * Preview screen implementation.
 *
 * Interprets PreviewRequest and renders the appropriate preview UI.
 */

package me.notanoticed.pdfmanager.feature.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.feature.split.prepareSplitPreviewPdf
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- SCREEN -------------------- */
@Composable
fun PreviewScreen(
    modifier: Modifier = Modifier,
    request: PreviewRequest,
    searchToggleRequestNonce: Int = 0
) {
    when (request) {
        is PreviewRequest.Single -> {
            PdfPreview(
                modifier = modifier.fillMaxSize(),
                pdf = request.pdf,
                searchToggleRequestNonce = searchToggleRequestNonce
            )
        }

        is PreviewRequest.Split -> {
            SplitPreviewContent(
                modifier = modifier,
                request = request,
                searchToggleRequestNonce = searchToggleRequestNonce
            )
        }
    }
}
/* ------------------------------------------------ */


/* -------------------- SPLIT PREVIEW -------------------- */
@Composable
private fun SplitPreviewContent(
    modifier: Modifier,
    request: PreviewRequest.Split,
    searchToggleRequestNonce: Int
) {
    val context = LocalContext.current
    val state by produceState<SplitPreviewState>(
        initialValue = SplitPreviewState.Loading,
        key1 = request.pdf.uri,
        key2 = request.configuration
    ) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                prepareSplitPreviewPdf(
                    context = context,
                    sourcePdf = request.pdf,
                    configuration = request.configuration
                )
            }.fold(
                onSuccess = SplitPreviewState::Ready,
                onFailure = { error ->
                    SplitPreviewState.Error(
                        error.message?.takeIf { it.isNotBlank() }
                            ?: "Failed to prepare split preview"
                    )
                }
            )
        }
    }

    when (val currentState = state) {
        SplitPreviewState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = Colors.Primary.blue)

                    Text(
                        text = "Preparing split preview...",
                        color = Colors.Text.secondary,
                        fontSize = 14.sp
                    )
                }
            }
        }

        is SplitPreviewState.Ready -> {
            PdfPreview(
                modifier = modifier.fillMaxSize(),
                pdf = currentState.pdf,
                searchToggleRequestNonce = searchToggleRequestNonce
            )
        }

        is SplitPreviewState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = Colors.Icon.default
                    )

                    Text(
                        text = currentState.message,
                        color = Colors.Text.secondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

private sealed interface SplitPreviewState {
    data object Loading : SplitPreviewState
    data class Ready(val pdf: PdfFile) : SplitPreviewState
    data class Error(val message: String) : SplitPreviewState
}
/* ------------------------------------------------------ */
