package me.notanoticed.pdfmanager.core.pdf.write

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.net.Uri
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.feature.images.ImageItem
import me.notanoticed.pdfmanager.core.pdf.util.PagesPerSheetOption
import me.notanoticed.pdfmanager.core.pdf.util.PREVIEW_A4_HEIGHT_PX
import me.notanoticed.pdfmanager.core.pdf.util.PREVIEW_A4_WIDTH_PX
import me.notanoticed.pdfmanager.core.pdf.util.buildSheetLayoutSlots
import me.notanoticed.pdfmanager.core.pdf.util.fitRectIntoSlot
import me.notanoticed.pdfmanager.core.pdf.util.pageCountForSheets
import me.notanoticed.pdfmanager.core.pdf.render.buildGeneratedPreviewPdf
import me.notanoticed.pdfmanager.core.pdf.render.GeneratedPreviewPdf
import me.notanoticed.pdfmanager.core.system.export.PreparedPdfFile
import me.notanoticed.pdfmanager.core.system.files.TempFileStore
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

internal object ImagesPdfWriter {
    fun buildPreviewPdf(
        context: Context,
        images: List<ImageItem>,
        pagesPerSheet: PagesPerSheetOption
    ): GeneratedPreviewPdf {
        require(images.isNotEmpty()) { context.getString(R.string.images_add_first) }

        val fileName = "images_preview_${pagesPerSheet.pagesPerSheet}_pages_${System.currentTimeMillis()}.pdf"
        return buildGeneratedPreviewPdf(
            context = context,
            directoryName = "images_preview_pdf",
            fileName = fileName,
            cleanupPrefix = "images_preview_"
        ) { outputFile ->
            writeImagesPdfToFile(
                context = context,
                images = images,
                pagesPerSheet = pagesPerSheet,
                outputFile = outputFile
            )
            pageCountForSheets(images.size, pagesPerSheet)
        }
    }

    fun prepareExportFile(
        context: Context,
        images: List<ImageItem>,
        pagesPerSheet: PagesPerSheetOption
    ): PreparedPdfFile {
        val tempFile = TempFileStore.createTempPdfFile(
            context = context,
            directoryName = "images_output_pdf",
            filePrefix = "images_export_"
        )

        return try {
            writeImagesPdfToFile(
                context = context,
                images = images,
                pagesPerSheet = pagesPerSheet,
                outputFile = tempFile
            )
            PreparedPdfFile(tempFile) {
                runCatching { tempFile.delete() }
            }
        } catch (error: Throwable) {
            runCatching { tempFile.delete() }
            throw error
        }
    }

    fun buildSuggestedFileName(
        context: Context,
        images: List<ImageItem>
    ): String {
        val firstName = images.firstOrNull()?.name
            ?.substringBeforeLast('.')
            .orEmpty()

        return if (images.size == 1 && firstName.isNotBlank()) {
            "${firstName}.pdf"
        } else {
            "${context.getString(R.string.images_output_fallback_name)}_${System.currentTimeMillis()}.pdf"
        }
    }

    private fun writeImagesPdfToFile(
        context: Context,
        images: List<ImageItem>,
        pagesPerSheet: PagesPerSheetOption,
        outputFile: File
    ) {
        val pdf = PdfDocument()

        try {
            val pageWidth = PREVIEW_A4_WIDTH_PX
            val pageHeight = PREVIEW_A4_HEIGHT_PX
            val sheetSlots = buildSheetLayoutSlots(
                sheetWidth = pageWidth.toFloat(),
                sheetHeight = pageHeight.toFloat(),
                pagesPerSheet = pagesPerSheet
            )

            images.chunked(pagesPerSheet.pagesPerSheet).forEachIndexed { index, sheetImages ->
                val page = pdf.startPage(
                    PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                )

                try {
                    page.canvas.drawColor(Color.WHITE)

                    sheetImages.forEachIndexed { slotIndex, image ->
                        val slot = sheetSlots[slotIndex]
                        val bitmap = decodeBitmapForPdfPage(
                            context = context,
                            uri = image.uri,
                            targetWidth = slot.width.roundToInt(),
                            targetHeight = slot.height.roundToInt()
                        )

                        if (bitmap != null) {
                            val dstRect = fitRectIntoSlot(
                                slot = slot,
                                srcWidth = bitmap.width,
                                srcHeight = bitmap.height
                            )
                            page.canvas.drawBitmap(bitmap, null, dstRect, null)
                            bitmap.recycle()
                        }
                    }
                } finally {
                    pdf.finishPage(page)
                }
            }

            FileOutputStream(outputFile).use { stream ->
                pdf.writeTo(stream)
            }
        } finally {
            runCatching { pdf.close() }
        }
    }

    private fun decodeBitmapForPdfPage(
        context: Context,
        uri: Uri,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap? {
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }

        val sourceWidth = bounds.outWidth
        val sourceHeight = bounds.outHeight
        if (sourceWidth <= 0 || sourceHeight <= 0) return null

        val sourceLongEdge = max(sourceWidth, sourceHeight)
        val targetLongEdge = max(targetWidth, targetHeight).coerceAtLeast(1)
        var sampleSize = 1
        while (sourceLongEdge / sampleSize > targetLongEdge * 2) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
    }
}
