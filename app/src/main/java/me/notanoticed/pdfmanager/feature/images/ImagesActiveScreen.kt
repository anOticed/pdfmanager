package me.notanoticed.pdfmanager.feature.images

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.notanoticed.pdfmanager.ui.theme.Colors
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/* -------------------- ACTIVE SCREEN -------------------- */
@Composable
fun ImageActiveScreen(
    modifier: Modifier = Modifier,
    viewModel: ImagesViewModel
) {
    val images = viewModel.selectedImages
    val listState = rememberLazyListState()

    val reorderableState = rememberReorderableLazyListState(
        lazyListState = listState,
        scrollThresholdPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        onMove = { from, to -> viewModel.move(from.index, to.index) }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Colors.Surface.charcoalSlate,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
            ) {
                Text(
                    text = "Image Order",
                    color = Colors.Text.blue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Drag items to reorder them. Each image becomes one PDF page.",
                    color = Colors.Text.secondary,
                    fontSize = 12.sp
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color.Transparent,
            border = BorderStroke(width = 2.dp, color = Colors.Border.darkBlue),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = images,
                    key = { _, item -> item.id }
                ) { index, item ->
                    ReorderableItem(
                        state = reorderableState,
                        key = item.id
                    ) { isDragging ->
                        val elevation = if (isDragging) 8.dp else 0.dp
                        val dragScale = remember(isDragging) { if (isDragging) 1.05f else 1f }

                        ImageItemCard(
                            index = index + 1,
                            image = item,
                            onRemove = { viewModel.remove(item) },
                            dragHandleModifier = Modifier.draggableHandle(),
                            modifier = Modifier.graphicsLayer {
                                shadowElevation = elevation.toPx()
                                scaleX = dragScale
                                scaleY = dragScale
                            }
                        )
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Colors.Surface.charcoalSlate,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { /* TODO: preview */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Colors.Button.darkSlate
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = "Preview",
                        tint = Colors.Icon.white,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Preview",
                        color = Colors.Text.primary,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = { /* TODO: create pdf */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Colors.Button.green
                    ),
                    modifier = Modifier.weight(1.75f),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PictureAsPdf,
                        contentDescription = "Create PDF",
                        tint = Colors.Icon.white,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Create PDF",
                        color = Colors.Text.primary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
/* ------------------------------------------------------- */



/* -------------------- ITEM CARD -------------------- */
@Composable
private fun ImageItemCard(
    index: Int,
    image: ImageItem,
    onRemove: () -> Unit,
    dragHandleModifier: Modifier,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Colors.Surface.card,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Colors.Primary.blue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.toString(),
                    color = Colors.Text.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Colors.Icon.darkGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = Colors.Icon.gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = image.name,
                    color = Colors.Text.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${image.widthPx} × ${image.heightPx}",
                    color = Colors.Text.secondary,
                    fontSize = 11.sp,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = dragHandleModifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Colors.Button.iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.DragHandle,
                    contentDescription = "Reorder",
                    tint = Colors.Icon.white,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(26.dp),
                shape = RoundedCornerShape(8.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Colors.Icon.red,
                    contentColor = Colors.Icon.white,
                )
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
/* ------------------------------------------------------- */