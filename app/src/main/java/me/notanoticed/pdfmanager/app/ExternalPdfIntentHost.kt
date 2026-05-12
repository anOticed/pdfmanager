package me.notanoticed.pdfmanager.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CallMerge
import androidx.compose.material.icons.automirrored.outlined.CallSplit
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.compose.ui.Alignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.pdf.PdfDocumentActions
import me.notanoticed.pdfmanager.core.pdf.PdfRepository
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.toast.rememberToast
import me.notanoticed.pdfmanager.ui.theme.Colors
import java.io.File

@Composable
fun ExternalPdfIntentHost(
    incomingIntent: Intent?,
    onPreview: (PdfFile) -> Unit,
    onSplit: (PdfFile) -> Unit,
    onMerge: (PdfFile) -> Unit
) {
    val context = LocalContext.current
    val toast = rememberToast()
    var importedPdf by remember { mutableStateOf<PdfFile?>(null) }

    LaunchedEffect(incomingIntent) {
        val intent = incomingIntent ?: return@LaunchedEffect
        val sourceUri = extractExternalPdfUri(intent)
        if (sourceUri == null) {
            return@LaunchedEffect
        }

        val pdf = withContext(Dispatchers.IO) {
            runCatching {
                importExternalPdf(
                    context = context,
                    sourceUri = sourceUri
                )
            }.getOrNull()
        }

        if (pdf == null) {
            toast(context.getString(R.string.external_pdf_open_failed))
            return@LaunchedEffect
        }

        importedPdf = pdf
    }

    ExternalPdfActionDialog(
        pdf = importedPdf,
        onDismiss = { importedPdf = null },
        onPreview = { pdf ->
            importedPdf = null
            onPreview(pdf)
        },
        onSplit = { pdf ->
            importedPdf = null
            onSplit(pdf)
        },
        onMerge = { pdf ->
            importedPdf = null
            onMerge(pdf)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExternalPdfActionDialog(
    pdf: PdfFile?,
    onDismiss: () -> Unit,
    onPreview: (PdfFile) -> Unit,
    onSplit: (PdfFile) -> Unit,
    onMerge: (PdfFile) -> Unit
) {
    val safePdf = pdf ?: return
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Colors.Surface.card,
        contentColor = Colors.Text.primary,
        dragHandle = null,
        properties = ModalBottomSheetProperties(
            isAppearanceLightStatusBars = false,
            isAppearanceLightNavigationBars = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SheetHandle()

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.external_pdf_action_title),
                    color = Colors.Text.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )

                Text(
                    text = safePdf.name,
                    color = Colors.Text.primary,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = safePdf.metaLine(context),
                    color = Colors.Text.secondary,
                    fontSize = 13.sp
                )
            }

            HorizontalDivider(color = Colors.Border.default)

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExternalPdfActionRow(
                    title = stringResource(R.string.external_pdf_action_preview),
                    icon = Icons.Outlined.Visibility,
                    accentColor = Colors.Button.blue,
                    iconTint = Colors.Icon.blue,
                    onClick = { onPreview(safePdf) }
                )

                ExternalPdfActionRow(
                    title = stringResource(R.string.external_pdf_action_split),
                    icon = Icons.AutoMirrored.Outlined.CallSplit,
                    accentColor = Colors.Primary.yellow,
                    iconTint = Colors.Icon.split,
                    onClick = { onSplit(safePdf) }
                )

                ExternalPdfActionRow(
                    title = stringResource(R.string.external_pdf_action_add_to_merge),
                    icon = Icons.AutoMirrored.Outlined.CallMerge,
                    accentColor = Colors.Button.green,
                    iconTint = Colors.Icon.green,
                    onClick = { onMerge(safePdf) }
                )
            }
        }
    }
}

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Colors.Primary.blue)
        )
    }
}

@Composable
private fun ExternalPdfActionRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Colors.Background.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accentColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Colors.Text.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Outlined.ArrowForward,
                contentDescription = null,
                tint = Colors.Text.secondary
            )
        }
    }
}

private suspend fun importExternalPdf(
    context: Context,
    sourceUri: Uri
): PdfFile {
    val displayName = resolveExternalPdfName(
        context = context,
        uri = sourceUri
    )

    val cachedPdfUri = copyExternalPdfToCache(
        context = context,
        sourceUri = sourceUri,
        displayName = displayName
    )

    return PdfRepository.loadPdfMetadata(context, cachedPdfUri)
}

private fun extractExternalPdfUri(intent: Intent): Uri? {
    return when (intent.action) {
        Intent.ACTION_VIEW -> intent.data
        Intent.ACTION_SEND -> {
            @Suppress("DEPRECATION")
            val streamUri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri

            streamUri
                ?: intent.clipData?.getItemAt(0)?.uri
        }
        else -> null
    }
}

private fun resolveExternalPdfName(
    context: Context,
    uri: Uri
): String {
    val fallbackName = context.getString(R.string.pdf_default_name_with_extension)

    val rawName = runCatching {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null

            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex < 0 || cursor.isNull(nameIndex)) null else cursor.getString(nameIndex)
        }
    }.getOrNull()
        ?: uri.lastPathSegment
        ?: fallbackName

    return PdfDocumentActions.normalizeDisplayName(
        rawName = rawName,
        fallbackName = fallbackName
    )
}

private fun copyExternalPdfToCache(
    context: Context,
    sourceUri: Uri,
    displayName: String
): Uri {
    val outputDir = File(context.cacheDir, EXTERNAL_PDF_DIRECTORY)
    if (!outputDir.exists() && !outputDir.mkdirs()) {
        error(context.getString(R.string.output_create_directory_failed))
    }

    outputDir.listFiles()?.forEach { cachedFile ->
        runCatching { cachedFile.delete() }
    }

    val outputFile = File(outputDir, displayName)

    context.contentResolver.openInputStream(sourceUri)?.use { input ->
        outputFile.outputStream().use { output ->
            input.copyTo(output)
        }
    } ?: error(context.getString(R.string.external_pdf_open_failed))

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        outputFile
    )
}

private const val EXTERNAL_PDF_DIRECTORY = "external_input_pdf"
