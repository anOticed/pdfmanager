/**
 * Top bar switcher for the main Scaffold.
 *
 * Selects the appropriate top bar based on the current tab and feature state
 * (for example, merge/split/images "active" states).
 */

package me.notanoticed.pdfmanager.app

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import me.notanoticed.pdfmanager.core.pickers.LocalPickers
import me.notanoticed.pdfmanager.feature.images.ImagesTopBar
import me.notanoticed.pdfmanager.feature.images.ImagesViewModel
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
    mergeViewModel: MergeViewModel,
    imagesViewModel: ImagesViewModel
) {
    val pickers = LocalPickers.current
    val context = LocalContext.current

    when (currentRoute) {
        Screen.PdfList.route -> PdfListTopBar(
            viewModel = pdfListViewModel,
            totalDocuments = pdfListViewModel.pdfFiles.size
        )
        Screen.Merge.route -> MergeTopBar(
            viewModel = mergeViewModel,
            onAddClick = { mergeViewModel.pickMergePdfs(context, pickers) },
            onCloseClick = { mergeViewModel.clear() }
        )
        Screen.Split.route -> SplitTopBar(
            viewModel = splitViewModel,
            onAddClick = { splitViewModel.pickSplitPdf(context, pickers) },
            onCloseClick = { splitViewModel.closeSelectedSplitPdf() }
        )
        Screen.Images.route -> ImagesTopBar(
            viewModel = imagesViewModel,
            onCameraClick = { /* TODO: camera */ },
            onGalleryClick = { imagesViewModel.pickFromGalleryStub() },
            onCloseClick = { imagesViewModel.clear() }
        )
        Screen.Settings.route -> SettingsTopBar()
    }
}
/* ------------------------------------------------- */