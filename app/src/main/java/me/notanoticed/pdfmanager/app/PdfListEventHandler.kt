package me.notanoticed.pdfmanager.app

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import me.notanoticed.pdfmanager.feature.merge.MergeViewModel
import me.notanoticed.pdfmanager.feature.pdflist.PdfListEvent
import me.notanoticed.pdfmanager.feature.pdflist.PdfListViewModel
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