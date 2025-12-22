package me.notanoticed.pdfmanager.feature.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- SCREEN -------------------- */
@Composable
fun PreviewScreen(
    modifier: Modifier = Modifier,
    request: PreviewRequest
) {
    when (request) {
        is PreviewRequest.Single -> {
            PdfPreview(
                modifier = modifier.fillMaxSize(),
                pdfs = listOf(request.pdf)
            )
        }

        is PreviewRequest.Merge -> {
            PdfPreview(
                modifier = modifier.fillMaxSize(),
                pdfs = request.pdfs
            )
        }

        is PreviewRequest.Split -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                   imageVector = Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = Colors.Icon.default
                )

                Text(
                    text = "Split preview is not implemented yet",
                    color = Colors.Text.secondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}
/* ------------------------------------------------ */