package me.notanoticed.pdfmanager.feature.pdflist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- BOTTOM BAR -------------------- */
@Composable
fun PdfListSelectionBottomBar(
    viewModel: PdfListViewModel
) {
    Surface(
        color = Colors.Surface.card,
        shadowElevation = 8.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Colors.Border.subtle)
            )

            NavigationBar(
                containerColor = Colors.Surface.card,
            ) {
                PdfSelectionAction(
                    icon = Icons.Outlined.CallMerge,
                    tint = Colors.Primary.green,
                    label = "Merge",
                    onClick = { viewModel.mergeSelected() },
                    enabled = viewModel.canMergeSelected
                )
                PdfSelectionAction(
                    icon = Icons.Outlined.Share,
                    label = "Share",
                    onClick = { viewModel.shareSelected() },
                    enabled = viewModel.selectionCount > 0
                )
                PdfSelectionAction(
                    icon = Icons.Outlined.Delete,
                    tint = Colors.Primary.red,
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
    tint: Color = Colors.Primary.lightGray,
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
            unselectedIconColor = tint,
            unselectedTextColor = tint,
            disabledIconColor = tint.copy(alpha = 0.5f),
            disabledTextColor = tint.copy(alpha = 0.5f),
        )
    )
}
/* ---------------------------------------------------- */