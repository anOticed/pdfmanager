/**
 * Bridge layer between ViewModels and the toast host.
 *
 * ViewModels that implement ToastBindable can be connected to the UI by calling
 * BindViewModelToasts(viewModel), which binds/unbinds the toast callback safely.
 */

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