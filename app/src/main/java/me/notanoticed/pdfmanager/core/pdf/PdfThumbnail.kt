/**
 * PDF thumbnail UI.
 */

package me.notanoticed.pdfmanager.core.pdf

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.ui.theme.Colors

@Composable
fun PdfThumbnail(
    pdf: PdfFile,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    placeholderBackground: Color = Colors.Surface.thumbnail,
    placeholderIconTint: Color = Colors.Icon.gray,
    placeholderIconSize: Dp = 30.dp
) {
    val thumbnail = if (pdf.isLocked) null else pdf.thumbnailBitmap?.takeUnless { it.isRecycled }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(placeholderBackground),
        contentAlignment = Alignment.Center
    ) {
        if (thumbnail != null) {
            Image(
                bitmap = thumbnail.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.None,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = "PDF icon",
                tint = placeholderIconTint,
                modifier = Modifier.size(placeholderIconSize)
            )
        }
    }
}
