/**
 * Shared pages-per-sheet options for preview/export flows.
 */

package me.notanoticed.pdfmanager.core.pdf

/* -------------------- PAGES PER SHEET -------------------- */
enum class PagesPerSheetOption(
    val pagesPerSheet: Int
) {
    ONE(1),
    TWO(2),
    FOUR(4)
}
/* -------------------------------------------------------- */
