package me.notanoticed.pdfmanager.core.system.export

open class PdfWorkflowException(
    override val message: String
) : RuntimeException(message)

class PdfExportException(
    override val message: String
) : PdfWorkflowException(message)
