package me.notanoticed.pdfmanager.core.pdf.render

import android.content.Context
import me.notanoticed.pdfmanager.core.pdf.catalog.PdfCatalogRepository
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.system.files.AppFileProvider
import me.notanoticed.pdfmanager.core.system.files.TempFileStore
import java.io.File

internal fun buildGeneratedPreviewPdf(
    context: Context,
    directoryName: String,
    fileName: String,
    cleanupPrefix: String,
    writePreviewToFile: (File) -> Int
): GeneratedPreviewPdf {
    val outputFile = TempFileStore.prepareGeneratedPdfFile(
        context = context,
        directoryName = directoryName,
        fileName = fileName,
        cleanupPrefix = cleanupPrefix
    )

    try {
        val outputPages = writePreviewToFile(outputFile)
        return GeneratedPreviewPdf(
            uri = AppFileProvider.getUriForFile(context, outputFile),
            name = fileName,
            sizeBytes = outputFile.length().coerceAtLeast(0L),
            pagesCount = outputPages.coerceAtLeast(0),
            storagePath = outputFile.absolutePath
        )
    } catch (error: Throwable) {
        runCatching { outputFile.delete() }
        throw error
    }
}

internal suspend fun loadPreviewPdf(
    context: Context,
    preview: GeneratedPreviewPdf
): PdfFile {
    return runCatching {
        PdfCatalogRepository.loadPdfMetadata(context, preview.uri)
    }.getOrElse {
        preview.toPdfFile()
    }
}

internal fun buildPreviewPdfFromFile(
    context: Context,
    previewFile: File,
    displayName: String,
    pageCount: Int
): PdfFile {
    val now = System.currentTimeMillis() / 1000L
    return PdfFile(
        uri = AppFileProvider.getUriForFile(context, previewFile),
        name = displayName,
        sizeBytes = previewFile.length().coerceAtLeast(0L),
        pagesCount = pageCount.coerceAtLeast(0),
        storagePath = previewFile.absolutePath,
        lastModifiedEpochSeconds = now,
        createdEpochSeconds = now,
        thumbnailBitmap = null,
        isLocked = false
    )
}

private fun GeneratedPreviewPdf.toPdfFile(): PdfFile {
    val now = System.currentTimeMillis() / 1000L
    return PdfFile(
        uri = uri,
        name = name,
        sizeBytes = sizeBytes,
        pagesCount = pagesCount,
        storagePath = storagePath,
        lastModifiedEpochSeconds = now,
        createdEpochSeconds = now,
        thumbnailBitmap = null,
        isLocked = false
    )
}
