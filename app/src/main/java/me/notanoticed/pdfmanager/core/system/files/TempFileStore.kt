package me.notanoticed.pdfmanager.core.system.files

import android.content.Context
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.system.export.PdfWorkflowException
import java.io.File

object TempFileStore {
    fun requireDirectory(
        context: Context,
        directoryName: String,
        failureMessage: String
    ): File {
        val outputDirectory = File(context.cacheDir, directoryName)
        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            throw PdfWorkflowException(failureMessage)
        }

        return outputDirectory
    }

    fun createTempPdfFile(
        context: Context,
        directoryName: String,
        filePrefix: String
    ): File {
        val outputDirectory = requireDirectory(
            context = context,
            directoryName = directoryName,
            failureMessage = context.getString(R.string.output_create_temp_directory_failed)
        )

        return File.createTempFile(filePrefix, ".pdf", outputDirectory)
    }

    fun prepareGeneratedPdfFile(
        context: Context,
        directoryName: String,
        fileName: String,
        cleanupPrefix: String,
        keepCount: Int = 6
    ): File {
        val outputDirectory = requireDirectory(
            context = context,
            directoryName = directoryName,
            failureMessage = context.getString(R.string.output_create_directory_failed)
        )

        cleanupGeneratedFiles(
            dir = outputDirectory,
            prefix = cleanupPrefix,
            keepCount = keepCount
        )

        return File(outputDirectory, fileName)
    }

    fun cleanupGeneratedFiles(
        dir: File,
        prefix: String,
        keepCount: Int,
        suffix: String = ".pdf"
    ) {
        if (!dir.exists() || !dir.isDirectory) return

        val staleFiles = dir.listFiles()
            ?.filter { it.isFile && it.name.startsWith(prefix) && it.name.endsWith(suffix) }
            ?.sortedByDescending { it.lastModified() }
            ?.drop(keepCount)
            ?: return

        staleFiles.forEach { file ->
            runCatching { file.delete() }
        }
    }
}
