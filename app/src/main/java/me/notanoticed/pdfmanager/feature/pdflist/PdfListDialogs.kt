package me.notanoticed.pdfmanager.feature.pdflist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.notanoticed.pdfmanager.R
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
        title = stringResource(R.string.pdflist_rename_dialog_title),
        confirmText = if (isProcessing) stringResource(R.string.action_saving) else stringResource(R.string.action_save),
        confirmContainerColor = Colors.Button.blue,
        isProcessing = isProcessing,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        textContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.pdflist_current_name_format, currentName),
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
                            text = stringResource(R.string.pdflist_file_name_label),
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
    fileName: String?,
    fileCount: Int,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!visible) return

    FileActionDialog(
        title = stringResource(R.string.pdflist_delete_dialog_title),
        confirmText = if (isProcessing) stringResource(R.string.action_deleting) else stringResource(R.string.action_delete),
        confirmContainerColor = Colors.Button.red,
        isProcessing = isProcessing,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        textContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = pluralStringResource(R.plurals.pdflist_delete_dialog_message, fileCount),
                    color = Colors.Text.secondary,
                    fontSize = 13.sp
                )

                Text(
                    text = if (fileCount == 1) {
                        "\"${fileName.orEmpty()}\""
                    } else {
                        pluralStringResource(R.plurals.pdflist_selected_pdf_count, fileCount, fileCount)
                    },
                    color = Colors.Text.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Composable
fun EditPdfMetadataDialog(
    viewModel: PdfListViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!viewModel.metadataDialogVisible) return

    FileActionDialog(
        title = stringResource(R.string.pdflist_metadata_dialog_title),
        confirmText = when {
            viewModel.isMetadataLoading -> stringResource(R.string.action_loading)
            viewModel.isFileActionInProgress -> stringResource(R.string.action_saving)
            else -> stringResource(R.string.action_save)
        },
        confirmContainerColor = Colors.Button.blue,
        isProcessing = viewModel.isFileActionInProgress || viewModel.isMetadataLoading,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        textContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = viewModel.metadataDialogPdf?.name.orEmpty(),
                    color = Colors.Text.secondary,
                    fontSize = 13.sp
                )

                if (viewModel.isMetadataLoading) {
                    Text(
                        text = stringResource(R.string.pdflist_metadata_loading),
                        color = Colors.Text.secondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    MetadataField(
                        label = stringResource(R.string.pdflist_metadata_title),
                        value = viewModel.metadataTitleInput,
                        enabled = !viewModel.isFileActionInProgress,
                        onValueChange = viewModel::updateMetadataTitle
                    )
                    MetadataField(
                        label = stringResource(R.string.pdflist_metadata_author),
                        value = viewModel.metadataAuthorInput,
                        enabled = !viewModel.isFileActionInProgress,
                        onValueChange = viewModel::updateMetadataAuthor
                    )
                    MetadataField(
                        label = stringResource(R.string.pdflist_metadata_subject),
                        value = viewModel.metadataSubjectInput,
                        enabled = !viewModel.isFileActionInProgress,
                        onValueChange = viewModel::updateMetadataSubject
                    )
                    MetadataField(
                        label = stringResource(R.string.pdflist_metadata_keywords),
                        value = viewModel.metadataKeywordsInput,
                        enabled = !viewModel.isFileActionInProgress,
                        onValueChange = viewModel::updateMetadataKeywords
                    )
                }
            }
        }
    )
}

@Composable
fun PdfPasswordDialog(
    viewModel: PdfListViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val mode = viewModel.passwordDialogMode ?: return

    FileActionDialog(
        title = if (mode == PdfListViewModel.PasswordDialogMode.SET) {
            stringResource(R.string.pdflist_password_set_dialog_title)
        } else {
            stringResource(R.string.pdflist_password_remove_dialog_title)
        },
        confirmText = when {
            viewModel.isFileActionInProgress && mode == PdfListViewModel.PasswordDialogMode.SET -> stringResource(R.string.action_saving)
            viewModel.isFileActionInProgress -> stringResource(R.string.action_removing)
            mode == PdfListViewModel.PasswordDialogMode.SET -> stringResource(R.string.action_save)
            else -> stringResource(R.string.action_remove)
        },
        confirmContainerColor = Colors.Button.blue,
        isProcessing = viewModel.isFileActionInProgress,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        textContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = viewModel.passwordDialogPdf?.name.orEmpty(),
                    color = Colors.Text.secondary,
                    fontSize = 13.sp
                )

                if (mode == PdfListViewModel.PasswordDialogMode.SET) {
                    Text(
                        text = stringResource(R.string.pdflist_password_set_message),
                        color = Colors.Text.secondary,
                        fontSize = 13.sp
                    )

                    PasswordField(
                        label = stringResource(R.string.pdflist_password_new_label),
                        value = viewModel.passwordPrimaryInput,
                        enabled = !viewModel.isFileActionInProgress,
                        onValueChange = viewModel::updatePasswordPrimaryInput
                    )
                    PasswordField(
                        label = stringResource(R.string.pdflist_password_confirm_label),
                        value = viewModel.passwordConfirmInput,
                        enabled = !viewModel.isFileActionInProgress,
                        onValueChange = viewModel::updatePasswordConfirmInput
                    )
                } else {
                    Text(
                        text = stringResource(R.string.pdflist_password_remove_message),
                        color = Colors.Text.secondary,
                        fontSize = 13.sp
                    )

                    PasswordField(
                        label = stringResource(R.string.pdflist_password_current_label),
                        value = viewModel.passwordPrimaryInput,
                        enabled = !viewModel.isFileActionInProgress,
                        onValueChange = viewModel::updatePasswordPrimaryInput
                    )
                }
            }
        }
    )
}

@Composable
private fun MetadataField(
    label: String,
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        enabled = enabled,
        label = {
            Text(
                text = label,
                color = Colors.Text.secondary
            )
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        enabled = enabled,
        label = {
            Text(
                text = label,
                color = Colors.Text.secondary
            )
        },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth()
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
                    text = stringResource(R.string.action_cancel),
                    color = Colors.Text.secondary
                )
            }
        }
    )
}
