/**
 * PDF viewer fragment.
 * Disables annotation/edit affordances (pencil FAB) and keeps toolbox hidden.
 */

package me.notanoticed.pdfmanager.feature.preview

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import androidx.annotation.RequiresExtension
import androidx.pdf.viewer.fragment.PdfStylingOptions
import androidx.pdf.viewer.fragment.PdfViewerFragment

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 13)
class AppPdfViewerFragment : PdfViewerFragment(PdfStylingOptions(0)) {
    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideEditingUi()
    }

    @SuppressLint("RestrictedApi")
    override fun onResume() {
        super.onResume()
        hideEditingUi()
    }

    @SuppressLint("RestrictedApi", "VisibleForTests")
    private fun hideEditingUi() {
        setAnnotationIntentResolvability(false)
        isToolboxVisible = false
        runCatching { toolboxView.visibility = GONE }
    }
}
