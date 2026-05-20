package me.notanoticed.pdfmanager.feature.pageeditor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DrawerValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.toast.BindViewModelToasts
import me.notanoticed.pdfmanager.feature.export.LocalPdfOutputFlow
import me.notanoticed.pdfmanager.feature.preview.PdfPreview
import me.notanoticed.pdfmanager.ui.theme.Colors
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.io.File

@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PageEditor(
    request: PageEditorRequest,
    viewModel: PageEditorViewModel,
    onBack: () -> Unit
) {
    BindViewModelToasts(viewModel)

    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val overlayInteractionSource = remember { MutableInteractionSource() }

    LaunchedEffect(request.pdf.uri) {
        viewModel.open(context, request.pdf)
    }

    BackHandler(onBack = onBack)
    val pdfOutputFlow = LocalPdfOutputFlow.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !viewModel.isLoading && !viewModel.isApplyingOperation,
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                drawerContainerColor = Colors.Surface.card,
                drawerContentColor = Colors.Text.primary,
                modifier = Modifier
                    .widthIn(max = 216.dp)
                    .fillMaxHeight()
            ) {
                PageEditorDrawer(
                    pages = viewModel.pages,
                    currentPageIndex = viewModel.currentPageIndex,
                    previewPdf = viewModel.previewPdf,
                    previewVersion = viewModel.previewVersion,
                    isApplyingOperation = viewModel.isApplyingOperation,
                    onSelectPage = viewModel::selectPage,
                    onMoveInUi = viewModel::movePageInUi,
                    onCommitMove = { viewModel.commitReorder(context) },
                    onDeletePage = { index ->
                        viewModel.selectPage(index)
                        viewModel.deleteCurrentPage(context)
                    },
                    onCloseDrawer = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Colors.Background.app,
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Colors.Surface.card,
                                contentColor = Colors.Icon.default
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = stringResource(R.string.action_back)
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.page_editor_title),
                                color = Colors.Text.primary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = request.pdf.name,
                                color = Colors.Text.secondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions = {
                        Button(
                            onClick = { scope.launch { drawerState.open() } },
                            enabled = !viewModel.isLoading,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Colors.Button.darkSlate,
                                contentColor = Colors.Primary.white
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.page_editor_drawer_title)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                viewModel.requestSave(
                                    context = context,
                                    onRequest = pdfOutputFlow::start
                                )
                            },
                            enabled = !viewModel.isLoading && !viewModel.isApplyingOperation && viewModel.previewPdf != null,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Colors.Button.blue,
                                contentColor = Colors.Primary.white
                            )
                        ) {
                            Text(text = stringResource(R.string.action_save))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Colors.Surface.card,
                        titleContentColor = Colors.Text.primary
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .imePadding()
                    .background(Colors.Background.app)
            ) {
                when {
                    viewModel.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = Colors.Primary.blue)
                                Text(
                                    text = stringResource(R.string.page_editor_loading),
                                    color = Colors.Text.secondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    viewModel.previewPdf != null -> {
                        key(viewModel.previewVersion, viewModel.previewPdf!!.uri) {
                            PdfPreview(
                                pdf = viewModel.previewPdf!!,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.page_editor_preview_unavailable),
                                color = Colors.Text.secondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }

                if (viewModel.isApplyingOperation) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Colors.Primary.nearBlack.copy(alpha = 0.45f))
                            .clickable(
                                interactionSource = overlayInteractionSource,
                                indication = null
                            ) {}
                    )
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Colors.Surface.card,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 28.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Colors.Primary.blue,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.6.dp
                            )
                            Text(
                                text = stringResource(R.string.page_editor_applying_changes),
                                color = Colors.Text.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageEditorDrawer(
    pages: List<PageEditorPage>,
    currentPageIndex: Int,
    previewPdf: PdfFile?,
    previewVersion: Long,
    isApplyingOperation: Boolean,
    onSelectPage: (Int) -> Unit,
    onMoveInUi: (Int, Int) -> Unit,
    onCommitMove: () -> Unit,
    onDeletePage: (Int) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val listState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(
        lazyListState = listState,
        scrollThresholdPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        onMove = { from, to -> onMoveInUi(from.index, to.index) }
    )
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Colors.Surface.card)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.page_editor_pages_button,
                        pages.size
                    ),
                    color = Colors.Text.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onCloseDrawer,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Colors.Button.iconBackground,
                        contentColor = Colors.Icon.default
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.action_hide)
                    )
                }
            }

            HorizontalDivider(color = Colors.Border.subtle)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(
                    items = pages,
                    key = { _, page -> page.id }
                ) { index, page ->
                    ReorderableItem(
                        state = reorderableState,
                        key = page.id
                    ) { isDragging ->
                        val previewPath = previewPdf?.storagePath
                        PageDrawerItem(
                            pageIndex = index,
                            thumbnailPageIndex = page.thumbnailPageIndex,
                            isCurrent = index == currentPageIndex,
                            previewPath = previewPath,
                            previewVersion = previewVersion,
                            isApplyingOperation = isApplyingOperation,
                            isDragging = isDragging,
                            onClick = { onSelectPage(index) },
                            onDelete = { onDeletePage(index) },
                            dragHandleModifier = Modifier.draggableHandle(
                                interactionSource = interactionSource,
                                onDragStopped = { onCommitMove() }
                            )
                        )
                    }
                }
            }
        }

        if (isApplyingOperation) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Colors.Primary.nearBlack.copy(alpha = 0.18f))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {}
            )
        }
    }
}

@Composable
private fun PageDrawerItem(
    pageIndex: Int,
    thumbnailPageIndex: Int,
    isCurrent: Boolean,
    previewPath: String?,
    previewVersion: Long,
    isApplyingOperation: Boolean,
    isDragging: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    dragHandleModifier: Modifier
) {
    val thumbnail by produceState<android.graphics.Bitmap?>(
        initialValue = null,
        key1 = previewPath,
        key2 = previewVersion,
        key3 = thumbnailPageIndex
    ) {
        value = previewPath
            ?.takeIf { it.isNotBlank() }
            ?.let { path -> File(path) }
            ?.let { file ->
                withContext(Dispatchers.IO) {
                    runCatching {
                        renderPdfPageThumbnail(file = file, pageIndex = thumbnailPageIndex)
                    }.getOrNull()
                }
            }
    }

    val borderColor = when {
        isCurrent -> Colors.Border.blue
        isDragging -> Colors.Border.lightBlue
        else -> Colors.Border.darkGray
    }
    val surfaceColor = if (isCurrent) Colors.Surface.selectedCard else Colors.Surface.card

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = if (isDragging) 8.dp else 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isApplyingOperation, onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Colors.Primary.blue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (pageIndex + 1).toString(),
                    color = Colors.Primary.white,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Colors.Surface.thumbnail),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail!!.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Colors.Button.iconBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (pageIndex + 1).toString(),
                            color = Colors.Text.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = dragHandleModifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isApplyingOperation) Colors.Button.iconBackgroundDisabled else Colors.Button.iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.DragHandle,
                    contentDescription = stringResource(R.string.action_reorder),
                    tint = if (isApplyingOperation) Colors.Icon.disabledGray else Colors.Icon.default,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                enabled = !isApplyingOperation,
                modifier = Modifier.size(26.dp),
                shape = RoundedCornerShape(8.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Colors.Icon.red,
                    contentColor = Colors.Icon.white,
                    disabledContainerColor = Colors.Button.iconBackgroundDisabled,
                    disabledContentColor = Colors.Icon.disabledGray
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.action_remove),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
