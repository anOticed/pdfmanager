package com.example.pdfmanager.feature.pdflist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CallMerge
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Reorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.ui.graphics.vector.ImageVector

/* -------------------- FILE OPTIONS -------------------- */
enum class PdfFileOptionAction {
    RENAME,
    MERGE,
    SPLIT,
    COMPRESS,
    REORDER_PAGES,
    SET_PASSWORD,
    REMOVE_PASSWORD,
    PRINT,
    SHARE,
    DETAILS,
    DELETE
}


data class PdfFileOptions(
    val action: PdfFileOptionAction,
    val title: String,
    val icon: ImageVector,
    val isDestructive: Boolean = false
)


val PdfFileOptionItems: List<PdfFileOptions> = listOf(
    PdfFileOptions(
        action = PdfFileOptionAction.RENAME,
        title = "Rename",
        icon = Icons.Outlined.Edit
    ),
    PdfFileOptions(
        action = PdfFileOptionAction.MERGE,
        title = "Merge",
        icon = Icons.Outlined.CallMerge
    ),
    PdfFileOptions(
        action = PdfFileOptionAction.SPLIT,
        title = "Split",
        icon = Icons.Outlined.CallSplit
    ),
    PdfFileOptions(
        action = PdfFileOptionAction.COMPRESS,
        title = "Compress PDF",
        icon = Icons.Outlined.Compress
    ),
    PdfFileOptions(
        action = PdfFileOptionAction.REORDER_PAGES,
        title = "Reorder pages",
        icon = Icons.Outlined.Reorder
    ),
    PdfFileOptions(
        action = PdfFileOptionAction.SET_PASSWORD,
        title = "Set password",
        icon = Icons.Outlined.Lock
    ),
    PdfFileOptions(
        action = PdfFileOptionAction.REMOVE_PASSWORD,
        title = "Remove password",
        icon = Icons.Outlined.LockOpen
    ),
    PdfFileOptions(
        action = PdfFileOptionAction.PRINT,
        title = "Print",
        icon = Icons.Outlined.Print
    ),
    PdfFileOptions(
        action = PdfFileOptionAction.SHARE,
        title = "Share",
        icon = Icons.Outlined.Share
    ),
    PdfFileOptions(
        action = PdfFileOptionAction.DETAILS,
        title = "Details",
        icon = Icons.Outlined.Info
    ),
    PdfFileOptions(
        action = PdfFileOptionAction.DELETE,
        title = "Delete",
        icon = Icons.Outlined.Delete,
        isDestructive = true
    )
)
/* ------------------------------------------------------ */