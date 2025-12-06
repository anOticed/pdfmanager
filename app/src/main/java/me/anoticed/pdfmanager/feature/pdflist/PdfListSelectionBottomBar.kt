package me.anoticed.pdfmanager.feature.pdflist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CallMerge
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.anoticed.pdfmanager.ui.theme.Colors

@Composable
fun PdfListSelectionBottomBar(
    viewModel: PdfListViewModel
) {
    Surface(
        color = Colors.cardColor,
        shadowElevation = 8.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.Gray.copy(alpha = 0.2f)) // TODO: change
            )

            NavigationBar(
                containerColor = Colors.cardColor.copy(alpha = 0.9f)
            ) {
                PdfSelectionAction(
                    icon = Icons.Outlined.CallMerge,
                    color = Color(0xFF059568),
                    label = "Merge",
                    onClick = { viewModel.mergeSelected() },
                    enabled = viewModel.selectionCount > 0
                )
                PdfSelectionAction(
                    icon = Icons.Outlined.Share,
                    label = "Share",
                    onClick = { viewModel.shareSelected() },
                    enabled = viewModel.selectionCount > 0
                )
                PdfSelectionAction(
                    icon = Icons.Outlined.Delete,
                    color = Color(0xFFE82D2C),
                    label = "Delete",
                    onClick = { viewModel.deleteSelectedPdfs() },
                    enabled = viewModel.selectionCount > 0
                )
            }
        }
    }
}


@Composable
fun RowScope.PdfSelectionAction(
    icon: ImageVector,
    color: Color = Colors.textMutedColor,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    NavigationBarItem(
        icon = { Icon(imageVector = icon, contentDescription = label) },
        label = {Text(text = label, fontSize = 12.sp)},
        onClick = { onClick() },
        enabled = enabled,
        selected = false,
        colors = NavigationBarItemDefaults.colors().copy(
            unselectedIconColor = color,
            unselectedTextColor = color,
            disabledIconColor = color.copy(alpha = 0.5f),
            disabledTextColor = color.copy(alpha = 0.5f),
        )
    )
}