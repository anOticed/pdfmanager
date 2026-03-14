/**
 * Top bar for the PDF list tab.
 *
 * Supports three modes:
 * - selection mode
 * - normal mode
 * - search mode
 */

package me.notanoticed.pdfmanager.feature.pdflist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
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
    var menuExpanded by remember { mutableStateOf(false) }

    Column {
        TopAppBar(
            title = {
                when {
                    viewModel.isSelectionMode -> {
                        Text(
                            text = "${viewModel.selectionCount} Selected",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Colors.Text.primary
                        )
                    }

                    viewModel.isSearchMode -> {
                        BasicTextField(
                            value = viewModel.searchQuery,
                            onValueChange = viewModel::updateSearchQuery,
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 14.sp,
                                color = Colors.Text.primary
                            ),
                            cursorBrush = SolidColor(Colors.Primary.blue),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .background(
                                            color = Colors.Surface.thumbnail,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = null,
                                        tint = Colors.Icon.default
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (viewModel.searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search files",
                                                color = Colors.Text.secondary,
                                                fontSize = 14.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            }
                        )
                    }

                    else -> {
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
                }
            },
            navigationIcon = {
                when {
                    viewModel.isSelectionMode -> {
                        IconButton(onClick = { viewModel.exitSelectionMode() }) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    }

                    viewModel.isSearchMode -> {
                        IconButton(onClick = { viewModel.closeSearch() }) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "Close Search")
                        }
                    }
                }
            },
            actions = {
                when {
                    viewModel.isSelectionMode -> {
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

                    else -> {
                        if (!viewModel.isSearchMode) {
                            IconButton(
                                onClick = { viewModel.openSearch() },
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = Colors.Icon.white
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = "Search"
                                )
                            }
                        }

                        PdfListMoreMenu(
                            viewModel = viewModel,
                            expanded = menuExpanded,
                            onExpandedChange = { menuExpanded = it }
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



/* -------------------- MORE MENU -------------------- */

@Composable
private fun PdfListMoreMenu(
    viewModel: PdfListViewModel,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    IconButton(
        onClick = { onExpandedChange(true) },
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = Colors.Icon.white
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = "More"
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) },
        containerColor = Colors.Surface.card
    ) {
        DropdownMenuItem(
            text = { Text(text = "Select", color = Colors.Text.primary) },
            onClick = {
                viewModel.enterSelectionMode()
                onExpandedChange(false)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Colors.Icon.default
                )
            }
        )

        HorizontalDivider(color = Colors.Border.subtle)

        DropdownMenuItem(
            text = { Text(text = "Name", color = Colors.Text.primary) },
            onClick = {
                viewModel.updateSortType(PdfSortType.NAME)
                onExpandedChange(false)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.SortByAlpha,
                    contentDescription = null,
                    tint = Colors.Icon.default
                )
            },
            trailingIcon = {
                if (viewModel.sortType == PdfSortType.NAME) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Colors.Icon.white
                    )
                }
            }
        )

        DropdownMenuItem(
            text = { Text(text = "File size", color = Colors.Text.primary) },
            onClick = {
                viewModel.updateSortType(PdfSortType.FILE_SIZE)
                onExpandedChange(false)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Storage,
                    contentDescription = null,
                    tint = Colors.Icon.default
                )
            },
            trailingIcon = {
                if (viewModel.sortType == PdfSortType.FILE_SIZE) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Colors.Icon.white
                    )
                }
            }
        )

        DropdownMenuItem(
            text = { Text(text = "Date", color = Colors.Text.primary) },
            onClick = {
                viewModel.updateSortType(PdfSortType.DATE)
                onExpandedChange(false)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Today,
                    contentDescription = null,
                    tint = Colors.Icon.default
                )
            },
            trailingIcon = {
                if (viewModel.sortType == PdfSortType.DATE) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Colors.Icon.white
                    )
                }
            }
        )

        HorizontalDivider(color = Colors.Border.subtle)

        DropdownMenuItem(
            text = { Text(text = "Ascending", color = Colors.Text.primary) },
            onClick = {
                viewModel.updateSortOrder(PdfSortOrder.ASCENDING)
                onExpandedChange(false)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.ArrowUpward,
                    contentDescription = null,
                    tint = Colors.Icon.default
                )
            },
            trailingIcon = {
                if (viewModel.sortOrder == PdfSortOrder.ASCENDING) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Colors.Icon.white
                    )
                }
            }
        )

        DropdownMenuItem(
            text = { Text(text = "Descending", color = Colors.Text.primary) },
            onClick = {
                viewModel.updateSortOrder(PdfSortOrder.DESCENDING)
                onExpandedChange(false)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.ArrowDownward,
                    contentDescription = null,
                    tint = Colors.Icon.default
                )
            },
            trailingIcon = {
                if (viewModel.sortOrder == PdfSortOrder.DESCENDING) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Colors.Icon.white
                    )
                }
            }
        )
    }
}
/* --------------------------------------------------- */
