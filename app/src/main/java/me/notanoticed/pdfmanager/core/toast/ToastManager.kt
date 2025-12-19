package me.notanoticed.pdfmanager.core.toast

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

typealias ToastMessage = (String) -> Unit

private val LocalToastMessage = staticCompositionLocalOf<ToastMessage> { error("ToastMessage not provided") }

@Composable
fun ProvideToasts(content: @Composable () -> Unit) {
    val context = LocalContext.current

    val toastMessage = remember(context) {
        { message: String ->
            if (message.isNotBlank()) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    CompositionLocalProvider(
        LocalToastMessage provides toastMessage,
        content = content
    )
}

@Composable
fun rememberToast(): ToastMessage = LocalToastMessage.current