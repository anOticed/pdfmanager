package me.notanoticed.pdfmanager.core.system.export

import android.content.Context
import android.net.Uri
import me.notanoticed.pdfmanager.core.system.files.DestinationWriter

enum class PdfOutputTarget {
    FILE,
    FOLDER
}

sealed class PdfOutputRequest(
    open val dialogTitle: String,
    open val inputLabel: String,
    open val inputHint: String,
    open val confirmLabel: String,
    open val suggestedName: String,
    open val processingMessage: String,
    open val target: PdfOutputTarget,
    open val cleanupDestinationOnFailure: Boolean,
    open val onCompleted: () -> Unit = {}
) {
    abstract suspend fun export(
        context: Context,
        destinationUri: Uri,
        inputName: String
    ): String

    data class SaveFile(
        override val dialogTitle: String,
        override val inputLabel: String,
        override val inputHint: String,
        override val confirmLabel: String,
        override val suggestedName: String,
        override val processingMessage: String,
        override val onCompleted: () -> Unit = {},
        val successMessage: (Context) -> String,
        val prepareFile: suspend (Context, String) -> PreparedPdfFile
    ) : PdfOutputRequest(
        dialogTitle = dialogTitle,
        inputLabel = inputLabel,
        inputHint = inputHint,
        confirmLabel = confirmLabel,
        suggestedName = suggestedName,
        processingMessage = processingMessage,
        target = PdfOutputTarget.FILE,
        cleanupDestinationOnFailure = true,
        onCompleted = onCompleted
    ) {
        override suspend fun export(
            context: Context,
            destinationUri: Uri,
            inputName: String
        ): String {
            return prepareFile(context, inputName).useFile { sourceFile ->
                DestinationWriter.copyFileToUri(
                    context = context,
                    sourceFile = sourceFile,
                    destinationUri = destinationUri
                )
                successMessage(context)
            }
        }
    }

    data class SaveFolder(
        override val dialogTitle: String,
        override val inputLabel: String,
        override val inputHint: String,
        override val confirmLabel: String,
        override val suggestedName: String,
        override val processingMessage: String,
        override val onCompleted: () -> Unit = {},
        val onSave: suspend (Context, Uri, String) -> String
    ) : PdfOutputRequest(
        dialogTitle = dialogTitle,
        inputLabel = inputLabel,
        inputHint = inputHint,
        confirmLabel = confirmLabel,
        suggestedName = suggestedName,
        processingMessage = processingMessage,
        target = PdfOutputTarget.FOLDER,
        cleanupDestinationOnFailure = false,
        onCompleted = onCompleted
    ) {
        override suspend fun export(
            context: Context,
            destinationUri: Uri,
            inputName: String
        ): String = onSave(context, destinationUri, inputName)
    }
}
