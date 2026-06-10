package me.notanoticed.pdfmanager.core.pdf.catalog

import android.content.Context
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.pdf.util.ensurePdfBoxInitialized
import me.notanoticed.pdfmanager.core.system.files.DestinationWriter
import me.notanoticed.pdfmanager.core.system.files.TempFileStore

enum class PdfPasswordActionResult {
    SUCCESS,
    INVALID_PASSWORD,
    FAILED
}

object PdfPasswordService {
    fun setPdfPassword(
        context: Context,
        pdf: PdfFile,
        password: String
    ): Boolean {
        ensurePdfBoxInitialized(context)

        val tempFile = TempFileStore.createTempPdfFile(
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

            DestinationWriter.copyFileToUri(
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

        val tempFile = TempFileStore.createTempPdfFile(
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

            DestinationWriter.copyFileToUri(
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
}
