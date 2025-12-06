package me.anoticed.pdfmanager.feature.merge

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
import me.anoticed.pdfmanager.ui.theme.Colors

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
            color = Color(0xB01F2937),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
            ) {
                Text(
                    text = "Merge Order",
                    color = Colors.blueColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Drag files up or down to reorder them. The final PDF will follow this exact order.",
                    color = Colors.textMutedColor,
                    fontSize = 12.sp
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color.Transparent,
            border = BorderStroke(width = 2.dp, color = Color(0xFF202B4C)),
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
                    containerColor = Color(0xFF374050)
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Clear All Files",
                    color = Colors.textMutedColor,
                    fontSize = 16.sp
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xB01F2937),
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
                        containerColor = Color(0xFF374050)
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = "PDF icon",
                        tint = Color.White, // TODO: change
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Preview",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = { /* TODO: merge */ },
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = Color(0xFF059568)
                    ),
                    modifier = Modifier.weight(1.75f),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = "PDF icon",
                        tint = Color.White, // TODO: change
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Merge PDFs",
                        color = Color.White,
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
        color = Colors.cardColor,
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
                    .background(Colors.blueColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = file.id.toString(),
                    color = Color.White,
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
                    .background(Color(0xFF2A2F37)), // TODO: change
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = "PDF icon",
                    tint = Color(0xFF7D8592), // TODO: change
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
                    color = Colors.textMainColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = file.meta,
                    color = Colors.textMutedColor,
                    fontSize = 11.sp,
                )
            }

            IconButton(
                onClick = { /* TODO: move up */ },
                modifier = Modifier.size(26.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = file.id > 1,
                colors = IconButtonDefaults.iconButtonColors().copy(
                    containerColor = Color(0xFF4A5462), // TODO: change
                    contentColor = Color(0xFFE0E1E3),
                    disabledContainerColor = Color(0xFF2F343C),
                    disabledContentColor = Color(0xFF696C6F),
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
                    containerColor = Color(0xFF4A5462), // TODO: change
                    contentColor = Color(0xFFE0E1E3),
                    disabledContainerColor = Color(0xFF2F343C),
                    disabledContentColor = Color(0xFF696C6F),
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
                    containerColor = Color(0xFFDA2626), // TODO: change
                    contentColor = Color(0xFFE0E1E3),
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
            tint = Colors.textMutedColor,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No PDFs selected",
            color = Colors.textMutedColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Add PDFs to merge them into a single document.",
            color = Colors.subtextMutedColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "You can reorder them by dragging.",
            color = Colors.subtextMutedColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
/* ------------------------------------------------ */