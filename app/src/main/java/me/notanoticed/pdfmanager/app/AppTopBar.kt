package me.notanoticed.pdfmanager.app

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import me.notanoticed.pdfmanager.core.pickers.LocalPickers
import me.notanoticed.pdfmanager.feature.merge.MergeTopBar
import me.notanoticed.pdfmanager.feature.merge.MergeViewModel
import me.notanoticed.pdfmanager.feature.pdflist.PdfListTopBar
import me.notanoticed.pdfmanager.feature.pdflist.PdfListViewModel
import me.notanoticed.pdfmanager.feature.settings.SettingsTopBar
import me.notanoticed.pdfmanager.feature.split.SplitTopBar
import me.notanoticed.pdfmanager.feature.split.SplitViewModel

/* -------------------- TOP BAR -------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentRoute: String,
    pdfListViewModel: PdfListViewModel,
    splitViewModel: SplitViewModel,
    mergeViewModel: MergeViewModel
) {
    val pickers = LocalPickers.current
    val context = LocalContext.current

    when (currentRoute) {
        Screen.PdfList.route -> PdfListTopBar(
            viewModel = pdfListViewModel,
            totalDocuments = pdfListViewModel.pdfFiles.size
        )
        Screen.Merge.route -> MergeTopBar(
            total = mergeViewModel.pdfMergeFiles.size,
            isActive = mergeViewModel.isActive,
            onAddClick = {
                if (mergeViewModel.isActive) {
                    mergeViewModel.clear()
                }
                else {
                    mergeViewModel.setMergeFiles(mergeViewModel.sampleMergeFiles)
                }
            }
        )
        Screen.Split.route -> SplitTopBar(
            viewModel = splitViewModel,
            onAddClick = { splitViewModel.pickSplitPdf(context, pickers) },
        )
        Screen.Images.route -> null
        Screen.Settings.route -> SettingsTopBar()
    }
}
/* ------------------------------------------------- */