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

data class SplitChunk(
    val pages: List<Int>
) {
    init {
        require(pages.isNotEmpty()) { "SplitChunk must contain at least one page" }
    }

    val pageCount: Int
        get() = pages.size

    val title: String
        get() = when {
            pageCount == 1 -> "Page ${pages.first()}"
            pages.isContiguous() -> "Pages ${pages.first()}-${pages.last()}"
            else -> "Pages ${pages.joinToString(", ")}"
        }

    val summaryLine: String
        get() = when (pageCount) {
            1 -> "1 page"
            else -> "$pageCount pages"
        }
}

data class SplitPlan(
    val method: SplitMethodType,
    val chunks: List<SplitChunk>
) {
    init {
        require(chunks.isNotEmpty()) { "SplitPlan must contain at least one chunk" }
    }

    val outputFileCount: Int
        get() = chunks.size

    val totalPagesCovered: Int
        get() = chunks.sumOf { it.pageCount }
}

sealed interface SplitPlanResult {
    data class Ready(val plan: SplitPlan) : SplitPlanResult
    data class Error(val message: String) : SplitPlanResult
}
/* ------------------------------------------------------ */


/* -------------------- PLAN BUILDER -------------------- */
fun buildSplitPlan(
    totalPages: Int,
    configuration: SplitConfiguration
): SplitPlanResult {
    if (totalPages <= 0) {
        return SplitPlanResult.Error("Selected PDF contains no pages")
    }

    return runCatching {
        when (configuration.method) {
            SplitMethodType.PAGE_RANGES -> {
                val chunks = parseRangeChunks(
                    totalPages = totalPages,
                    rawRanges = configuration.pageRanges
                )
                SplitPlan(
                    method = configuration.method,
                    chunks = chunks
                )
            }

            SplitMethodType.SINGLE_PAGE_PER_FILE -> {
                SplitPlan(
                    method = configuration.method,
                    chunks = (1..totalPages).map { page ->
                        SplitChunk(pages = listOf(page))
                    }
                )
            }

            SplitMethodType.EVERY_N_PAGES -> {
                val pagesPerFile = configuration.pagesPerFile.trim().toIntOrNull()
                    ?: error("Pages per file must be a valid number")

                if (pagesPerFile <= 0) {
                    error("Pages per file must be greater than 0")
                }

                SplitPlan(
                    method = configuration.method,
                    chunks = (1..totalPages)
                        .chunked(pagesPerFile)
                        .map(::SplitChunk)
                )
            }
        }
    }.fold(
        onSuccess = SplitPlanResult::Ready,
        onFailure = { error ->
            SplitPlanResult.Error(
                error.message?.takeIf { it.isNotBlank() }
                    ?: "Failed to build split plan"
            )
        }
    )
}
/* ----------------------------------------------------- */


/* -------------------- PARSING -------------------- */
private fun parseRangeChunks(
    totalPages: Int,
    rawRanges: String
): List<SplitChunk> {
    val trimmed = rawRanges.trim()
    if (trimmed.isEmpty()) {
        error("Enter page ranges first")
    }

    return trimmed.split(",").map { rawChunk ->
        val chunk = rawChunk.trim()
        if (chunk.isEmpty()) {
            error("Page ranges contain an empty value")
        }

        if ("-" in chunk) {
            val parts = chunk.split("-", limit = 2).map(String::trim)
            if (parts.size != 2 || parts.any { it.isEmpty() }) {
                error("Invalid page range: $chunk")
            }

            val start = parts[0].toIntOrNull()
                ?: error("Invalid page number: ${parts[0]}")
            val end = parts[1].toIntOrNull()
                ?: error("Invalid page number: ${parts[1]}")

            if (start <= 0 || end <= 0) {
                error("Page numbers must start from 1")
            }
            if (start > end) {
                error("Invalid page range: $chunk")
            }
            if (end > totalPages) {
                error("Page range exceeds document length")
            }

            SplitChunk(pages = (start..end).toList())
        } else {
            val page = chunk.toIntOrNull()
                ?: error("Invalid page number: $chunk")

            if (page <= 0) {
                error("Page numbers must start from 1")
            }
            if (page > totalPages) {
                error("Page range exceeds document length")
            }

            SplitChunk(pages = listOf(page))
        }
    }
}

private fun List<Int>.isContiguous(): Boolean {
    if (size < 2) return true
    return zipWithNext().all { (previous, next) -> next == previous + 1 }
}
/* ------------------------------------------------ */
