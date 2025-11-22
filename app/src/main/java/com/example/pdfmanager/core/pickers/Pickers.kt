package com.example.pdfmanager.core.pickers

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

interface Pickers {
    fun pickPdf(onPicked: (Uri) -> Unit)
}

val LocalPickers = staticCompositionLocalOf<Pickers> { error("No pickers provided") }

@Composable
fun ProvidePickers(content: @Composable () -> Unit) {
    val context = LocalContext.current

    var onPdfPicked by remember { mutableStateOf<(Uri) -> Unit>({}) }
    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->

        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                )
            } catch (_: Exception) { /* ignore */ }

            onPdfPicked(uri)
        }
    }

    val pickers = remember {
        object : Pickers {
            override fun pickPdf(onPicked: (Uri) -> Unit) {
                onPdfPicked = onPicked
                pdfLauncher.launch(arrayOf("application/pdf"))
            }
        }
    }

    CompositionLocalProvider(LocalPickers provides pickers) { content() }
}