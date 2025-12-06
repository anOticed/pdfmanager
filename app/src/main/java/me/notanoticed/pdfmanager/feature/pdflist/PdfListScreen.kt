package me.notanoticed.pdfmanager.feature.pdflist

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.ui.theme.Colors
import me.notanoticed.pdfmanager.ui.theme.PdfManagerTheme

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

    val hasData = pdfFiles.isNotEmpty()
    val showFullscreenLoader = isLoading && !hasData
    val showFullscreenError = errorText != null && !hasData

    when {
        showFullscreenLoader -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Colors.blueColor)
            }
        }

        showFullscreenError -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorText, color = Colors.textMutedColor)
            }
        }

        else -> {
            val pullState = rememberPullToRefreshState()
            val isRefreshing = isLoading && hasData

            PullToRefreshBox(
                modifier = modifier.fillMaxSize(),
                state = pullState,
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.loadAll(context) },
                indicator = {
                    Indicator(
                        state = pullState,
                        isRefreshing = isRefreshing,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = Colors.cardColor,
                        color = Colors.blueColor
                    )
                }
            ) {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(pdfFiles) { pdf ->
                        DocumentCard(
                            pdf = pdf,
                            isSelectionMode = viewModel.isSelectionMode,
                            isSelected = viewModel.isSelected(pdf),
                            onClick = { viewModel.onItemClick(pdf) },
                            onLongPress = { viewModel.onItemLongPress(pdf) },
                            onMoreClick = { viewModel.openOptions(pdf = pdf) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}
/* ------------------------------------------------ */



/* -------------------- DOCUMENT CARD -------------------- */
@Composable
fun DocumentCard(
    pdf: PdfFile,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
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
            modifier = Modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongPress
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DocumentInfoRow(
                modifier = Modifier.weight(1f),
                pdf = pdf
            )
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors().copy(
                        checkedBoxColor = Colors.blueColor,
                        checkedBorderColor = Colors.blueColor,
                        checkedCheckmarkColor = Color.White
                    )
                )
            }
            else {
                IconButton(onClick = onMoreClick) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = "More icon",
                        tint = Colors.textMutedColor
                    )
                }
            }
        }
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