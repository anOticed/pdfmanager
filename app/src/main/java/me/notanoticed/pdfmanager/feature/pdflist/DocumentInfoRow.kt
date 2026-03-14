/**
 * Small UI helper for rendering a label/value metadata row.
 */

package me.notanoticed.pdfmanager.feature.pdflist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.notanoticed.pdfmanager.core.pdf.PdfThumbnail
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- DOCUMENT INFO ROW -------------------- */
@Composable
fun DocumentInfoRow(
    modifier: Modifier = Modifier,
    pdf: PdfFile,
    searchQuery: String = ""
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PdfThumbnail(
            pdf = pdf,
            modifier = Modifier
                .width(56.dp)
                .height(76.dp),
            cornerRadius = 10.dp,
            placeholderBackground = Colors.Surface.thumbnail,
            placeholderIconTint = Colors.Icon.gray,
            placeholderIconSize = 30.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = buildHighlightedName(
                    fileName = pdf.name,
                    query = searchQuery
                ),
                color = Colors.Text.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            Text(
                text = pdf.metaLine(),
                color = Colors.Text.secondary,
                fontSize = 12.sp
            )
            Text(
                text = pdf.createdDate(),
                color = Colors.Text.secondary,
                fontSize = 11.sp,
            )
        }

        if (pdf.isLocked) {
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Locked icon",
                tint = Colors.Icon.lock,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}


private fun buildHighlightedName(fileName: String, query: String) = buildAnnotatedString {
    val key = query.trim()

    if (key.isEmpty()) {
        append(fileName)
        return@buildAnnotatedString
    }

    var current = 0
    while (current < fileName.length) {
        val matchStart = fileName.indexOf(key, startIndex = current, ignoreCase = true)

        if (matchStart < 0) {
            append(fileName.substring(current))
            break
        }

        if (matchStart > current) {
            append(fileName.substring(current, matchStart))
        }

        val matchEnd = (matchStart + key.length).coerceAtMost(fileName.length)
        pushStyle(SpanStyle(color = Colors.Text.blue))
        append(fileName.substring(matchStart, matchEnd))
        pop()

        current = matchEnd
    }
}

/* ----------------------------------------------------------- */
