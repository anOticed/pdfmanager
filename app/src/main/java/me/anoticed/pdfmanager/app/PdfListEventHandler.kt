package me.anoticed.pdfmanager.app

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import me.anoticed.pdfmanager.core.pdf.model.metaLine
import me.anoticed.pdfmanager.feature.merge.MergeFile
import me.anoticed.pdfmanager.feature.merge.MergeViewModel
import me.anoticed.pdfmanager.feature.pdflist.PdfListEvent
import me.anoticed.pdfmanager.feature.pdflist.PdfListViewModel
import me.anoticed.pdfmanager.feature.split.SplitViewModel

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
                val mergeFiles = event.pdfs.mapIndexed { index, pdf ->
                    MergeFile(
                        id = index + 1,
                        name = pdf.name,
                        meta = pdf.metaLine()
                    )
                }
                mergeViewModel.setMergeFiles(mergeFiles)

                val page = tabs.indexOf(Screen.Merge.route)
                if (page >= 0) pagerState.animateScrollToPage(page)

                pdfListViewModel.closeOptions()
                pdfListViewModel.exitSelectionMode()
                pdfListViewModel.clearPendingEvent()
            }
            is PdfListEvent.OpenSplit -> {
                splitViewModel.updateSelectedSplitPdf(pdfFile = event.pdf)
                val page = tabs.indexOf(Screen.Split.route)
                if (page >= 0) pagerState.animateScrollToPage(page)

                pdfListViewModel.closeOptions()
                pdfListViewModel.clearPendingEvent()
            }
            null -> Unit
        }
    }
}
/* ------------------------------------------------------- */