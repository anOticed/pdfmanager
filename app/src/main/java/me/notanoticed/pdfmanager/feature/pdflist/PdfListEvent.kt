/**
 * One-shot events emitted by PdfListViewModel.
 *
 * Events represent actions that should be handled once (navigation / opening overlays)
 * and are cleared after handling.
 */

package me.notanoticed.pdfmanager.feature.pdflist

import me.notanoticed.pdfmanager.core.pdf.model.PdfFile

/* -------------------- EVENTS -------------------- */
sealed class PdfListEvent {
    data class OpenMerge(val pdfs: List<PdfFile>) : PdfListEvent()
    data class OpenSplit(val pdf: PdfFile) : PdfListEvent()
    data class OpenPreview(val pdf: PdfFile): PdfListEvent()
    data class OpenDetails(val pdf: PdfFile): PdfListEvent()
}
/* ------------------------------------------------ */