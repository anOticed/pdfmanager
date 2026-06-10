package me.notanoticed.pdfmanager.app

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.pdf.catalog.PdfCatalogRepository
import me.notanoticed.pdfmanager.core.pdf.model.PdfFile
import me.notanoticed.pdfmanager.core.pdf.util.PdfFileNamePolicy
import me.notanoticed.pdfmanager.core.system.files.AppFileProvider
import me.notanoticed.pdfmanager.core.system.files.DestinationWriter
import me.notanoticed.pdfmanager.core.system.files.TempFileStore
import me.notanoticed.pdfmanager.core.system.toast.rememberToast
import me.notanoticed.pdfmanager.feature.merge.MergeViewModel
import me.notanoticed.pdfmanager.feature.preview.PreviewNav
import me.notanoticed.pdfmanager.feature.split.SplitViewModel
import me.notanoticed.pdfmanager.ui.theme.Colors
import java.io.File

@Composable
fun ExternalPdfIntentHandler(
    externalIntent: Intent?,
    onExternalIntentConsumed: () -> Unit,
    mergeViewModel: MergeViewModel,
    splitViewModel: SplitViewModel,
    previewNav: PreviewNav,
    onNavigate: (AppDestination) -> Unit
) {
    val context = LocalContext.current
    val toast = rememberToast()
    var pendingExternalPdf by remember { mutableStateOf<PdfFile?>(null) }

    fun consumePendingPdf(action: (PdfFile) -> Unit) {
        val pdf = pendingExternalPdf ?: return
        pendingExternalPdf = null
        action(pdf)
    }

    LaunchedEffect(externalIntent) {
        val intent = externalIntent ?: return@LaunchedEffect
        val sourceUri = extractExternalPdfUri(intent)
        if (sourceUri == null) {
            onExternalIntentConsumed()
            return@LaunchedEffect
        }

        val importedPdf = withContext(Dispatchers.IO) {
            runCatching {
                importExternalPdf(context, sourceUri)
            }.getOrNull()
        }

        if (importedPdf == null) {
            toast(context.getString(R.string.external_pdf_open_failed))
            onExternalIntentConsumed()
            return@LaunchedEffect
        }

        pendingExternalPdf = importedPdf
        onExternalIntentConsumed()
    }

    ExternalPdfActionSheet(
        pdf = pendingExternalPdf,
        onDismiss = { pendingExternalPdf = null },
        onPreview = {
            consumePendingPdf { pdf ->
                previewNav.openSingle(pdf = pdf)
            }
        },
        onSplit = {
            consumePendingPdf { pdf ->
                splitViewModel.updateSelectedSplitPdf(context, pdf)
                onNavigate(AppDestination.Split)
            }
        },
        onMerge = {
            consumePendingPdf { pdf ->
                mergeViewModel.addMergeFiles(context, listOf(pdf))
                onNavigate(AppDestination.Merge)
            }
        }
    )
}

private suspend fun importExternalPdf(
    context: android.content.Context,
    sourceUri: Uri
): PdfFile {
    val fallbackName = context.getString(R.string.pdf_default_name_with_extension)
    val rawName = runCatching {
        context.contentResolver.query(
            sourceUri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex < 0 || cursor.isNull(nameIndex)) null else cursor.getString(nameIndex)
        }
    }.getOrNull() ?: sourceUri.lastPathSegment ?: fallbackName

    val displayName = PdfFileNamePolicy.normalizeDisplayName(
        rawName = rawName,
        fallbackName = fallbackName
    )

    val outputDirectory = TempFileStore.requireDirectory(
        context = context,
        directoryName = "external_input_pdf",
        failureMessage = context.getString(R.string.external_pdf_open_failed)
    )
    outputDirectory.listFiles()?.forEach { file ->
        runCatching { file.delete() }
    }

    val outputFile = File(outputDirectory, displayName)
    DestinationWriter.copyUriToFile(
        context = context,
        sourceUri = sourceUri,
        outputFile = outputFile,
        openSourceErrorMessage = context.getString(R.string.external_pdf_open_failed)
    )

    return PdfCatalogRepository.loadPdfMetadata(
        context = context,
        uri = AppFileProvider.getUriForFile(context, outputFile)
    )
}

private fun extractExternalPdfUri(intent: Intent): Uri? {
    return when (intent.action) {
        Intent.ACTION_VIEW -> intent.data
        Intent.ACTION_SEND -> {
            @Suppress("DEPRECATION")
            val streamUri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
            streamUri ?: intent.clipData?.getItemAt(0)?.uri
        }

        else -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExternalPdfActionSheet(
    pdf: PdfFile?,
    onDismiss: () -> Unit,
    onPreview: () -> Unit,
    onSplit: () -> Unit,
    onMerge: () -> Unit
) {
    val currentPdf = pdf ?: return
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

            Column(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.external_pdf_action_title),
                    color = Colors.Text.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
                Text(
                    text = currentPdf.name,
                    color = Colors.Text.primary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = currentPdf.metaLine(context),
                    color = Colors.Text.secondary,
                    fontSize = 13.sp
                )
            }

            HorizontalDivider(color = Colors.Border.default)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExternalPdfActionRow(
                    title = stringResource(R.string.external_pdf_action_preview),
                    icon = Icons.Outlined.Visibility,
                    accentColor = Colors.Button.blue,
                    iconTint = Colors.Icon.blue,
                    onClick = onPreview
                )
                ExternalPdfActionRow(
                    title = stringResource(R.string.external_pdf_action_split),
                    icon = Icons.AutoMirrored.Outlined.CallSplit,
                    accentColor = Colors.Primary.yellow,
                    iconTint = Colors.Icon.split,
                    onClick = onSplit
                )
                ExternalPdfActionRow(
                    title = stringResource(R.string.external_pdf_action_add_to_merge),
                    icon = Icons.AutoMirrored.Outlined.CallMerge,
                    accentColor = Colors.Button.green,
                    iconTint = Colors.Icon.green,
                    onClick = onMerge
                )
            }
        }
    }
}

@Composable
private fun ExternalPdfActionRow(
    title: String,
    icon: ImageVector,
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
