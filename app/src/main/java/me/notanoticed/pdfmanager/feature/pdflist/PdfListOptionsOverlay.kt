package me.notanoticed.pdfmanager.feature.pdflist

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.ui.theme.Colors
import me.notanoticed.pdfmanager.ui.theme.PdfManagerTheme

/* -------------------- OPTIONS PANEL -------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsOverlay(
    visible: Boolean,
    pdf: PdfFile?,
    onDismiss: () -> Unit,
    onAction: (PdfFileOptionAction) -> Unit
) {
    if (!visible || pdf == null) return

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Colors.cardColor,
        contentColor = Colors.blueColor,
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
            DocumentInfoRow(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 20.dp),
                pdf = pdf
            )
            HorizontalDivider(
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OptionsOverlayList(
                items = pdfFileOptionItems,
                onItemClick = {action ->
                    onAction(action)
                    onDismiss()
                },
                isLocked = pdf.isLocked
            )
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
                .background(Colors.blueColor)
        )
    }
}


@Composable
private fun OptionsOverlayList(
    items: List<PdfFileOptionItem>,
    onItemClick: (PdfFileOptionAction) -> Unit,
    isLocked: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        items.filterNot{ item ->
            (item.action == PdfFileOptionAction.SET_PASSWORD && isLocked) ||
                    (item.action == PdfFileOptionAction.REMOVE_PASSWORD && !isLocked)
        }.forEach { item ->
            FileOptionRow(
                item = item,
                onClick = { onItemClick(item.action) }
            )
        }
    }
}


@Composable
private fun FileOptionRow(
    item: PdfFileOptionItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = item.tint,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(24.dp))

        Text(
            text = item.title,
            color = Colors.textMainColor
        )
    }
}
/* ------------------------------------------------------- */



@Preview(showBackground = true)
@Composable
fun OptionsOverlayPreview() {
    val pdfListViewModel: PdfListViewModel = viewModel()

    val file = PdfFile(
        uri = Uri.parse(""),
        name = "document.pdf",
        sizeBytes = 100000000,
        pagesCount = 100,
        createdEpochSeconds = 1666666666,
        bitmap = null,
        isLocked = true
    )

    pdfListViewModel.openOptions(file)

    PdfManagerTheme {
        OptionsOverlay(
            visible = pdfListViewModel.optionsPanelVisible,
            pdf = pdfListViewModel.optionsPanelPdf,
            onDismiss = { pdfListViewModel.closeOptions() },
            onAction = {}
        )
    }
}