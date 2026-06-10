package me.notanoticed.pdfmanager.core.pdf.edit

import android.content.Context
import com.tom_roush.pdfbox.pdmodel.PDDocument
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.pdf.render.buildPreviewPdfFromFile
import me.notanoticed.pdfmanager.core.pdf.util.ensurePdfBoxInitialized
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.system.files.DestinationWriter
import me.notanoticed.pdfmanager.core.system.files.TempFileStore
import java.io.File

private const val PAGE_EDITOR_DIRECTORY_NAME = "page_editor_pdf"
private const val PAGE_EDITOR_FILE_PREFIX = "page_editor_session_"

internal data class PageEditorSession(
    val directory: File,
    val currentFile: File,
    val generatedFiles: List<File>
) {
    fun withCurrent(newCurrentFile: File): PageEditorSession {
        val updatedFiles = (generatedFiles + newCurrentFile).distinctBy { it.absolutePath }
        return copy(
            currentFile = newCurrentFile,
            generatedFiles = updatedFiles
        )
    }
}

internal data class PageEditorOpenResult(
    val session: PageEditorSession,
    val pageCount: Int,
    val previewPdf: PdfFile
)

internal object PageEditorSessionStore {
    fun openSession(
        context: Context,
        pdf: PdfFile
    ): PageEditorOpenResult {
        ensurePdfBoxInitialized(context)

        val sessionPrefix = "page_editor_session_${System.currentTimeMillis()}"
        val outputDir = TempFileStore.requireDirectory(
            context = context,
            directoryName = PAGE_EDITOR_DIRECTORY_NAME,
            failureMessage = context.getString(R.string.output_create_temp_directory_failed)
        )
        TempFileStore.cleanupGeneratedFiles(
            dir = outputDir,
            prefix = PAGE_EDITOR_FILE_PREFIX,
            keepCount = 8
        )

        val initialFile = createSessionFile(
            directory = outputDir,
            sessionPrefix = sessionPrefix
        )
        DestinationWriter.copyUriToFile(
            context = context,
            sourceUri = pdf.uri,
            outputFile = initialFile,
            openSourceErrorMessage = context.getString(R.string.page_editor_open_failed)
        )

        val pageCount = readPdfPageCount(initialFile)
        val session = PageEditorSession(
            directory = outputDir,
            currentFile = initialFile,
            generatedFiles = listOf(initialFile)
        )

        return PageEditorOpenResult(
            session = session,
            pageCount = pageCount,
            previewPdf = buildPreviewPdf(
                context = context,
                sourceName = pdf.name,
                currentFile = session.currentFile,
                pageCount = pageCount
            )
        )
    }

    fun createNextSessionFile(session: PageEditorSession): File {
        return createSessionFile(
            directory = session.directory,
            sessionPrefix = PAGE_EDITOR_FILE_PREFIX + System.currentTimeMillis()
        )
    }

    fun buildPreviewPdf(
        context: Context,
        sourceName: String,
        currentFile: File,
        pageCount: Int
    ): PdfFile {
        return buildPreviewPdfFromFile(
            context = context,
            previewFile = currentFile,
            displayName = sourceName,
            pageCount = pageCount
        )
    }

    fun cleanupUntrackedGeneratedFiles(session: PageEditorSession) {
        val trackedFiles = session.generatedFiles.map { it.absolutePath }.toSet()
        session.directory
            .listFiles()
            ?.filter { file ->
                file.name.startsWith(PAGE_EDITOR_FILE_PREFIX) &&
                    file.absolutePath !in trackedFiles
            }
            ?.forEach { file -> runCatching { file.delete() } }
    }

    fun deleteSession(session: PageEditorSession) {
        session.generatedFiles
            .distinctBy { it.absolutePath }
            .forEach { file -> runCatching { file.delete() } }
    }

    private fun createSessionFile(
        directory: File,
        sessionPrefix: String
    ): File {
        return File(
            directory,
            "${sessionPrefix}_${System.nanoTime()}.pdf"
        )
    }

    private fun readPdfPageCount(file: File): Int {
        PDDocument.load(file).use { document ->
            return document.numberOfPages.coerceAtLeast(0)
        }
    }
}
