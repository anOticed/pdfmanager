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
import androidx.compose.ui.graphics.vector.ImageVector
import me.notanoticed.pdfmanager.R

/* -------------------- FILE OPTIONS -------------------- */
enum class PdfFileOptionAction {
    RENAME,
    EDIT_METADATA,
    MERGE,
    SPLIT,
    COMPRESS,
    EDIT_PAGES,
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
    val icon: ImageVector
)


val pdfFileOptionItems: List<PdfFileOptionItem> = listOf(
    PdfFileOptionItem(
        action = PdfFileOptionAction.RENAME,
        titleRes = R.string.pdf_option_rename,
        icon = Icons.Outlined.Edit
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.EDIT_METADATA,
        titleRes = R.string.pdf_option_edit_metadata,
        icon = Icons.Outlined.Description
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.MERGE,
        titleRes = R.string.pdf_option_merge,
        icon = Icons.Outlined.CallMerge
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.SPLIT,
        titleRes = R.string.pdf_option_split,
        icon = Icons.Outlined.CallSplit
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.COMPRESS,
        titleRes = R.string.pdf_option_compress,
        icon = Icons.Outlined.Compress
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.EDIT_PAGES,
        titleRes = R.string.pdf_option_edit_pages,
        icon = Icons.Outlined.Reorder
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.SET_PASSWORD,
        titleRes = R.string.pdf_option_set_password,
        icon = Icons.Outlined.Lock
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.REMOVE_PASSWORD,
        titleRes = R.string.pdf_option_remove_password,
        icon = Icons.Outlined.LockOpen
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.PRINT,
        titleRes = R.string.pdf_option_print,
        icon = Icons.Outlined.Print
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.SHARE,
        titleRes = R.string.pdf_option_share,
        icon = Icons.Outlined.Share
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.DETAILS,
        titleRes = R.string.pdf_option_details,
        icon = Icons.Outlined.Info
    ),
    PdfFileOptionItem(
        action = PdfFileOptionAction.DELETE,
        titleRes = R.string.pdf_option_delete,
        icon = Icons.Outlined.Delete
    )
)
/* ------------------------------------------------------ */
