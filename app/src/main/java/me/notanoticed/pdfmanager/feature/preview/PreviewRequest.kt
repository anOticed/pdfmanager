/**
 * Preview input model.
 *
 * Defines the supported preview modes (single PDF, split stub) and
 * the data needed to render each mode.
 */

package me.notanoticed.pdfmanager.feature.preview

import me.notanoticed.pdfmanager.core.pdf.model.PdfFile

/* -------------------- PREVIEW REQUEST -------------------- */
sealed interface PreviewRequest {
    val topBarTitle: String

    data class Single(
        val pdf: PdfFile,
        val allowSearch: Boolean = true
    ): PreviewRequest {
        override val topBarTitle: String = pdf.name
    }

    data class Split(val pdf: PdfFile, val splitMethodId: Int): PreviewRequest {
        override val topBarTitle: String = "Preview"
    }
}
/* --------------------------------------------------------- */
