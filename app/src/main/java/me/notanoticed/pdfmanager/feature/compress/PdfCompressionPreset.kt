package me.notanoticed.pdfmanager.feature.compress

enum class PdfCompressionPreset(
    val jpegQuality: Float,
    val maxLongEdge: Int
) {
    LOW(
        jpegQuality = 0.45f,
        maxLongEdge = 1280
    ),
    MEDIUM(
        jpegQuality = 0.60f,
        maxLongEdge = 1600
    ),
    HIGH(
        jpegQuality = 0.75f,
        maxLongEdge = 2048
    )
}
