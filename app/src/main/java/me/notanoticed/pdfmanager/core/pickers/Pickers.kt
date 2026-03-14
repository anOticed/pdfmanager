/**
 * System picker/capture abstraction.
 *
 * Provides an API for:
 * - picking one/many PDFs (SAF)
 * - picking many images (Photo Picker)
 * - taking a photo (camera app + FileProvider URI)
 */

package me.notanoticed.pdfmanager.core.pickers

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

/* -------------------- PICKERS -------------------- */
interface Pickers {
    fun pickPdf(onPicked: (Uri) -> Unit)
    fun pickPdfs(onPicked: (List<Uri>) -> Unit)
    fun pickImages(onPicked: (List<Uri>) -> Unit)
    fun takePhoto(
        onCaptured: (Uri) -> Unit,
        onError: (Throwable) -> Unit = {}
    )
}

val LocalPickers = staticCompositionLocalOf<Pickers> { error("No pickers provided") }

@Composable
fun ProvidePickers(content: @Composable () -> Unit) {
    val context = LocalContext.current

    var onPdfPicked by remember { mutableStateOf<(Uri) -> Unit>({}) }
    var onPdfsPicked by remember { mutableStateOf<(List<Uri>) -> Unit>({}) }
    var onImagesPicked by remember { mutableStateOf<(List<Uri>) -> Unit>({}) }
    var onPhotoCaptured by remember { mutableStateOf<(Uri) -> Unit>({}) }
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { /* ignore */ }

            onPdfPicked(uri)
        }
    }

    val pdfsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) { /* ignore */ }
            }

            onPdfsPicked(uris)
        }
    }

    val imagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(50)
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) { /* ignore */ }
            }

            onImagesPicked(uris)
        }
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        val uri = pendingPhotoUri
        pendingPhotoUri = null

        if (isSuccess && uri != null) {
            onPhotoCaptured(uri)
        }
    }

    val pickers = remember {
        object : Pickers {
            override fun pickPdf(onPicked: (Uri) -> Unit) {
                onPdfPicked = onPicked
                pdfLauncher.launch(arrayOf("application/pdf"))
            }

            override fun pickPdfs(onPicked: (List<Uri>) -> Unit) {
                onPdfsPicked = onPicked
                pdfsLauncher.launch(arrayOf("application/pdf"))
            }

            override fun pickImages(onPicked: (List<Uri>) -> Unit) {
                onImagesPicked = onPicked
                imagesLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }

            override fun takePhoto(
                onCaptured: (Uri) -> Unit,
                onError: (Throwable) -> Unit
            ) {
                onPhotoCaptured = onCaptured

                val uri = createTempImageUri(context)
                if (uri == null) {
                    onError(IllegalStateException("Failed to create temporary image file"))
                    return
                }

                pendingPhotoUri = uri
                runCatching {
                    takePhotoLauncher.launch(uri)
                }.onFailure { error ->
                    pendingPhotoUri = null
                    onError(error)
                }
            }
        }
    }

    CompositionLocalProvider(LocalPickers provides pickers) { content() }
}

private fun createTempImageUri(context: Context): Uri? {
    return runCatching {
        val parent = File(context.cacheDir, "captured_images").apply { mkdirs() }
        val photoFile = File.createTempFile(
            "photo_${System.currentTimeMillis()}_",
            ".jpg",
            parent
        )

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }.getOrNull()
}
/* ------------------------------------------------- */
