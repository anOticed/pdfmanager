package com.example.pdfmanager.feature.pdflist

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pdfmanager.core.pdf.model.PdfFile
import com.example.pdfmanager.core.pdf.model.createdDate
import com.example.pdfmanager.core.pdf.model.metaLine
import com.example.pdfmanager.ui.theme.Colors
import com.example.pdfmanager.ui.theme.PdfManagerTheme

/* -------------------- SCREEN -------------------- */
@Composable
fun PdfListScreen(
    modifier: Modifier = Modifier,
    viewModel: PdfListViewModel,
) {
    val context = LocalContext.current

    var isLoading = viewModel.isLoading
    var errorText = viewModel.errorText
    val pdfFiles = viewModel.pdfFiles

    // Launcher for MANAGE_EXTERNAL_STORAGE intent (Android 11+)
    val manageAllFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Environment.isExternalStorageManager()) {
            viewModel.loadAll(context)
        } else {
            errorText = "All files access was not granted"
        }
    }

    // API <= 32
    val readStorageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.loadAll(context)
        } else {
            errorText = "Storage permission denied"
        }
    }

    LaunchedEffect(Unit) {
        when {
            // API 33+ (Android 13+)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (!Environment.isExternalStorageManager()) {
                    isLoading = false
                    viewModel.requestAllFilesAccess(context, manageAllFilesLauncher)
                } else {
                    viewModel.loadAll(context)
                }
            }

            // API 30–32 (Android 10, 11, 12)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                val granted = context.checkSelfPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

                if (!granted) {
                    isLoading = false
                    readStorageLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                } else {
                    viewModel.loadAll(context)
                }
            }
        }
    }

    when {
        isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Colors.blueColor)
            }
        }

        errorText != null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorText, color = Colors.textMutedColor)
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(pdfFiles) { pdf ->
                    DocumentCard(
                        pdf = pdf,
                        onMoreClick = { viewModel.openOptions(pdf = pdf) })
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}
/* ------------------------------------------------ */



/* -------------------- DOCUMENT CARD -------------------- */
@Composable
fun DocumentInfoRow(
    modifier: Modifier = Modifier,
    pdf: PdfFile
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(76.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF2A2F37)), // TODO: change
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = "PDF icon",
                tint = Color(0xFF7D8592), // TODO: change
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = pdf.name,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            Text(
                text = pdf.metaLine(),
                color = Colors.textMutedColor,
                fontSize = 12.sp
            )
            Text(
                text = pdf.createdDate(),
                color = Colors.textMutedColor,
                fontSize = 11.sp,
            )
        }

        if (pdf.isLocked) {
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Locked icon",
                tint = Color(0xFFFFB74D), // TODO: change
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

@Composable
fun DocumentCard(
    pdf: PdfFile,
    onMoreClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Colors.cardColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DocumentInfoRow(
                modifier = Modifier.weight(1f),
                pdf = pdf
            )
            IconButton(onClick = onMoreClick /* TODO: options menu */ ) {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = "More icon",
                    tint = Colors.textMutedColor
                )
            }
        }
    }
}
/* ------------------------------------------------------- */



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
                items = PdfFileOptionItems,
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
    items: List<PdfFileOptions>,
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
    item: PdfFileOptions,
    onClick: () -> Unit
) {
    val color = if (item.isDestructive) {
        Color(0xFFE82D2C)
    }
    else {
        Colors.textMutedColor
    }

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
            tint = color,
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