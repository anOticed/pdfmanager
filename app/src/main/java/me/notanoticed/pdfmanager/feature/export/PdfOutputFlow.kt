package me.notanoticed.pdfmanager.feature.export

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.notanoticed.pdfmanager.core.pdf.PdfDocumentActions
import me.notanoticed.pdfmanager.core.pickers.LocalPickers
import me.notanoticed.pdfmanager.core.pickers.Pickers
import me.notanoticed.pdfmanager.core.toast.rememberToast
import me.notanoticed.pdfmanager.ui.theme.Colors

enum class PdfOutputTarget {
    FILE,
    FOLDER
}

sealed class PdfOutputRequest(
    open val dialogTitle: String,
    open val inputLabel: String,
    open val inputHint: String,
    open val confirmLabel: String,
    open val suggestedName: String,
    open val processingMessage: String,
    open val target: PdfOutputTarget
) {
    abstract suspend fun export(
        context: Context,
        destinationUri: Uri,
        inputName: String
    ): String

    data class SaveFile(
        override val dialogTitle: String,
        override val inputLabel: String,
        override val inputHint: String,
        override val confirmLabel: String,
        override val suggestedName: String,
        override val processingMessage: String,
        val onSave: suspend (Context, Uri, String) -> String
    ) : PdfOutputRequest(
        dialogTitle = dialogTitle,
        inputLabel = inputLabel,
        inputHint = inputHint,
        confirmLabel = confirmLabel,
        suggestedName = suggestedName,
        processingMessage = processingMessage,
        target = PdfOutputTarget.FILE
    ) {
        override suspend fun export(
            context: Context,
            destinationUri: Uri,
            inputName: String
        ): String = onSave(context, destinationUri, inputName)
    }

    data class SaveFolder(
        override val dialogTitle: String,
        override val inputLabel: String,
        override val inputHint: String,
        override val confirmLabel: String,
        override val suggestedName: String,
        override val processingMessage: String,
        val onSave: suspend (Context, Uri, String) -> String
    ) : PdfOutputRequest(
        dialogTitle = dialogTitle,
        inputLabel = inputLabel,
        inputHint = inputHint,
        confirmLabel = confirmLabel,
        suggestedName = suggestedName,
        processingMessage = processingMessage,
        target = PdfOutputTarget.FOLDER
    ) {
        override suspend fun export(
            context: Context,
            destinationUri: Uri,
            inputName: String
        ): String = onSave(context, destinationUri, inputName)
    }
}

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
fun ProvidePdfOutputFlow(
    onExportFinished: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val pickers = LocalPickers.current
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
                activeRequest = null
                inputName = ""
                toast(message)
                onExportFinished()
            },
            onError = { error ->
                processingMessage = null
                toast(
                    error.message?.takeIf { it.isNotBlank() }
                        ?: "Failed to save PDF"
                )
            }
        )
    }

    val flow = remember {
        PdfOutputFlow { request ->
            activeRequest = request
            inputName = request.suggestedName
        }
    }

    CompositionLocalProvider(LocalPdfOutputFlow provides flow) {
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
                                pickers = pickers,
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

private fun PdfOutputRequest.normalizeInputName(rawName: String): String {
    return when (target) {
        PdfOutputTarget.FILE -> PdfDocumentActions.normalizeDisplayName(
            rawName = rawName,
            fallbackName = suggestedName
        )

        PdfOutputTarget.FOLDER -> PdfDocumentActions.normalizeBaseName(
            rawName = rawName,
            fallbackName = suggestedName
        )
    }
}

private fun PdfOutputRequest.pickDestination(
    pickers: Pickers,
    normalizedInput: String,
    onPicked: (Uri) -> Unit
) {
    when (target) {
        PdfOutputTarget.FILE -> pickers.createPdfDocument(
            suggestedName = normalizedInput,
            onPicked = onPicked
        )

        PdfOutputTarget.FOLDER -> pickers.pickFolder(onPicked = onPicked)
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
        }.onSuccess(onComplete)
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
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                    modifier = Modifier.fillMaxWidth()
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
                    text = "Cancel",
                    color = Colors.Text.secondary
                )
            }
        }
    )
}

@Composable
private fun PdfProcessingOverlay(
    message: String
) {
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
                    CircularProgressIndicator(
                        color = Colors.Primary.blue
                    )

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
