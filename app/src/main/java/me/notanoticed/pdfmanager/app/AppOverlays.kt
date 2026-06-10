package me.notanoticed.pdfmanager.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import me.notanoticed.pdfmanager.feature.compress.CompressSheet
import me.notanoticed.pdfmanager.feature.compress.CompressViewModel
import me.notanoticed.pdfmanager.feature.pdflist.DeletePdfDialog
import me.notanoticed.pdfmanager.feature.pdflist.EditPdfMetadataDialog
import me.notanoticed.pdfmanager.feature.pdflist.PdfDetailsOverlay
import me.notanoticed.pdfmanager.feature.pdflist.PdfListOptionsOverlay
import me.notanoticed.pdfmanager.feature.pdflist.PdfPasswordDialog
import me.notanoticed.pdfmanager.feature.pdflist.PdfListViewModel
import me.notanoticed.pdfmanager.feature.pdflist.RenamePdfDialog

@Composable
fun AppOverlays(
    pdfListViewModel: PdfListViewModel,
    compressViewModel: CompressViewModel
) {
    val context = LocalContext.current

    PdfListOptionsOverlay(
        visible = pdfListViewModel.optionsPanelVisible,
        pdf = pdfListViewModel.optionsPanelPdf,
        onDismiss = pdfListViewModel::closeOptions,
        onAction = { action ->
            val pdf = pdfListViewModel.optionsPanelPdf ?: return@PdfListOptionsOverlay
            pdfListViewModel.closeOptions()
            pdfListViewModel.onFileOptionSelected(action, pdf)
        }
    )

    PdfDetailsOverlay(
        visible = pdfListViewModel.detailsPanelVisible,
        pdf = pdfListViewModel.detailsPanelPdf,
        onDismiss = pdfListViewModel::closeDetails
    )

    RenamePdfDialog(
        visible = pdfListViewModel.renameDialogVisible,
        currentName = pdfListViewModel.renameDialogPdf?.name.orEmpty(),
        inputValue = pdfListViewModel.renameInput,
        isProcessing = pdfListViewModel.isFileActionInProgress,
        onValueChange = pdfListViewModel::updateRenameInput,
        onDismiss = pdfListViewModel::closeRenameDialog,
        onConfirm = { pdfListViewModel.confirmRename(context) }
    )

    EditPdfMetadataDialog(
        viewModel = pdfListViewModel,
        onDismiss = pdfListViewModel::closeMetadataDialog,
        onConfirm = { pdfListViewModel.confirmMetadataUpdate(context) }
    )

    PdfPasswordDialog(
        viewModel = pdfListViewModel,
        onDismiss = pdfListViewModel::closePasswordDialog,
        onConfirm = { pdfListViewModel.confirmPasswordAction(context) }
    )

    DeletePdfDialog(
        visible = pdfListViewModel.deleteDialogVisible,
        fileName = pdfListViewModel.deleteDialogPdfs.firstOrNull()?.name,
        fileCount = pdfListViewModel.deleteDialogPdfs.size,
        isProcessing = pdfListViewModel.isFileActionInProgress,
        onDismiss = pdfListViewModel::closeDeleteDialog,
        onConfirm = { pdfListViewModel.confirmDelete(context) }
    )

    CompressSheet(viewModel = compressViewModel)
}
