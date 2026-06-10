package me.notanoticed.pdfmanager.core.pdf.catalog

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.pdf.model.PdfDocumentMetadata
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.pdf.util.ensurePdfBoxInitialized
import me.notanoticed.pdfmanager.core.system.files.DestinationWriter
import me.notanoticed.pdfmanager.core.system.files.TempFileStore
import me.notanoticed.pdfmanager.core.system.export.PdfWorkflowException

object PdfMetadataService {
    fun readPdfMetadata(
        context: Context,
        pdfUri: Uri
    ): PdfDocumentMetadata {
        ensurePdfBoxInitialized(context)

        val inputStream = context.contentResolver.openInputStream(pdfUri)
            ?: throw PdfWorkflowException(context.getString(R.string.pdflist_metadata_load_failed))

        inputStream.use { stream ->
            PDDocument.load(stream).use { document ->
                val info = document.documentInformation ?: PDDocumentInformation()
                return PdfDocumentMetadata(
                    title = info.title.orEmpty(),
                    author = info.author.orEmpty(),
                    subject = info.subject.orEmpty(),
                    keywords = info.keywords.orEmpty()
                )
            }
        }
    }

    fun updatePdfMetadata(
        context: Context,
        pdf: PdfFile,
        metadata: PdfDocumentMetadata
    ): Boolean {
        ensurePdfBoxInitialized(context)

        val tempFile = TempFileStore.createTempPdfFile(
            context = context,
            directoryName = "pdf_metadata_edit",
            filePrefix = "metadata_edit_"
        )

        return try {
            val inputStream = context.contentResolver.openInputStream(pdf.uri)
                ?: return false

            inputStream.use { stream ->
                PDDocument.load(stream).use { document ->
                    val info = document.documentInformation ?: PDDocumentInformation()

                    info.title = metadata.title
                    info.author = metadata.author
                    info.subject = metadata.subject
                    info.keywords = metadata.keywords

                    document.documentInformation = info
                    document.save(tempFile)
                }
            }

            DestinationWriter.copyFileToUri(
                context = context,
                destinationUri = pdf.uri,
                sourceFile = tempFile
            )
            true
        } catch (_: Exception) {
            false
        } finally {
            runCatching { tempFile.delete() }
        }
    }
}
