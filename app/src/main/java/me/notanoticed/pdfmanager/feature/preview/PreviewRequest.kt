package me.notanoticed.pdfmanager.feature.preview

import me.notanoticed.pdfmanager.core.pdf.model.PdfFile

/* -------------------- PREVIEW REQUEST -------------------- */
sealed interface PreviewRequest {
    val topBarTitle: String

    data class Single(val pdf: PdfFile): PreviewRequest {
        override val topBarTitle: String = pdf.name
    }

    data class Merge(val pdfs: List<PdfFile>): PreviewRequest {
        override val topBarTitle: String = "Preview"
    }

    data class Split(val pdf: PdfFile, val splitMethodId: Int): PreviewRequest {
        override val topBarTitle: String = "Preview"
    }
}
/* --------------------------------------------------------- */