/**
 * One-shot event bridge for the PDF list.
 *
 * Consumes pending events emitted by PdfListViewModel (open merge/split/preview/details)
 * and performs the corresponding side effects: switching tabs, updating other ViewModels,
 * or opening the preview overlay.
 */

package me.notanoticed.pdfmanager.app

import android.content.Intent
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import me.notanoticed.pdfmanager.core.pdf.PdfDocumentActions
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
                mergeViewModel.addMergeFiles(pdfFiles = event.pdfs)

                val page = tabs.indexOf(Screen.Merge.route)
                if (page >= 0) pagerState.animateScrollToPage(page)

                handleAfterNavigation(pdfListViewModel, clearSelection = true)
            }
            is PdfListEvent.OpenSplit -> {
                splitViewModel.updateSelectedSplitPdf(pdfFile = event.pdf)
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
            is PdfListEvent.OpenRenameDialog -> {
                pdfListViewModel.showRenameDialog(event.pdf)
                handleAfterNavigation(pdfListViewModel, clearSelection = false)
            }
            is PdfListEvent.OpenDeleteDialog -> {
                pdfListViewModel.showDeleteDialog(event.pdf)
                handleAfterNavigation(pdfListViewModel, clearSelection = false)
            }
            is PdfListEvent.SharePdf -> {
                runCatching {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, event.pdf.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    val chooser = Intent.createChooser(shareIntent, "Share PDF").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    context.startActivity(chooser)
                }.onFailure {
                    toast("Failed to open share menu")
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
                    toast("Failed to open print dialog")
                }
                handleAfterNavigation(pdfListViewModel, clearSelection = false)
            }
            null -> Unit
        }
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
