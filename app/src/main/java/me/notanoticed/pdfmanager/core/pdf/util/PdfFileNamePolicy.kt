package me.notanoticed.pdfmanager.core.pdf.util

object PdfFileNamePolicy {
    private const val PDF_EXTENSION = ".pdf"
    private val invalidFileNameChars = Regex("""[\\/:*?"<>|]""")

    fun normalizeBaseName(rawName: String, fallbackName: String): String {
        val sanitized = sanitizeFileName(rawName)

        val fallbackBaseName = fallbackName
            .let(::sanitizeFileName)
            .removePdfSuffix()
            .ifBlank { PDF_EXTENSION.removePrefix(".") }

        return sanitized
            .removePdfSuffix()
            .ifBlank { fallbackBaseName }
    }

    fun normalizeDisplayName(rawName: String, fallbackName: String): String {
        return normalizeBaseName(
            rawName = rawName,
            fallbackName = fallbackName
        ).ensurePdfSuffix()
    }

    private fun sanitizeFileName(rawName: String): String {
        return rawName
            .trim()
            .replace(invalidFileNameChars, "_")
            .trim()
    }

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
}
