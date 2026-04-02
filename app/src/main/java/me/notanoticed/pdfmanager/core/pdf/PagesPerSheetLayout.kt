package me.notanoticed.pdfmanager.core.pdf

import android.graphics.Rect
import com.tom_roush.pdfbox.multipdf.LayerUtility
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.util.Matrix
import java.io.File
import kotlin.math.min
import kotlin.math.roundToInt

const val PREVIEW_A4_WIDTH_PX = 1240
const val PREVIEW_A4_HEIGHT_PX = 1754

data class SheetLayoutSlot(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
)

data class PdfPageSource(
    val document: PDDocument,
    val pageIndex: Int
)

fun pageCountForSheets(
    itemCount: Int,
    pagesPerSheet: PagesPerSheetOption
): Int {
    if (itemCount <= 0) return 0

    val itemsPerPage = pagesPerSheet.pagesPerSheet.coerceAtLeast(1)
    return (itemCount + itemsPerPage - 1) / itemsPerPage
}

fun buildSheetLayoutSlots(
    sheetWidth: Float,
    sheetHeight: Float,
    pagesPerSheet: PagesPerSheetOption
): List<SheetLayoutSlot> {
    val contentWidth = sheetWidth.coerceAtLeast(1f)
    val contentHeight = sheetHeight.coerceAtLeast(1f)

    return when (pagesPerSheet) {
        PagesPerSheetOption.ONE -> {
            listOf(
                SheetLayoutSlot(
                    left = 0f,
                    top = 0f,
                    width = contentWidth,
                    height = contentHeight
                )
            )
        }

        PagesPerSheetOption.TWO -> {
            val slotHeight = (contentHeight / 2f).coerceAtLeast(1f)

            listOf(
                SheetLayoutSlot(
                    left = 0f,
                    top = 0f,
                    width = contentWidth,
                    height = slotHeight
                ),
                SheetLayoutSlot(
                    left = 0f,
                    top = slotHeight,
                    width = contentWidth,
                    height = slotHeight
                )
            )
        }

        PagesPerSheetOption.FOUR -> {
            val slotWidth = (contentWidth / 2f).coerceAtLeast(1f)
            val slotHeight = (contentHeight / 2f).coerceAtLeast(1f)

            listOf(
                SheetLayoutSlot(
                    left = 0f,
                    top = 0f,
                    width = slotWidth,
                    height = slotHeight
                ),
                SheetLayoutSlot(
                    left = slotWidth,
                    top = 0f,
                    width = slotWidth,
                    height = slotHeight
                ),
                SheetLayoutSlot(
                    left = 0f,
                    top = slotHeight,
                    width = slotWidth,
                    height = slotHeight
                ),
                SheetLayoutSlot(
                    left = slotWidth,
                    top = slotHeight,
                    width = slotWidth,
                    height = slotHeight
                )
            )
        }
    }
}

fun buildPdfFromPageGroups(
    outputFile: File,
    pageGroups: List<List<PdfPageSource>>,
    pagesPerSheet: PagesPerSheetOption
): Int {
    require(pageGroups.any { it.isNotEmpty() }) { "No source pages selected" }

    PDDocument().use { outputDocument ->
        val sheetSize = PDRectangle.A4
        val slots = buildSheetLayoutSlots(
            sheetWidth = sheetSize.width,
            sheetHeight = sheetSize.height,
            pagesPerSheet = pagesPerSheet
        )
        val layerUtility = LayerUtility(outputDocument)

        pageGroups.forEach { pageGroup ->
            if (pageGroup.isEmpty()) return@forEach

            pageGroup.chunked(pagesPerSheet.pagesPerSheet).forEach { sheetPages ->
                val outputPage = PDPage(sheetSize)
                outputDocument.addPage(outputPage)

                PDPageContentStream(
                    outputDocument,
                    outputPage,
                    PDPageContentStream.AppendMode.OVERWRITE,
                    false,
                    true
                ).use { contentStream ->
                    sheetPages.forEachIndexed { slotIndex, source ->
                        val sourcePage = source.document.getPage(source.pageIndex)
                        val form = layerUtility.importPageAsForm(source.document, sourcePage)
                        val sourceBounds = form.getBBox()
                            ?: sourcePage.getCropBox()
                            ?: sourcePage.getMediaBox()

                        val sourceWidth = sourceBounds.width.coerceAtLeast(1f)
                        val sourceHeight = sourceBounds.height.coerceAtLeast(1f)
                        val slot = slots[slotIndex]

                        val scale = min(
                            slot.width / sourceWidth,
                            slot.height / sourceHeight
                        )

                        val drawnWidth = sourceWidth * scale
                        val drawnHeight = sourceHeight * scale
                        val slotBottom = sheetSize.height - slot.top - slot.height
                        val translateX = slot.left +
                            (slot.width - drawnWidth) / 2f -
                            sourceBounds.lowerLeftX * scale
                        val translateY = slotBottom +
                            (slot.height - drawnHeight) / 2f -
                            sourceBounds.lowerLeftY * scale

                        contentStream.saveGraphicsState()
                        contentStream.transform(
                            Matrix(
                                scale,
                                0f,
                                0f,
                                scale,
                                translateX,
                                translateY
                            )
                        )
                        contentStream.drawForm(form)
                        contentStream.restoreGraphicsState()
                    }
                }
            }
        }

        outputDocument.save(outputFile)
        return outputDocument.numberOfPages
    }
}

fun fitRectIntoSlot(
    slot: SheetLayoutSlot,
    srcWidth: Int,
    srcHeight: Int
): Rect {
    val innerRect = fitInsideRect(
        srcWidth = srcWidth,
        srcHeight = srcHeight,
        dstWidth = slot.width.roundToInt(),
        dstHeight = slot.height.roundToInt()
    )

    val left = slot.left.roundToInt() + innerRect.left
    val top = slot.top.roundToInt() + innerRect.top

    return Rect(
        left,
        top,
        left + innerRect.width(),
        top + innerRect.height()
    )
}

private fun fitInsideRect(
    srcWidth: Int,
    srcHeight: Int,
    dstWidth: Int,
    dstHeight: Int
): Rect {
    if (srcWidth <= 0 || srcHeight <= 0 || dstWidth <= 0 || dstHeight <= 0) {
        return Rect(0, 0, dstWidth.coerceAtLeast(1), dstHeight.coerceAtLeast(1))
    }

    val scale = min(
        dstWidth.toFloat() / srcWidth.toFloat(),
        dstHeight.toFloat() / srcHeight.toFloat()
    )

    val outWidth = (srcWidth * scale).roundToInt().coerceAtLeast(1)
    val outHeight = (srcHeight * scale).roundToInt().coerceAtLeast(1)
    val left = ((dstWidth - outWidth) / 2).coerceAtLeast(0)
    val top = ((dstHeight - outHeight) / 2).coerceAtLeast(0)

    return Rect(left, top, left + outWidth, top + outHeight)
}
