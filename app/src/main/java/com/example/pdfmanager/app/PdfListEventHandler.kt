package com.example.pdfmanager.app

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.pdfmanager.feature.merge.MergeFile
import com.example.pdfmanager.feature.merge.MergeViewModel
import com.example.pdfmanager.feature.pdflist.PdfListEvent
import com.example.pdfmanager.feature.pdflist.PdfListViewModel
import com.example.pdfmanager.feature.split.SplitViewModel

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
                mergeViewModel.setMergeFiles(
                    listOf(
                        MergeFile(id = 1, name = "test.pdf", pages = "1 page", size = "1.0 MB")
                    )
                )
                val page = tabs.indexOf(Screen.Merge.route)
                if (page >= 0) pagerState.animateScrollToPage(page)

                pdfListViewModel.closeOptions()
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