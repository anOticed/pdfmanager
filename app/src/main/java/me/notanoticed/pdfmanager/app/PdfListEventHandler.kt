/**
 * One-shot event bridge for the PDF list.
 *
 * Consumes pending events emitted by PdfListViewModel (open merge/split/preview/details)
 * and performs the corresponding side effects: switching tabs, updating other ViewModels,
 * or opening the preview overlay.
 */

package me.notanoticed.pdfmanager.app

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.pdf.PdfDocumentActions
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.toast.rememberToast
import me.notanoticed.pdfmanager.feature.merge.MergeViewModel
import me.notanoticed.pdfmanager.feature.pdflist.PdfListEvent
import me.notanoticed.pdfmanager.feature.pdflist.PdfListViewModel
import me.notanoticed.pdfmanager.feature.preview.LocalPreviewNav
import me.notanoticed.pdfmanager.feature.split.SplitViewModel

/* -------------------- EVENT HANDLER -------------------- */
@Composable
fun PdfListEventHandler(
    pdfListViewModel: PdfListViewModel,
    mergeViewModel: MergeViewModel,
    splitViewModel: SplitViewModel,
    pagerState: PagerState,
    tabs: List<String>
) {
    val event = pdfListViewModel.pendingEvent
    val previewNav = LocalPreviewNav.current
    val context = LocalContext.current
    val toast = rememberToast()

    LaunchedEffect(event) {
        when (event) {
            is PdfListEvent.OpenMerge -> {
                mergeViewModel.addMergeFiles(
                    context = context,
                    pdfFiles = event.pdfs
                )

                val page = tabs.indexOf(Screen.Merge.route)
                if (page >= 0) pagerState.animateScrollToPage(page)

                handleAfterNavigation(pdfListViewModel, clearSelection = true)
            }
            is PdfListEvent.OpenSplit -> {
                splitViewModel.updateSelectedSplitPdf(
                    context = context,
                    pdfFile = event.pdf
                )
                val page = tabs.indexOf(Screen.Split.route)
                if (page >= 0) pagerState.animateScrollToPage(page)

                handleAfterNavigation(pdfListViewModel, clearSelection = false)
            }
            is PdfListEvent.OpenPreview -> {
                previewNav.openSingle(pdf = event.pdf)
                handleAfterNavigation(pdfListViewModel, clearSelection = false)
            }
            is PdfListEvent.OpenDetails -> {
                pdfListViewModel.openDetails(event.pdf)
                handleAfterNavigation(pdfListViewModel, clearSelection = false)
            }
            is PdfListEvent.OpenMetadataDialog -> {
                pdfListViewModel.showMetadataDialog(context, event.pdf)
                handleAfterNavigation(pdfListViewModel, clearSelection = false)
            }
            is PdfListEvent.OpenSetPasswordDialog -> {
                pdfListViewModel.showSetPasswordDialog(event.pdf)
                handleAfterNavigation(pdfListViewModel, clearSelection = false)
            }
            is PdfListEvent.OpenRemovePasswordDialog -> {
                pdfListViewModel.showRemovePasswordDialog(event.pdf)
                handleAfterNavigation(pdfListViewModel, clearSelection = false)
            }
            is PdfListEvent.OpenRenameDialog -> {
                pdfListViewModel.showRenameDialog(event.pdf)
                handleAfterNavigation(pdfListViewModel, clearSelection = false)
            }
            is PdfListEvent.OpenDeleteDialog -> {
                pdfListViewModel.showDeleteDialog(event.pdfs)
                handleAfterNavigation(pdfListViewModel, clearSelection = false)
            }
            is PdfListEvent.SharePdf -> {
                runCatching {
                    context.startActivity(
                        buildShareIntent(
                            context = context,
                            pdfs = listOf(event.pdf)
                        )
                    )
                }.onFailure {
                    toast(context.getString(R.string.pdflist_share_menu_failed))
                }
                handleAfterNavigation(pdfListViewModel, clearSelection = false)
            }
            is PdfListEvent.SharePdfs -> {
                runCatching {
                    context.startActivity(
                        buildShareIntent(
                            context = context,
                            pdfs = event.pdfs
                        )
                    )
                }.onFailure {
                    toast(context.getString(R.string.pdflist_share_menu_failed))
                }
                handleAfterNavigation(pdfListViewModel, clearSelection = false)
            }
            is PdfListEvent.PrintPdf -> {
                runCatching {
                    PdfDocumentActions.printPdf(
                        context = context,
                        pdfUri = event.pdf.uri,
                        documentName = event.pdf.name
                    )
                }.onFailure {
                    toast(context.getString(R.string.pdflist_print_dialog_failed))
                }
                handleAfterNavigation(pdfListViewModel, clearSelection = false)
            }
            null -> Unit
        }
    }
}


private fun buildShareIntent(
    context: Context,
    pdfs: List<PdfFile>
): Intent {
    val shareIntent = if (pdfs.size == 1) {
        Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfs.first().uri)
        }
    } else {
        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "application/pdf"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(pdfs.map { it.uri }))
        }
    }

    val clipData = ClipData.newUri(
        context.contentResolver,
        pdfs.first().name,
        pdfs.first().uri
    ).apply {
        pdfs.drop(1).forEach { pdf ->
            addItem(ClipData.Item(pdf.uri))
        }
    }

    shareIntent.clipData = clipData
    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    return Intent.createChooser(
        shareIntent,
        context.resources.getQuantityString(
            R.plurals.pdflist_share_chooser_title,
            pdfs.size
        )
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}


private fun handleAfterNavigation(
    pdfListViewModel: PdfListViewModel,
    clearSelection: Boolean
) {
    pdfListViewModel.closeOptions()
    if (clearSelection) {
        pdfListViewModel.exitSelectionMode()
    }
    pdfListViewModel.clearPendingEvent()
}
/* ------------------------------------------------------- */
