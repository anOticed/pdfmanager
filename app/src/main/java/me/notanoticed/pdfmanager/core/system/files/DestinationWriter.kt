package me.notanoticed.pdfmanager.core.system.files

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.system.export.PdfExportException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object DestinationWriter {
    fun copyUriToFile(
        context: Context,
        sourceUri: Uri,
        outputFile: File,
        openSourceErrorMessage: String = context.getString(R.string.output_save_failed)
    ) {
        val inputStream = context.contentResolver.openInputStream(sourceUri)
            ?: throw PdfExportException(openSourceErrorMessage)

        inputStream.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
                output.flush()
            }
        }
    }

    fun copyFileToUri(
        context: Context,
        sourceFile: File,
        destinationUri: Uri
    ) {
        val resolver = context.contentResolver
        resolver.openFileDescriptor(destinationUri, "rwt")?.use { descriptor ->
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(descriptor.fileDescriptor).use { output ->
                    input.copyTo(output)
                    output.flush()
                    runCatching { descriptor.fileDescriptor.sync() }
                }
            }
        } ?: throw PdfExportException(context.getString(R.string.output_open_destination_failed))
    }

    fun writePdfDocumentInTree(
        context: Context,
        treeUri: Uri,
        displayName: String,
        sourceFile: File
    ): Uri {
        val outputUri = createPdfDocumentInTree(
            context = context,
            treeUri = treeUri,
            displayName = displayName
        )

        return try {
            copyFileToUri(
                context = context,
                sourceFile = sourceFile,
                destinationUri = outputUri
            )
            outputUri
        } catch (error: Throwable) {
            deleteUri(context, outputUri)
            throw error
        }
    }

    fun deleteUri(
        context: Context,
        uri: Uri
    ) {
        runCatching {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                DocumentsContract.deleteDocument(context.contentResolver, uri)
            } else {
                context.contentResolver.delete(uri, null, null)
            }
        }
    }

    private fun createPdfDocumentInTree(
        context: Context,
        treeUri: Uri,
        displayName: String
    ): Uri {
        val resolver = context.contentResolver
        val treeDocumentId = DocumentsContract.getTreeDocumentId(treeUri)
        val parentDocumentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocumentId)

        return DocumentsContract.createDocument(
            resolver,
            parentDocumentUri,
            "application/pdf",
            displayName
        ) ?: throw PdfExportException(context.getString(R.string.output_create_document_failed))
    }
}
