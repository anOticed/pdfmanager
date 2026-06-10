package me.notanoticed.pdfmanager.core.system.export

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.pdf.util.PdfFileNamePolicy
import me.notanoticed.pdfmanager.core.system.files.DestinationWriter
import me.notanoticed.pdfmanager.core.system.pickers.FilePickers
import me.notanoticed.pdfmanager.core.system.pickers.LocalFilePickers
import me.notanoticed.pdfmanager.core.system.toast.rememberToast
import me.notanoticed.pdfmanager.ui.theme.Colors

class PdfOutputFlow internal constructor(
    private val openRequest: (PdfOutputRequest) -> Unit
) {
    fun start(request: PdfOutputRequest) {
        openRequest(request)
    }
}

val LocalPdfOutputFlow = staticCompositionLocalOf<PdfOutputFlow> {
    error("PdfOutputFlow not provided")
}

@Composable
fun ProvidePdfExportHost(
    onExportFinished: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val filePickers = LocalFilePickers.current
    val toast = rememberToast()
    val scope = rememberCoroutineScope()

    var activeRequest by remember { mutableStateOf<PdfOutputRequest?>(null) }
    var inputName by rememberSaveable { mutableStateOf("") }
    var processingMessage by remember { mutableStateOf<String?>(null) }

    fun closeRequest() {
        if (processingMessage != null) return
        activeRequest = null
        inputName = ""
    }

    fun startExport(destinationUri: Uri) {
        val request = activeRequest ?: return
        val normalizedInput = request.normalizeInputName(inputName)

        inputName = normalizedInput
        processingMessage = request.processingMessage

        launchExport(
            scope = scope,
            request = request,
            context = context,
            destinationUri = destinationUri,
            inputName = normalizedInput,
            onComplete = { message ->
                processingMessage = null
                runCatching { request.onCompleted() }
                activeRequest = null
                inputName = ""
                toast(message)
                onExportFinished()
            },
            onError = { error ->
                processingMessage = null
                if (request.cleanupDestinationOnFailure) {
                    DestinationWriter.deleteUri(context, destinationUri)
                }
                activeRequest = null
                inputName = ""
                toast(error.toPdfExportMessage(context))
            }
        )
    }

    val outputFlow = remember {
        PdfOutputFlow { request ->
            activeRequest = request
            inputName = request.suggestedName
        }
    }

    CompositionLocalProvider(LocalPdfOutputFlow provides outputFlow) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()

            if (processingMessage == null) {
                activeRequest?.let { request ->
                    PdfOutputDialog(
                        request = request,
                        inputName = inputName,
                        onInputChange = { inputName = it },
                        onDismiss = ::closeRequest,
                        onConfirm = {
                            val normalizedInput = request.normalizeInputName(inputName)
                            inputName = normalizedInput
                            request.pickDestination(
                                filePickers = filePickers,
                                normalizedInput = normalizedInput,
                                onPicked = ::startExport
                            )
                        }
                    )
                }
            }

            processingMessage?.let { message ->
                PdfProcessingOverlay(message = message)
            }
        }
    }
}

private fun Throwable.toPdfExportMessage(context: Context): String {
    return message?.takeIf { it.isNotBlank() }
        ?: context.getString(R.string.output_save_failed)
}

private fun PdfOutputRequest.normalizeInputName(rawName: String): String {
    return when (target) {
        PdfOutputTarget.FILE -> PdfFileNamePolicy.normalizeDisplayName(
            rawName = rawName,
            fallbackName = suggestedName
        )

        PdfOutputTarget.FOLDER -> PdfFileNamePolicy.normalizeBaseName(
            rawName = rawName,
            fallbackName = suggestedName
        )
    }
}

private fun PdfOutputRequest.pickDestination(
    filePickers: FilePickers,
    normalizedInput: String,
    onPicked: (Uri) -> Unit
) {
    when (target) {
        PdfOutputTarget.FILE -> filePickers.createPdfDocument(
            suggestedName = normalizedInput,
            onPicked = onPicked
        )

        PdfOutputTarget.FOLDER -> filePickers.pickFolder(onPicked = onPicked)
    }
}

private fun launchExport(
    scope: CoroutineScope,
    request: PdfOutputRequest,
    context: Context,
    destinationUri: Uri,
    inputName: String,
    onComplete: (String) -> Unit,
    onError: (Throwable) -> Unit
) {
    scope.launch {
        runCatching {
            withContext(Dispatchers.IO) {
                request.export(
                    context = context,
                    destinationUri = destinationUri,
                    inputName = inputName
                )
            }
        }
            .onSuccess(onComplete)
            .onFailure(onError)
    }
}

@Composable
private fun PdfOutputDialog(
    request: PdfOutputRequest,
    inputName: String,
    onInputChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Colors.Surface.card,
        title = {
            Text(
                text = request.dialogTitle,
                color = Colors.Text.primary,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = request.inputHint,
                    color = Colors.Text.secondary,
                    fontSize = 13.sp
                )

                OutlinedTextField(
                    value = inputName,
                    onValueChange = onInputChange,
                    singleLine = true,
                    label = {
                        Text(
                            text = request.inputLabel,
                            color = Colors.Text.secondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Colors.Text.primary,
                        unfocusedTextColor = Colors.Text.primary,
                        disabledTextColor = Colors.Text.secondary,
                        cursorColor = Colors.Primary.blue,
                        focusedBorderColor = Colors.Border.blue,
                        unfocusedBorderColor = Colors.Border.gray,
                        focusedLabelColor = Colors.Text.secondary,
                        unfocusedLabelColor = Colors.Text.secondary,
                        focusedContainerColor = Colors.Surface.card,
                        unfocusedContainerColor = Colors.Surface.card
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Colors.Button.blue
                )
            ) {
                Text(
                    text = request.confirmLabel,
                    color = Colors.Primary.white
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = Colors.Text.secondary
                )
            }
        }
    )
}

@Composable
private fun PdfProcessingOverlay(message: String) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Colors.Primary.nearBlack.copy(alpha = 0.65f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Colors.Surface.card,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Colors.Primary.blue)

                    Text(
                        text = message,
                        color = Colors.Text.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Colors.Primary.blue,
                        trackColor = Colors.Surface.thumbnail
                    )
                }
            }
        }
    }
}
