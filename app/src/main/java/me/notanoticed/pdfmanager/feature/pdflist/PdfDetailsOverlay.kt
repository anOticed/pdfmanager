/**
 * PDF details overlay.
 *
 * Shows a lightweight read-only view of PDF metadata (file name, size, path, timestamps).
 * The overlay is controlled by PdfListViewModel.
 */

package me.notanoticed.pdfmanager.feature.pdflist

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.ui.theme.Colors
import me.notanoticed.pdfmanager.ui.theme.PdfManagerTheme

/* -------------------- DETAILS PANEL -------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfDetailsOverlay(
    visible: Boolean,
    pdf: PdfFile?,
    onDismiss: () -> Unit
) {
    if (!visible || pdf == null) return

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Colors.Surface.card,
        contentColor = Colors.Primary.blue,
        dragHandle = null,
        properties = ModalBottomSheetProperties(
            isAppearanceLightStatusBars = false,
            isAppearanceLightNavigationBars = false,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp)
        ) {
            SheetHandle()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Colors.Text.primary
                )
            }

            HorizontalDivider(color = Colors.Border.subtle)
            Spacer(modifier = Modifier.height(10.dp))

            DetailsItem("File name", pdf.name)
            DetailsItem("Storage path", pdf.storagePath)
            DetailsItem("Created", pdf.createdDate())
            DetailsItem("Last modified", pdf.lastModifiedDate())
            DetailsItem("File size", pdf.size())

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}


@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Colors.Primary.blue)
        )
    }
}


@Composable
private fun DetailsItem(title: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            color = Colors.Text.primary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            color = Colors.Text.secondary,
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
/* ------------------------------------------------------- */



@Preview(showBackground = true)
@Composable
fun PdfDetailsOverlayPreview() {
    val pdfListViewModel: PdfListViewModel = viewModel()

    val file = PdfFile(
        uri = Uri.parse(""),
        name = "document.pdf",
        sizeBytes = 100000000,
        pagesCount = 100,
        storagePath = "/storage/emulated/0/Download/document.pdf",
        lastModifiedEpochSeconds = 1666666666,
        createdEpochSeconds = 1666666666,
        bitmap = null,
        isLocked = true
    )

    pdfListViewModel.openDetails(file)

    PdfManagerTheme {
        PdfDetailsOverlay(
            visible = pdfListViewModel.detailsPanelVisible,
            pdf = pdfListViewModel.detailsPanelPdf,
            onDismiss = { pdfListViewModel.closeDetails() }
        )
    }
}