package me.notanoticed.pdfmanager.feature.pdflist

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardCapitalization

@Composable
fun RenamePdfDialog(
    visible: Boolean,
    currentName: String,
    inputValue: String,
    isProcessing: Boolean,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = {
            if (!isProcessing) onDismiss()
        },
        title = { Text("Rename PDF") },
        text = {
            OutlinedTextField(
                value = inputValue,
                onValueChange = onValueChange,
                singleLine = true,
                enabled = !isProcessing,
                label = { Text("File name") },
                supportingText = { Text("Current name: $currentName") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isProcessing
            ) {
                Text(if (isProcessing) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isProcessing
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeletePdfDialog(
    visible: Boolean,
    fileName: String,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = {
            if (!isProcessing) onDismiss()
        },
        title = { Text("Delete PDF") },
        text = {
            Text("Are you sure you want to permanently delete \"$fileName\"?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isProcessing
            ) {
                Text(if (isProcessing) "Deleting..." else "Delete")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isProcessing
            ) {
                Text("Cancel")
            }
        }
    )
}
