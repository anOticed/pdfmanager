package me.notanoticed.pdfmanager.feature.pdflist

import me.notanoticed.pdfmanager.core.pdf.model.PdfFile


/* -------------------- EVENTS -------------------- */
sealed class PdfListAction {
    data class OpenMerge(val pdfs: List<PdfFile>) : PdfListAction()
    data class OpenSplit(val pdf: PdfFile) : PdfListAction()
    data class OpenPageEditor(val pdf: PdfFile) : PdfListAction()
    data class OpenPreview(val pdf: PdfFile): PdfListAction()
    data class OpenDetails(val pdf: PdfFile): PdfListAction()
    data class OpenMetadataDialog(val pdf: PdfFile): PdfListAction()
    data class OpenCompressDialog(val pdf: PdfFile): PdfListAction()
    data class OpenSetPasswordDialog(val pdf: PdfFile): PdfListAction()
    data class OpenRemovePasswordDialog(val pdf: PdfFile): PdfListAction()
    data class OpenRenameDialog(val pdf: PdfFile): PdfListAction()
    data class OpenDeleteDialog(val pdfs: List<PdfFile>): PdfListAction()
    data class SharePdf(val pdf: PdfFile): PdfListAction()
    data class SharePdfs(val pdfs: List<PdfFile>): PdfListAction()
    data class PrintPdf(val pdf: PdfFile): PdfListAction()
}
/* ------------------------------------------------ */
