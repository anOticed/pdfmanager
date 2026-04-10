package me.notanoticed.pdfmanager.core.pdf

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

fun createTempPdfFile(
    context: Context,
    directoryName: String,
    filePrefix: String
): File {
    val outputDir = File(context.cacheDir, directoryName)
    if (!outputDir.exists() && !outputDir.mkdirs()) {
        error("Failed to create temporary PDF directory")
    }

    return File.createTempFile(filePrefix, ".pdf", outputDir)
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
    } ?: error("Failed to open output destination")
}

fun createPdfDocumentInTree(
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
    ) ?: error("Failed to create output PDF in the selected folder")
}
