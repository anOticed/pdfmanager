package me.notanoticed.pdfmanager.feature.compress

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.ui.components.PdfThumbnail
import me.notanoticed.pdfmanager.core.system.export.LocalPdfOutputFlow
import me.notanoticed.pdfmanager.ui.theme.Colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressSheet(
    viewModel: CompressViewModel
) {
    val pdf = viewModel.selectedPdf ?: return
    val context = LocalContext.current
    val pdfOutputFlow = LocalPdfOutputFlow.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = viewModel::close,
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
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

            Text(
                text = stringResource(R.string.compress_sheet_title),
                color = Colors.Text.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Colors.Surface.charcoalSlate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PdfThumbnail(
                        pdf = pdf,
                        modifier = Modifier
                            .width(48.dp)
                            .height(64.dp),
                        cornerRadius = 10.dp,
                        placeholderBackground = Colors.Surface.thumbnail,
                        placeholderIconTint = Colors.Icon.gray,
                        placeholderIconSize = 26.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = pdf.name,
                            color = Colors.Text.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = pdf.metaLine(context),
                            color = Colors.Text.secondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Colors.Surface.charcoalSlate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.compress_preset_title),
                        color = Colors.Text.blue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    CompressionPresetCard(
                        preset = CompressionPreset.LOW,
                        title = stringResource(R.string.compress_preset_low_title),
                        subtitle = stringResource(R.string.compress_preset_low_subtitle),
                        selectedPreset = viewModel.selectedPreset,
                        onSelect = viewModel::selectPreset
                    )

                    CompressionPresetCard(
                        preset = CompressionPreset.MEDIUM,
                        title = stringResource(R.string.compress_preset_medium_title),
                        subtitle = stringResource(R.string.compress_preset_medium_subtitle),
                        selectedPreset = viewModel.selectedPreset,
                        onSelect = viewModel::selectPreset
                    )

                    CompressionPresetCard(
                        preset = CompressionPreset.HIGH,
                        title = stringResource(R.string.compress_preset_high_title),
                        subtitle = stringResource(R.string.compress_preset_high_subtitle),
                        selectedPreset = viewModel.selectedPreset,
                        onSelect = viewModel::selectPreset
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Colors.Surface.charcoalSlate,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = viewModel::close,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Colors.Button.darkSlate
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            color = Colors.Primary.white,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.requestExport(
                                context = context,
                                onRequest = pdfOutputFlow::start
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Colors.Button.compress
                        ),
                        modifier = Modifier.weight(1.75f),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = stringResource(R.string.compress_export_action),
                            tint = Colors.Icon.white,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = stringResource(R.string.compress_export_action),
                            color = Colors.Primary.white,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompressionPresetCard(
    preset: CompressionPreset,
    title: String,
    subtitle: String,
    selectedPreset: CompressionPreset,
    onSelect: (CompressionPreset) -> Unit
) {
    val isSelected = preset == selectedPreset
    val surfaceColor = if (isSelected) Colors.Surface.selectedCard else Colors.Surface.card
    val borderColor = if (isSelected) Colors.Border.lightBlue else Colors.Border.darkGray
    val iconBackground = if (isSelected) Colors.Button.compress else Colors.Icon.darkGray
    val iconTint = if (isSelected) Colors.Icon.white else Colors.Icon.default

    Card(
        onClick = { onSelect(preset) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        border = BorderStroke(
            width = 2.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ListItem(
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconBackground)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Compress,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .fillMaxSize(0.65f)
                            .align(Alignment.Center)
                    )
                }
            },
            headlineContent = {
                Text(
                    text = title,
                    color = Colors.Text.primary,
                    fontWeight = FontWeight.SemiBold
                )
            },
            supportingContent = {
                Text(
                    text = subtitle,
                    color = Colors.Text.secondary,
                    fontSize = 12.sp
                )
            },
            trailingContent = {
                RadioButton(
                    selected = isSelected,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Colors.Button.skyBlue
                    )
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
    }
}
