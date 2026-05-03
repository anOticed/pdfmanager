/**
 * Reusable UI row used in the PDF options overlay.
 */

package me.notanoticed.pdfmanager.feature.pdflist

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CallMerge
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Reorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- FILE OPTIONS -------------------- */
enum class PdfFileOptionAction {
    RENAME,
    EDIT_METADATA,
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
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val tint: Color = Colors.Icon.default
)


val pdfFileOptionItems: List<PdfFileOptionItem> = listOf(
    PdfFileOptionItem(
        action = PdfFileOptionAction.RENAME,
        titleRes = R.string.pdf_option_rename,
        icon = Icons.Outlined.Edit,
        tint = Colors.Icon.rename
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.EDIT_METADATA,
        titleRes = R.string.pdf_option_edit_metadata,
        icon = Icons.Outlined.Description,
        tint = Colors.Icon.blue
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.MERGE,
        titleRes = R.string.pdf_option_merge,
        icon = Icons.Outlined.CallMerge,
        tint = Colors.Icon.merge
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.SPLIT,
        titleRes = R.string.pdf_option_split,
        icon = Icons.Outlined.CallSplit,
        tint = Colors.Icon.split
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.COMPRESS,
        titleRes = R.string.pdf_option_compress,
        icon = Icons.Outlined.Compress
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.REORDER_PAGES,
        titleRes = R.string.pdf_option_reorder_pages,
        icon = Icons.Outlined.Reorder
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.SET_PASSWORD,
        titleRes = R.string.pdf_option_set_password,
        icon = Icons.Outlined.Lock,
        tint = Colors.Icon.lock
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.REMOVE_PASSWORD,
        titleRes = R.string.pdf_option_remove_password,
        icon = Icons.Outlined.LockOpen,
        tint = Colors.Icon.lock
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.PRINT,
        titleRes = R.string.pdf_option_print,
        icon = Icons.Outlined.Print,
        tint = Colors.Icon.print
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.SHARE,
        titleRes = R.string.pdf_option_share,
        icon = Icons.Outlined.Share,
        tint = Colors.Icon.share
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.DETAILS,
        titleRes = R.string.pdf_option_details,
        icon = Icons.Outlined.Info
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.DELETE,
        titleRes = R.string.pdf_option_delete,
        icon = Icons.Outlined.Delete,
        tint = Colors.Icon.delete
    )
)
/* ------------------------------------------------------ */
