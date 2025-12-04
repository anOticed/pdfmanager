package com.example.pdfmanager.feature.pdflist

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CallMerge
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Reorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pdfmanager.app.Screen
import com.example.pdfmanager.core.pdf.model.PdfFile
import com.example.pdfmanager.core.pdf.model.createdDate
import com.example.pdfmanager.core.pdf.model.metaLine
import com.example.pdfmanager.feature.merge.MergeFile
import com.example.pdfmanager.feature.merge.MergeViewModel
import com.example.pdfmanager.feature.split.SplitViewModel
import com.example.pdfmanager.ui.theme.Colors
import com.example.pdfmanager.ui.theme.PdfManagerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
                items(pdfFiles) { file ->
                    val document = Document(
                        name = file.name,
                        meta = file.metaLine(),
                        time = file.createdDate(),
                        bitmap = file.bitmap,
                        locked = file.isLocked
                    )
                    DocumentCard(document = document, onMoreClick = { viewModel.openOptions(pdf = file) })
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}
/* ------------------------------------------------ */



/* -------------------- DOCUMENT CARD -------------------- */
data class Document(
    val name: String,
    val meta: String,
    val time: String,
    val bitmap: Bitmap? = null,
    val locked: Boolean = false
)

@Composable
fun DocumentCard(
    document: Document,
    onMoreClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Colors.cardColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(76.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF2A2F37)), // TODO: change
                    contentAlignment = Alignment.Center
                ) {
                    if (document.bitmap == null) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = "PDF icon",
                            tint = Color(0xFF7D8592), // TODO: change
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    else {
                        Image(
                            bitmap = document.bitmap.asImageBitmap(),
                            contentDescription = "PDF preview",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(scaleX = 1.2f, scaleY = 1.2f),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = document.name,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = document.meta,
                        color = Colors.textMutedColor,
                        fontSize = 12.sp
                    )
                    Text(
                        text = document.time,
                        color = Colors.textMutedColor,
                        fontSize = 11.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (document.locked) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Locked icon",
                            tint = Color(0xFFFFB74D), // TODO: change
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

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
    onAction: (FileOptionAction) -> Unit
) {
    if (!visible || pdf == null) return

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Colors.cardColor,
        properties = ModalBottomSheetProperties(
            isAppearanceLightStatusBars = false,
            isAppearanceLightNavigationBars = false,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(16.dp)
        ) {
            OptionsOverlayHeader(pdf)
            HorizontalDivider(
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OptionsOverlayList(
                items = fileOptions(),
                onItemClick = {action ->
                    onAction(action)
                    onDismiss()
                }
            )
        }
    }
}


enum class FileOptionAction {
    RENAME,
    MERGE,
    SPLIT,
    REORDER_PAGES,
    SET_PASSWORD,
    REMOVE_PASSWORD,
    PRINT,
    SHARE,
    DETAILS,
    DELETE
}


private data class FileOptionItem(
    val action: FileOptionAction,
    val title: String,
    val icon: ImageVector,
    val isDestructive: Boolean = false
)


@Composable
private fun OptionsOverlayHeader(pdf: PdfFile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
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

        Text(
            text = pdf.name,
            color = Colors.textMainColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
private fun OptionsOverlayList(
    items: List<FileOptionItem>,
    onItemClick: (FileOptionAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(state = rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        items.forEach { item ->
            FileOptionRow(
                item = item,
                onClick = { onItemClick(item.action) }
            )
        }
    }
}


@Composable
private fun FileOptionRow(
    item: FileOptionItem,
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


private fun fileOptions(): List<FileOptionItem> = listOf(
    FileOptionItem(
        action = FileOptionAction.RENAME,
        title = "Rename",
        icon = Icons.Outlined.Edit
    ),
    FileOptionItem(
        action = FileOptionAction.MERGE,
        title = "Merge",
        icon = Icons.Outlined.CallMerge
    ),
    FileOptionItem(
        action = FileOptionAction.SPLIT,
        title = "Split",
        icon = Icons.Outlined.CallSplit
    ),
    FileOptionItem(
        action = FileOptionAction.REORDER_PAGES,
        title = "Reorder Pages",
        icon = Icons.Outlined.Reorder
    ),
    FileOptionItem(
        action = FileOptionAction.SET_PASSWORD,
        title = "Set password",
        icon = Icons.Outlined.Lock
    ),
    FileOptionItem(
        action = FileOptionAction.REMOVE_PASSWORD,
        title = "Remove password",
        icon = Icons.Outlined.LockOpen
    ),
    FileOptionItem(
        action = FileOptionAction.PRINT,
        title = "Print",
        icon = Icons.Outlined.Print
    ),
    FileOptionItem(
        action = FileOptionAction.SHARE,
        title = "Share",
        icon = Icons.Outlined.Share
    ),
    FileOptionItem(
        action = FileOptionAction.DETAILS,
        title = "Details",
        icon = Icons.Outlined.Info
    ),
    FileOptionItem(
        action = FileOptionAction.DELETE,
        title = "Delete",
        icon = Icons.Outlined.Delete,
        isDestructive = true
    )
)


fun handleFileOptionAction(
    action: FileOptionAction,
    pdf: PdfFile,
    splitViewModel: SplitViewModel,
    mergeViewModel: MergeViewModel,
    scope: CoroutineScope,
    pagerState: PagerState,
    tabs: List<String>
) {
    when (action) {
        FileOptionAction.RENAME -> {
        }
        FileOptionAction.MERGE -> {
            scope.launch {
                mergeViewModel.setMergeFiles(
                    listOf(
                        MergeFile(1, "test.pdf", "1 page", "1.0 MB")
                    )
                )
                val page = tabs.indexOf(Screen.Merge.route)
                if (page >= 0) pagerState.animateScrollToPage(page)
            }
        }
        FileOptionAction.SPLIT -> {
            splitViewModel.updateSelectedSplitPdf(pdf)
            scope.launch {
                val page = tabs.indexOf(Screen.Split.route)
                if (page >= 0) pagerState.animateScrollToPage(page)
            }
        }
        FileOptionAction.REORDER_PAGES -> {
        }
        FileOptionAction.SET_PASSWORD -> {
        }
        FileOptionAction.REMOVE_PASSWORD -> {
        }
        FileOptionAction.PRINT -> {
        }
        FileOptionAction.SHARE -> {
        }
        FileOptionAction.DETAILS -> {
        }
        FileOptionAction.DELETE -> {
        }
    }
}

/* ------------------------------------------------------- */



@Preview(showBackground = true)
@Composable
fun OptionsOverlayPreview() {
    val pdfListViewModel: PdfListViewModel = viewModel()

    val file: PdfFile = PdfFile(
        uri = Uri.parse(""),
        name = "document.pdf",
        sizeBytes = 100000000,
        pagesCount = 100,
        createdEpochSeconds = 1666666666,
        bitmap = null,
        isLocked = false
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