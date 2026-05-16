package me.notanoticed.pdfmanager.feature.compress

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDResources
import com.tom_roush.pdfbox.pdmodel.graphics.form.PDFormXObject
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import me.notanoticed.pdfmanager.core.pdf.ensurePdfBoxInitialized
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt

fun compressPdf(
    context: Context,
    sourceUri: Uri,
    outputFile: File,
    preset: PdfCompressionPreset
) {
    ensurePdfBoxInitialized(context)

    val inputStream = context.contentResolver.openInputStream(sourceUri)
        ?: error("Failed to open source PDF")

    inputStream.use { stream ->
        PDDocument.load(stream).use { document ->
            for (page in document.pages) {
                compressResources(
                    document = document,
                    resources = page.resources,
                    preset = preset
                )
            }

            document.save(outputFile)
        }
    }
}

private fun compressResources(
    document: PDDocument,
    resources: PDResources?,
    preset: PdfCompressionPreset
) {
    if (resources == null) return

    for (name in resources.xObjectNames) {
        when (val xObject = resources.getXObject(name)) {
            is PDImageXObject -> {
                val replacement = compressImage(
                    document = document,
                    image = xObject,
                    preset = preset
                )

                if (replacement != null) {
                    resources.put(name, replacement)
                }
            }

            is PDFormXObject -> {
                compressResources(
                    document = document,
                    resources = xObject.resources,
                    preset = preset
                )
            }
        }
    }
}

private fun compressImage(
    document: PDDocument,
    image: PDImageXObject,
    preset: PdfCompressionPreset
): PDImageXObject? {
    val filter = image.cosObject.getDictionaryObject(COSName.FILTER)
        ?.toString()
        .orEmpty()

    if (filter.contains("DCTDecode") ||
        filter.contains("JPXDecode") ||
        filter.contains("JBIG2Decode") ||
        filter.contains("CCITTFaxDecode")
    ) {
        return null
    }

    val originalBytes = image.cosObject.createRawInputStream().use { it.readBytes() }
    val bitmap = image.image ?: return null

    val preparedBitmap = prepareBitmap(
        source = bitmap,
        maxLongEdge = preset.maxLongEdge
    )

    val jpegBytes = preparedBitmap.toJpegBytes(preset.jpegQuality)
    if (jpegBytes.size >= originalBytes.size) {
        return null
    }

    return JPEGFactory.createFromByteArray(document, jpegBytes)
}

private fun prepareBitmap(
    source: Bitmap,
    maxLongEdge: Int
): Bitmap {
    val scaled = downscaleIfNeeded(
        source = source,
        maxLongEdge = maxLongEdge
    )

    return if (scaled.hasAlpha()) {
        flattenToWhite(scaled)
    } else {
        scaled
    }
}

private fun downscaleIfNeeded(
    source: Bitmap,
    maxLongEdge: Int
): Bitmap {
    val longEdge = maxOf(source.width, source.height)
    if (longEdge <= maxLongEdge) return source

    val scale = maxLongEdge.toFloat() / longEdge.toFloat()
    val newWidth = (source.width * scale).roundToInt().coerceAtLeast(1)
    val newHeight = (source.height * scale).roundToInt().coerceAtLeast(1)

    return Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
}

private fun flattenToWhite(source: Bitmap): Bitmap {
    val result = Bitmap.createBitmap(
        source.width,
        source.height,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(result)
    canvas.drawColor(Color.WHITE)
    canvas.drawBitmap(source, 0f, 0f, null)
    return result
}

private fun Bitmap.toJpegBytes(quality: Float): ByteArray {
    val output = ByteArrayOutputStream()

    compress(
        Bitmap.CompressFormat.JPEG,
        (quality.coerceIn(0f, 1f) * 100f).roundToInt(),
        output
    )

    return output.toByteArray()
}
