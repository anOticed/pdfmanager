package me.notanoticed.pdfmanager.core.pdf.model

data class PdfDocumentMetadata(
    val title: String = "",
    val author: String = "",
    val subject: String = "",
    val keywords: String = ""
) {
    fun normalized(): PdfDocumentMetadata {
        return copy(
            title = title.trim(),
            author = author.trim(),
            subject = subject.trim(),
            keywords = keywords.trim()
        )
    }
}
