package me.notanoticed.pdfmanager.core.pdf

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.provider.DocumentsContract
import android.provider.MediaStore
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object PdfDocumentActions {
    private const val PDF_EXTENSION = ".pdf"
    private val INVALID_FILE_NAME_CHARS = Regex("""[\\/:*?"<>|]""")

    private fun String.removePdfSuffix(): String {
        return if (endsWith(PDF_EXTENSION, ignoreCase = true)) {
            dropLast(PDF_EXTENSION.length)
        } else {
            this
        }
    }

    private fun String.ensurePdfSuffix(): String {
        return if (endsWith(PDF_EXTENSION, ignoreCase = true)) {
            this
        } else {
            this + PDF_EXTENSION
        }
    }

    private fun sanitizeFileName(rawName: String): String {
        return rawName
            .trim()
            .replace(INVALID_FILE_NAME_CHARS, "_")
            .trim()
    }

    fun normalizeBaseName(rawName: String, fallbackName: String): String {
        val sanitized = sanitizeFileName(rawName)

        val fallbackBaseName = fallbackName
            .let(::sanitizeFileName)
            .removePdfSuffix()
            .ifBlank { "document" }

        return sanitized
            .removePdfSuffix()
            .ifBlank { fallbackBaseName }
    }

    fun normalizeDisplayName(rawName: String, fallbackName: String): String {
        return normalizeBaseName(
            rawName = rawName,
            fallbackName = fallbackName
        )
            .ensurePdfSuffix()
    }

    fun renamePdf(
        context: Context,
        pdf: PdfFile,
        newDisplayName: String
    ): Boolean {
        val contentResolver = context.contentResolver
        val uri = pdf.uri

        return when {
            uri.scheme == "file" -> {
                val sourceFile = uri.path?.let(::File) ?: return false
                if (!sourceFile.exists()) return false

                val targetFile = File(sourceFile.parentFile ?: return false, newDisplayName)
                if (targetFile.exists()) return false
                sourceFile.renameTo(targetFile)
            }

            DocumentsContract.isDocumentUri(context, uri) -> {
                runCatching {
                    DocumentsContract.renameDocument(contentResolver, uri, newDisplayName) != null
                }.getOrDefault(false)
            }

            else -> {
                runCatching {
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, newDisplayName)
                    }
                    contentResolver.update(uri, values, null, null) > 0
                }.getOrDefault(false)
            }
        }
    }

    fun deletePdf(
        context: Context,
        pdf: PdfFile
    ): Boolean {
        val contentResolver = context.contentResolver
        val uri = pdf.uri

        return when {
            uri.scheme == "file" -> {
                val sourceFile = uri.path?.let(::File) ?: return false
                sourceFile.exists() && sourceFile.delete()
            }

            DocumentsContract.isDocumentUri(context, uri) -> {
                runCatching {
                    DocumentsContract.deleteDocument(contentResolver, uri)
                }.getOrDefault(false)
            }

            else -> {
                runCatching {
                    contentResolver.delete(uri, null, null) > 0
                }.getOrDefault(false)
            }
        }
    }

    fun printPdf(
        context: Context,
        pdfUri: Uri,
        documentName: String
    ) {
        val printManager = context.getSystemService(PrintManager::class.java)
            ?: error("Print service is unavailable")

        printManager.print(
            documentName,
            UriPrintDocumentAdapter(
                context = context,
                pdfUri = pdfUri,
                documentName = documentName
            ),
            null
        )
    }

    private class UriPrintDocumentAdapter(
        private val context: Context,
        private val pdfUri: Uri,
        private val documentName: String
    ) : PrintDocumentAdapter() {
        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes,
            cancellationSignal: CancellationSignal,
            callback: LayoutResultCallback,
            extras: Bundle?
        ) {
            if (cancellationSignal.isCanceled) {
                callback.onLayoutCancelled()
                return
            }

            callback.onLayoutFinished(
                PrintDocumentInfo.Builder(documentName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build(),
                true
            )
        }

        override fun onWrite(
            pages: Array<out PageRange>,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal,
            callback: WriteResultCallback
        ) {
            try {
                context.contentResolver.openFileDescriptor(pdfUri, "r")?.use { inputDescriptor ->
                    FileInputStream(inputDescriptor.fileDescriptor).use { input ->
                        FileOutputStream(destination.fileDescriptor).use { output ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            while (true) {
                                if (cancellationSignal.isCanceled) {
                                    callback.onWriteCancelled()
                                    return
                                }

                                val bytesRead = input.read(buffer)
                                if (bytesRead <= 0) break
                                output.write(buffer, 0, bytesRead)
                            }

                            output.flush()
                        }
                    }
                } ?: error("Failed to open source PDF")

                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } catch (error: Exception) {
                callback.onWriteFailed(error.message ?: "Failed to print PDF")
            }
        }
    }
}
