/**
 * Split feature models shared by the ViewModel, UI, and preview flow.
 */

package me.notanoticed.pdfmanager.feature.split

import android.content.Context
import androidx.annotation.StringRes
import me.notanoticed.pdfmanager.R

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

    fun title(context: Context): String {
        return when {
            pageCount == 1 -> context.getString(R.string.split_chunk_page_single, pages.first())
            pages.isContiguous() -> context.getString(
                R.string.split_chunk_page_range,
                pages.first(),
                pages.last()
            )
            else -> context.getString(
                R.string.split_chunk_page_list,
                pages.joinToString(", ")
            )
        }
    }

    fun summaryLine(context: Context): String {
        return context.resources.getQuantityString(
            R.plurals.split_chunk_page_count,
            pageCount,
            pageCount
        )
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
    data class Error(
        @StringRes val messageRes: Int,
        val formatArgs: List<Any> = emptyList()
    ) : SplitPlanResult
}
/* ------------------------------------------------------ */


/* -------------------- PLAN BUILDER -------------------- */
fun buildSplitPlan(
    totalPages: Int,
    configuration: SplitConfiguration
): SplitPlanResult {
    if (totalPages <= 0) {
        return SplitPlanResult.Error(R.string.split_plan_error_no_pages)
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
                    ?: splitPlanError(R.string.split_plan_error_invalid_pages_per_file_number)

                if (pagesPerFile <= 0) {
                    splitPlanError(R.string.split_plan_error_pages_per_file_positive)
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
            (error as? SplitPlanException)?.toResultError()
                ?: SplitPlanResult.Error(R.string.split_plan_error_generic)
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
        splitPlanError(R.string.split_plan_error_enter_ranges_first)
    }

    return trimmed.split(",").map { rawChunk ->
        val chunk = rawChunk.trim()
        if (chunk.isEmpty()) {
            splitPlanError(R.string.split_plan_error_empty_value)
        }

        if ("-" in chunk) {
            val parts = chunk.split("-", limit = 2).map(String::trim)
            if (parts.size != 2 || parts.any { it.isEmpty() }) {
                splitPlanError(R.string.split_plan_error_invalid_range, chunk)
            }

            val start = parts[0].toIntOrNull()
                ?: splitPlanError(R.string.split_plan_error_invalid_page_number, parts[0])
            val end = parts[1].toIntOrNull()
                ?: splitPlanError(R.string.split_plan_error_invalid_page_number, parts[1])

            if (start <= 0 || end <= 0) {
                splitPlanError(R.string.split_plan_error_page_numbers_start_at_one)
            }
            if (start > end) {
                splitPlanError(R.string.split_plan_error_invalid_range, chunk)
            }
            if (end > totalPages) {
                splitPlanError(R.string.split_plan_error_exceeds_document_length)
            }

            SplitChunk(pages = (start..end).toList())
        } else {
            val page = chunk.toIntOrNull()
                ?: splitPlanError(R.string.split_plan_error_invalid_page_number, chunk)

            if (page <= 0) {
                splitPlanError(R.string.split_plan_error_page_numbers_start_at_one)
            }
            if (page > totalPages) {
                splitPlanError(R.string.split_plan_error_exceeds_document_length)
            }

            SplitChunk(pages = listOf(page))
        }
    }
}

private fun List<Int>.isContiguous(): Boolean {
    if (size < 2) return true
    return zipWithNext().all { (previous, next) -> next == previous + 1 }
}

private class SplitPlanException(
    @StringRes val messageRes: Int,
    val formatArgs: List<Any> = emptyList()
) : IllegalStateException()

private fun splitPlanError(
    @StringRes messageRes: Int,
    vararg formatArgs: Any
): Nothing {
    throw SplitPlanException(messageRes = messageRes, formatArgs = formatArgs.toList())
}

private fun SplitPlanException.toResultError(): SplitPlanResult.Error {
    return SplitPlanResult.Error(
        messageRes = messageRes,
        formatArgs = formatArgs
    )
}

fun SplitPlanResult.Error.resolveMessage(context: Context): String {
    return if (formatArgs.isEmpty()) {
        context.getString(messageRes)
    } else {
        context.getString(messageRes, *formatArgs.toTypedArray())
    }
}
/* ------------------------------------------------ */
