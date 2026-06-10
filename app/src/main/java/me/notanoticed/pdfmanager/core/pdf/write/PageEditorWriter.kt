package me.notanoticed.pdfmanager.core.pdf.write

import com.tom_roush.pdfbox.pdmodel.PDDocument
import java.io.File

object PageEditorWriter {
    fun applyPageDelete(
        sourceFile: File,
        outputFile: File,
        pageIndex: Int
    ) {
        PDDocument.load(sourceFile).use { document ->
            require(document.numberOfPages > 1) { "Cannot delete the last remaining page" }
            document.removePage(pageIndex)
            document.save(outputFile)
        }
    }

    fun applyPageMove(
        sourceFile: File,
        outputFile: File,
        fromIndex: Int,
        toIndex: Int
    ) {
        PDDocument.load(sourceFile).use { document ->
            if (fromIndex == toIndex) {
                document.save(outputFile)
                return
            }

            val pageTree = document.pages
            val movedPage = document.getPage(fromIndex)
            pageTree.remove(movedPage)

            when {
                toIndex <= 0 -> pageTree.insertBefore(movedPage, document.getPage(0))
                toIndex >= document.numberOfPages -> document.addPage(movedPage)
                else -> pageTree.insertBefore(movedPage, document.getPage(toIndex))
            }

            document.save(outputFile)
        }
    }
}
