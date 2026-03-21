/**
 * Split feature models shared by the ViewModel, UI, and preview flow.
 */

package me.notanoticed.pdfmanager.feature.split

/* -------------------- SPLIT MODELS -------------------- */
enum class SplitMethodType {
    PAGE_RANGES,
    SINGLE_PAGE_PER_FILE,
    EVERY_N_PAGES
}

data class SplitConfiguration(
    val method: SplitMethodType = SplitMethodType.PAGE_RANGES,
    val pageRanges: String = "",
    val pagesPerFile: String = ""
)
/* ------------------------------------------------------ */
