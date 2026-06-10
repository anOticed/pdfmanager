package me.notanoticed.pdfmanager.feature.images

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.pdf.render.loadPreviewPdf
import me.notanoticed.pdfmanager.core.pdf.util.PagesPerSheetOption
import me.notanoticed.pdfmanager.core.pdf.write.ImagesPdfWriter
import me.notanoticed.pdfmanager.core.system.toast.ToastBindable
import me.notanoticed.pdfmanager.core.system.export.PdfOutputRequest

class ImagesViewModel : ViewModel(), ToastBindable {
    var selectedImages: List<ImageItem> by mutableStateOf(emptyList())
        private set

    val isActive: Boolean get() = selectedImages.isNotEmpty()
    val selectedCount: Int get() = selectedImages.size
    var pagesPerSheetOption by mutableStateOf(PagesPerSheetOption.ONE)
        private set

    var isPreparingPreview by mutableStateOf(false)
        private set

    private var toast: ((String) -> Unit)? = null

    override fun bindToast(toast: (String) -> Unit) {
        this.toast = toast
    }

    override fun unbindToast() {
        toast = null
    }

    private fun showToast(message: String) {
        toast?.invoke(message)
    }

    private fun showToast(
        context: Context,
        messageRes: Int,
        vararg args: Any
    ) {
        showToast(context.getString(messageRes, *args))
    }

    fun addFromGallery(context: Context, uris: List<Uri>) {
        if (uris.isEmpty()) return

        viewModelScope.launch {
            val existing = selectedImages.map { it.uri }.toHashSet()
            val uniqueUris = uris.filter { existing.add(it) }

            if (uniqueUris.isEmpty()) {
                showToast(context, R.string.images_already_selected)
                return@launch
            }

            val items = uniqueUris.map { uri ->
                async(Dispatchers.IO) { ImageItemReader.buildImageItem(context, uri) }
            }.awaitAll().filterNotNull()

            if (items.isEmpty()) {
                showToast(context, R.string.images_add_failed)
                return@launch
            }

            selectedImages = selectedImages + items
        }
    }

    fun addCapturedPhoto(
        context: Context,
        uri: Uri
    ) {
        viewModelScope.launch {
            val item = withContext(Dispatchers.IO) {
                ImageItemReader.buildImageItem(context, uri)
            }

            if (item == null) {
                showToast(context, R.string.images_capture_load_failed)
                return@launch
            }

            selectedImages = selectedImages + item
        }
    }

    fun onCameraPermissionDenied(context: Context) {
        showToast(context, R.string.images_camera_permission_required)
    }

    fun onCameraLaunchFailed(context: Context) {
        showToast(context, R.string.images_camera_open_failed)
    }

    fun clear() {
        selectedImages = emptyList()
    }

    fun remove(image: ImageItem) {
        selectedImages -= image
    }

    fun move(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val list = selectedImages.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        selectedImages = list
    }

    fun updatePagesPerSheet(option: PagesPerSheetOption) {
        pagesPerSheetOption = option
    }

    fun openPreview(
        context: Context,
        onReady: (PdfFile) -> Unit
    ) {
        if (selectedImages.isEmpty()) {
            showToast(context, R.string.images_add_first)
            return
        }
        if (isPreparingPreview) return

        val snapshot = selectedImages
        val pagesPerSheetSnapshot = pagesPerSheetOption

        viewModelScope.launch {
            isPreparingPreview = true

            val previewResult = withContext(Dispatchers.IO) {
                runCatching {
                    ImagesPdfWriter.buildPreviewPdf(
                        context = context,
                        images = snapshot,
                        pagesPerSheet = pagesPerSheetSnapshot
                    )
                }
            }

            val preview = previewResult.getOrNull()
            if (preview == null) {
                isPreparingPreview = false
                showToast(context, R.string.images_preview_failed)
                return@launch
            }

            val pdf = withContext(Dispatchers.IO) {
                loadPreviewPdf(context, preview)
            }

            isPreparingPreview = false
            onReady(pdf)
        }
    }

    fun requestCreatePdfExport(
        context: Context,
        onRequest: (PdfOutputRequest) -> Unit
    ) {
        if (selectedImages.isEmpty()) {
            showToast(context, R.string.images_add_first)
            return
        }

        val snapshot = selectedImages
        val pagesPerSheetSnapshot = pagesPerSheetOption
        val suggestedName = ImagesPdfWriter.buildSuggestedFileName(
            context = context,
            images = snapshot
        )

        onRequest(
            PdfOutputRequest.SaveFile(
                dialogTitle = context.getString(R.string.images_save_dialog_title),
                inputLabel = context.getString(R.string.pdflist_file_name_label),
                inputHint = context.getString(R.string.images_save_dialog_hint),
                confirmLabel = context.getString(R.string.output_choose_location),
                suggestedName = suggestedName,
                processingMessage = context.getString(R.string.output_processing_message),
                successMessage = { it.getString(R.string.images_export_success) },
                prepareFile = { requestContext, _ ->
                    ImagesPdfWriter.prepareExportFile(
                        context = requestContext,
                        images = snapshot,
                        pagesPerSheet = pagesPerSheetSnapshot
                    )
                }
            )
        )
    }
}
