package me.notanoticed.pdfmanager.feature.preview

import me.notanoticed.pdfmanager.core.pdf.util.PagesPerSheetOption
import me.notanoticed.pdfmanager.core.pdf.edit.SplitPlan
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile

sealed interface PreviewRequest {
    val topBarTitle: String?

    data class Single(
        val pdf: PdfFile,
        val allowSearch: Boolean = true,
        override val topBarTitle: String? = pdf.name
    ): PreviewRequest

    data class Split(
        val pdf: PdfFile,
        val plan: SplitPlan,
        val pagesPerSheet: PagesPerSheetOption
    ): PreviewRequest {
        override val topBarTitle: String? = null
    }
}
