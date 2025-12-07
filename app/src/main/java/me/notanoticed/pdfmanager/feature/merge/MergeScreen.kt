package me.notanoticed.pdfmanager.feature.merge

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CallMerge
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- SCREEN -------------------- */
data class MergeFile(
    val id: Int,
    val name: String,
    val meta: String
)

@Composable
fun MergeActiveScreen(
    modifier: Modifier = Modifier,
    viewModel: MergeViewModel
) {
    val mergeFiles = viewModel.pdfMergeFiles

    Column(
        modifier = modifier.fillMaxSize().padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Colors.Surface.charcoalSlate,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
            ) {
                Text(
                    text = "Merge Order",
                    color = Colors.Text.blue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Drag files up or down to reorder them. The final PDF will follow this exact order.",
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
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mergeFiles) { file ->
                    MergeFileCard(file)
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).padding(top = 12.dp)
        ) {
            Button(
                onClick = { viewModel.clear() },
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = Colors.Button.darkSlate
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Clear All Files",
                    color = Colors.Text.secondary,
                    fontSize = 16.sp
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Colors.Surface.charcoalSlate,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { /* TODO: preview */ },
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = Colors.Button.darkSlate
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = "PDF icon",
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
                    onClick = { /* TODO: merge */ },
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = Colors.Button.green
                    ),
                    modifier = Modifier.weight(1.75f),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = "PDF icon",
                        tint = Colors.Icon.white,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Merge PDFs",
                        color = Colors.Text.primary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}


@Composable
fun MergeFileCard(file: MergeFile) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Colors.Surface.card,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
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
                    text = file.id.toString(),
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
                    imageVector = Icons.Outlined.Description,
                    contentDescription = "PDF icon",
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
                    text = file.name,
                    color = Colors.Text.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = file.meta,
                    color = Colors.Text.secondary,
                    fontSize = 11.sp,
                )
            }

            IconButton(
                onClick = { /* TODO: move up */ },
                modifier = Modifier.size(26.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = file.id > 1,
                colors = IconButtonDefaults.iconButtonColors().copy(
                    containerColor = Colors.Button.iconBackground,
                    contentColor = Colors.Icon.white,
                    disabledContainerColor = Colors.Button.iconBackgroundDisabled,
                    disabledContentColor = Colors.Icon.disabledGray,
                )
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.ArrowUpward,
                        contentDescription = "Move up",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = { /* TODO: move down */ },
                modifier = Modifier.size(26.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = file.id <= 1,
                colors = IconButtonDefaults.iconButtonColors().copy(
                    containerColor = Colors.Button.iconBackground,
                    contentColor = Colors.Icon.white,
                    disabledContainerColor = Colors.Button.iconBackgroundDisabled,
                    disabledContentColor = Colors.Icon.disabledGray,
                )
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.ArrowDownward,
                        contentDescription = "Move up",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = { /* TODO: delete file */ },
                modifier = Modifier.size(26.dp),
                shape = RoundedCornerShape(8.dp),
                colors = IconButtonDefaults.iconButtonColors().copy(
                    containerColor = Colors.Icon.red,
                    contentColor = Colors.Icon.white,
                )
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Move up",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}



@Composable
fun MergeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.CallMerge,
            contentDescription = null,
            tint = Colors.Icon.default,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No PDFs selected",
            color = Colors.Text.secondary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Add PDFs to merge them into a single document.",
            color = Colors.Text.muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "You can reorder them by dragging.",
            color = Colors.Text.muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
/* ------------------------------------------------ */