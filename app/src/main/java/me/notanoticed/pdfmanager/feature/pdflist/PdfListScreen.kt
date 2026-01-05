package me.notanoticed.pdfmanager.feature.pdflist

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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- SCREEN -------------------- */
@Composable
fun PdfListScreen(
    modifier: Modifier = Modifier,
    viewModel: PdfListViewModel,
) {
    val context = LocalContext.current

    val isLoading = viewModel.isLoading
    val pdfFiles = viewModel.pdfFiles

    val hasData = pdfFiles.isNotEmpty()
    val showFullscreenLoader = isLoading && !hasData


    if (showFullscreenLoader) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Colors.Primary.blue)
        }
    }
    else {
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
                    containerColor = Colors.Surface.card,
                    color = Colors.Primary.blue
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
        color = Colors.Surface.card,
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
                        checkedBoxColor = Colors.Primary.blue,
                        checkedBorderColor = Colors.Primary.blue,
                        checkedCheckmarkColor = Colors.Primary.white
                    )
                )
            }
            else {
                IconButton(onClick = onMoreClick) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = "More icon",
                        tint = Colors.Icon.default
                    )
                }
            }
        }
    }
}
/* ------------------------------------------------------- */