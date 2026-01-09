package me.notanoticed.pdfmanager.feature.images

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.collections.emptyList

/* -------------------- VIEW MODEL -------------------- */
data class ImageItem(
    val id: String,
    val name: String,
    val widthPx: Int,
    val heightPx: Int
)

class ImagesViewModel : ViewModel() {
    var selectedImages: List<ImageItem> by mutableStateOf(emptyList())
        private set

    val isActive: Boolean get() = selectedImages.isNotEmpty()
    val selectedCount: Int get() = selectedImages.size

    // TEMP: stub gallery picker
    // TODO: replace with real picker
    fun pickFromGalleryStub() {
        selectedImages = listOf(
            ImageItem(
                id = "1",
                name = "replica_exact_page1.png",
                widthPx = 1200,
                heightPx = 1400
            ),
            ImageItem(
                id = "2",
                name = "diagram_page2.png",
                widthPx = 1400,
                heightPx = 1000
            )
        )
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
}
/* ---------------------------------------------------- */