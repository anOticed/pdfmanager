package me.notanoticed.pdfmanager.feature.pdflist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- TOP BAR -------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfListTopBar(
    viewModel: PdfListViewModel,
    totalDocuments: Int
) {
    Column {
        TopAppBar(
            title = {
                if (viewModel.isSelectionMode) {
                    Text(
                        text = "${viewModel.selectionCount} Selected",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Colors.Text.primary
                    )
                }
                else {
                    Column {
                        Text(
                            text = "All Files",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Colors.Text.primary
                        )
                        Text(
                            text = "$totalDocuments documents",
                            fontSize = 12.sp,
                            color = Colors.Text.secondary
                        )
                    }
                }
            },
            navigationIcon = {
                if (viewModel.isSelectionMode) {
                    IconButton(onClick = { viewModel.exitSelectionMode() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            },
            actions = {
                if (viewModel.isSelectionMode) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Select All",
                            fontSize = 14.sp,
                            color = Colors.Text.primary
                        )

                        Checkbox(
                            checked = viewModel.isAllSelected,
                            onCheckedChange = { viewModel.toggleSelectAll() },
                            colors = CheckboxDefaults.colors().copy(
                                checkedBoxColor = Colors.Primary.white,
                                checkedBorderColor = Colors.Primary.white,
                                checkedCheckmarkColor = Colors.Primary.charcoal
                            )
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Colors.Surface.card,
                titleContentColor = Colors.Primary.white,
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Colors.Border.subtle)
        )
    }
}
/* ------------------------------------------------- */