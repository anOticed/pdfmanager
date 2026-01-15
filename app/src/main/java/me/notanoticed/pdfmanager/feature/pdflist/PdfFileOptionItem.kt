/**
 * Reusable UI row used in the PDF options overlay.
 */

package me.notanoticed.pdfmanager.feature.pdflist

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import me.notanoticed.pdfmanager.ui.theme.Colors

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


data class PdfFileOptionItem(
    val action: PdfFileOptionAction,
    val title: String,
    val icon: ImageVector,
    val tint: Color = Colors.Icon.default
)


val pdfFileOptionItems: List<PdfFileOptionItem> = listOf(
    PdfFileOptionItem(
        action = PdfFileOptionAction.RENAME,
        title = "Rename",
        icon = Icons.Outlined.Edit
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.MERGE,
        title = "Merge",
        icon = Icons.Outlined.CallMerge,
        tint = Colors.Icon.merge
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.SPLIT,
        title = "Split",
        icon = Icons.Outlined.CallSplit
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.COMPRESS,
        title = "Compress PDF",
        icon = Icons.Outlined.Compress
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.REORDER_PAGES,
        title = "Reorder pages",
        icon = Icons.Outlined.Reorder
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.SET_PASSWORD,
        title = "Set password",
        icon = Icons.Outlined.Lock,
        tint = Colors.Icon.lock
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.REMOVE_PASSWORD,
        title = "Remove password",
        icon = Icons.Outlined.LockOpen,
        tint = Colors.Icon.lock
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.PRINT,
        title = "Print",
        icon = Icons.Outlined.Print
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.SHARE,
        title = "Share",
        icon = Icons.Outlined.Share
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.DETAILS,
        title = "Details",
        icon = Icons.Outlined.Info
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.DELETE,
        title = "Delete",
        icon = Icons.Outlined.Delete,
        tint = Colors.Icon.delete
    )
)
/* ------------------------------------------------------ */