package me.notanoticed.pdfmanager.core.toast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

interface ToastBindable {
    fun bindToast(toast: (String) -> Unit)
    fun unbindToast()
}

@Composable
fun BindViewModelToasts(viewModel: ToastBindable) {
    val toast = rememberToast()

    DisposableEffect(viewModel, toast) {
        viewModel.bindToast(toast)
        onDispose { viewModel.unbindToast() }
    }
}