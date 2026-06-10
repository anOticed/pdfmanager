package me.notanoticed.pdfmanager.app

enum class AppDestination(
    val route: String
) {
    PdfList(route = "pdfs"),
    Merge(route = "merge"),
    Split(route = "split"),
    Images(route = "images"),
    Settings(route = "settings");

    companion object {
        fun fromIndex(index: Int): AppDestination {
            return entries.getOrElse(index) { PdfList }
        }
    }
}
