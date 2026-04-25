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
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.pdf.model.PdfDocumentMetadata
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

enum class PdfPasswordActionResult {
    SUCCESS,
    INVALID_PASSWORD,
    FAILED
}

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

    fun readPdfMetadata(
        context: Context,
        pdfUri: Uri
    ): PdfDocumentMetadata {
        ensurePdfBoxInitialized(context)

        val inputStream = context.contentResolver.openInputStream(pdfUri)
            ?: error("Failed to open PDF for metadata reading")

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

        val tempFile = createTempPdfFile(
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

            copyFileToUri(
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

    fun setPdfPassword(
        context: Context,
        pdf: PdfFile,
        password: String
    ): Boolean {
        ensurePdfBoxInitialized(context)

        val tempFile = createTempPdfFile(
            context = context,
            directoryName = "pdf_password_edit",
            filePrefix = "password_set_"
        )

        return try {
            val inputStream = context.contentResolver.openInputStream(pdf.uri)
                ?: return false

            inputStream.use { stream ->
                PDDocument.load(stream).use { document ->
                    val protectionPolicy = StandardProtectionPolicy(
                        password,
                        password,
                        AccessPermission()
                    ).apply {
                        encryptionKeyLength = 128
                    }

                    document.protect(protectionPolicy)
                    document.save(tempFile)
                }
            }

            copyFileToUri(
                context = context,
                sourceFile = tempFile,
                destinationUri = pdf.uri
            )
            true
        } catch (_: Exception) {
            false
        } finally {
            runCatching { tempFile.delete() }
        }
    }

    fun removePdfPassword(
        context: Context,
        pdf: PdfFile,
        currentPassword: String
    ): PdfPasswordActionResult {
        ensurePdfBoxInitialized(context)

        val tempFile = createTempPdfFile(
            context = context,
            directoryName = "pdf_password_edit",
            filePrefix = "password_remove_"
        )

        return try {
            val inputStream = context.contentResolver.openInputStream(pdf.uri)
                ?: return PdfPasswordActionResult.FAILED

            inputStream.use { stream ->
                val document = try {
                    PDDocument.load(stream, currentPassword)
                } catch (error: Exception) {
                    return if (error.javaClass.simpleName == "InvalidPasswordException") {
                        PdfPasswordActionResult.INVALID_PASSWORD
                    } else {
                        PdfPasswordActionResult.FAILED
                    }
                }

                document.use {
                    document.setAllSecurityToBeRemoved(true)
                    document.save(tempFile)
                }
            }

            copyFileToUri(
                context = context,
                sourceFile = tempFile,
                destinationUri = pdf.uri
            )
            PdfPasswordActionResult.SUCCESS
        } catch (_: Exception) {
            PdfPasswordActionResult.FAILED
        } finally {
            runCatching { tempFile.delete() }
        }
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
