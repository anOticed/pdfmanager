package me.anoticed.pdfmanager.feature.pdflist

import me.anoticed.pdfmanager.core.pdf.model.PdfFile

/* -------------------- EVENTS -------------------- */
sealed class PdfListEvent {
    data class OpenMerge(val pdfs: List<PdfFile>) : PdfListEvent()
    data class OpenSplit(val pdf: PdfFile) : PdfListEvent()
}
/* ------------------------------------------------ */