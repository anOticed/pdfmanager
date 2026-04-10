package me.notanoticed.pdfmanager.feature.pdflist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.notanoticed.pdfmanager.ui.theme.Colors

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

    FileActionDialog(
        title = "Rename PDF",
        confirmText = if (isProcessing) "Saving..." else "Save",
        confirmContainerColor = Colors.Button.blue,
        isProcessing = isProcessing,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        textContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Current name: $currentName",
                    color = Colors.Text.secondary,
                    fontSize = 13.sp
                )

                OutlinedTextField(
                    value = inputValue,
                    onValueChange = onValueChange,
                    singleLine = true,
                    enabled = !isProcessing,
                    label = {
                        Text(
                            text = "File name",
                            color = Colors.Text.secondary
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
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

    FileActionDialog(
        title = "Delete PDF",
        confirmText = if (isProcessing) "Deleting..." else "Delete",
        confirmContainerColor = Colors.Button.red,
        isProcessing = isProcessing,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        textContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "This action permanently removes the selected PDF.",
                    color = Colors.Text.secondary,
                    fontSize = 13.sp
                )

                Text(
                    text = "\"$fileName\"",
                    color = Colors.Text.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Composable
private fun FileActionDialog(
    title: String,
    confirmText: String,
    confirmContainerColor: Color,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    textContent: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isProcessing) onDismiss()
        },
        containerColor = Colors.Surface.card,
        title = {
            Text(
                text = title,
                color = Colors.Text.primary,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = textContent,
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = confirmContainerColor
                )
            ) {
                Text(
                    text = confirmText,
                    color = Colors.Primary.white
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isProcessing
            ) {
                Text(
                    text = "Cancel",
                    color = Colors.Text.secondary
                )
            }
        }
    )
}
