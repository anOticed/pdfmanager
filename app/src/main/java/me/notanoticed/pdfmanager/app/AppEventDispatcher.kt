package me.notanoticed.pdfmanager.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.feature.compress.CompressViewModel
import me.notanoticed.pdfmanager.feature.merge.MergeViewModel
import me.notanoticed.pdfmanager.feature.pageeditor.LocalPageEditorNav
import me.notanoticed.pdfmanager.feature.pdflist.PdfListAction
import me.notanoticed.pdfmanager.feature.pdflist.PdfListViewModel
import me.notanoticed.pdfmanager.feature.preview.PreviewNav
import me.notanoticed.pdfmanager.feature.split.SplitViewModel
import me.notanoticed.pdfmanager.core.pdf.catalog.PdfFileService
import me.notanoticed.pdfmanager.core.system.toast.rememberToast

@Composable
fun AppEventDispatcher(
    pdfListViewModel: PdfListViewModel,
    mergeViewModel: MergeViewModel,
    splitViewModel: SplitViewModel,
    compressViewModel: CompressViewModel,
    previewNav: PreviewNav,
    onNavigate: (AppDestination) -> Unit
) {
    val context = LocalContext.current
    val pageEditorNav = LocalPageEditorNav.current
    val toast = rememberToast()
    val action = pdfListViewModel.pendingEvent

    LaunchedEffect(action) {
        when (action) {
            is PdfListAction.OpenMerge -> {
                mergeViewModel.addMergeFiles(context, action.pdfs)
                onNavigate(AppDestination.Merge)
                pdfListViewModel.finishAction(clearSelection = true)
            }

            is PdfListAction.OpenSplit -> {
                splitViewModel.updateSelectedSplitPdf(context, action.pdf)
                onNavigate(AppDestination.Split)
                pdfListViewModel.finishAction(clearSelection = false)
            }

            is PdfListAction.OpenPreview -> {
                previewNav.openSingle(pdf = action.pdf)
                pdfListViewModel.finishAction(clearSelection = false)
            }

            is PdfListAction.OpenPageEditor -> {
                pageEditorNav.open(pdf = action.pdf)
                pdfListViewModel.finishAction(clearSelection = false)
            }

            is PdfListAction.OpenDetails -> {
                pdfListViewModel.openDetails(action.pdf)
                pdfListViewModel.finishAction(clearSelection = false)
            }

            is PdfListAction.OpenMetadataDialog -> {
                pdfListViewModel.showMetadataDialog(context, action.pdf)
                pdfListViewModel.finishAction(clearSelection = false)
            }

            is PdfListAction.OpenCompressDialog -> {
                compressViewModel.open(action.pdf)
                pdfListViewModel.finishAction(clearSelection = false)
            }

            is PdfListAction.OpenSetPasswordDialog -> {
                pdfListViewModel.showSetPasswordDialog(action.pdf)
                pdfListViewModel.finishAction(clearSelection = false)
            }

            is PdfListAction.OpenRemovePasswordDialog -> {
                pdfListViewModel.showRemovePasswordDialog(action.pdf)
                pdfListViewModel.finishAction(clearSelection = false)
            }

            is PdfListAction.OpenRenameDialog -> {
                pdfListViewModel.showRenameDialog(action.pdf)
                pdfListViewModel.finishAction(clearSelection = false)
            }

            is PdfListAction.OpenDeleteDialog -> {
                pdfListViewModel.showDeleteDialog(action.pdfs)
                pdfListViewModel.finishAction(clearSelection = false)
            }

            is PdfListAction.SharePdf -> {
                pdfListViewModel.sharePdfs(context, listOf(action.pdf), onFailure = {
                    toast(context.getString(R.string.pdflist_share_menu_failed))
                })
                pdfListViewModel.finishAction(clearSelection = false)
            }

            is PdfListAction.SharePdfs -> {
                pdfListViewModel.sharePdfs(context, action.pdfs, onFailure = {
                    toast(context.getString(R.string.pdflist_share_menu_failed))
                })
                pdfListViewModel.finishAction(clearSelection = false)
            }

            is PdfListAction.PrintPdf -> {
                runCatching {
                    PdfFileService.printPdf(
                        context = context,
                        pdfUri = action.pdf.uri,
                        documentName = action.pdf.name
                    )
                }.onFailure {
                    toast(context.getString(R.string.pdflist_print_dialog_failed))
                }
                pdfListViewModel.finishAction(clearSelection = false)
            }

            null -> Unit
        }
    }
}
