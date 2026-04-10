/**
 * Split tab "active" state.
 *
 * Displayed once a PDF is selected.
 *
 * - Shows the selected file summary.
 * - Lets the user choose a split method.
 * - Exposes actions (Preview / Split).
 */

package me.notanoticed.pdfmanager.feature.split

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.notanoticed.pdfmanager.core.pdf.PdfThumbnail
import me.notanoticed.pdfmanager.core.toast.BindViewModelToasts
import me.notanoticed.pdfmanager.feature.export.LocalPdfOutputFlow
import me.notanoticed.pdfmanager.feature.preview.LocalPreviewNav
import me.notanoticed.pdfmanager.ui.components.ExpandablePagesPerSheetSection
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- ACTIVE SCREEN -------------------- */
@Composable
fun SplitActiveScreen(
    modifier: Modifier = Modifier,
    viewModel: SplitViewModel
) {
    BindViewModelToasts(viewModel)
    val pdfOutputFlow = LocalPdfOutputFlow.current
    val previewNav = LocalPreviewNav.current
    val selectedSplitPdf = viewModel.selectedSplitPdf ?: return
    val splitPlanResult = viewModel.splitPlanResult

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
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
                        pdf = selectedSplitPdf,
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
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = selectedSplitPdf.name,
                            color = Colors.Text.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = selectedSplitPdf.metaLine(),
                            color = Colors.Text.secondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Split Method",
                color = Colors.Text.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            RadioButtonSingleSelection(viewModel = viewModel)
        }

        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Colors.Surface.charcoalSlate,
                modifier = Modifier.fillMaxWidth()
            ) {
                ExpandablePagesPerSheetSection(
                    selectedOption = viewModel.pagesPerSheetOption,
                    onOptionSelected = viewModel::updatePagesPerSheet,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        item {
            SplitPlanSummaryCard(splitPlanResult = splitPlanResult)
        }

        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Colors.Surface.charcoalSlate,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.openPreview { pdf, plan, pagesPerSheet ->
                                previewNav.openSplit(
                                    pdf = pdf,
                                    plan = plan,
                                    pagesPerSheet = pagesPerSheet
                                )
                            }
                        },
                        enabled = splitPlanResult is SplitPlanResult.Ready,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Colors.Button.darkSlate
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = "Preview",
                            tint = Colors.Icon.white,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Preview",
                            color = Colors.Primary.white,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.requestSplitExport(pdfOutputFlow::start)
                        },
                        enabled = splitPlanResult is SplitPlanResult.Ready,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Colors.Button.red
                        ),
                        modifier = Modifier.weight(1.75f),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = "Split PDF",
                            tint = Colors.Icon.white,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Split PDF",
                            color = Colors.Primary.white,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
        }

    }

}


data class SplitMethod(
    val type: SplitMethodType,
    val title: String,
    val description: String,
    val icon: ImageVector
)


data class MethodView(
    val type: SplitMethodType,
    val title: String,
    val description: String
)


@Composable
fun RadioButtonSingleSelection(
    viewModel: SplitViewModel
) {
    val selectedSplitPdf = viewModel.selectedSplitPdf ?: return
    val selectedMethod = viewModel.selectedSplitMethod

    val radioOptions = listOf(
        SplitMethod(
            type = SplitMethodType.PAGE_RANGES,
            title = "By Page Ranges",
            description = "Split into specific ranges (e.g., 1-5, 10-15)",
            icon = Icons.Outlined.ContentCut
        ),
        SplitMethod(
            type = SplitMethodType.SINGLE_PAGE_PER_FILE,
            title = "One Page Per File",
            description = "Create a separate file for each page",
            icon = Icons.Outlined.FileCopy
        ),
        SplitMethod(
            type = SplitMethodType.EVERY_N_PAGES,
            title = "Every N Pages",
            description = "Split into files with N pages each",
            icon = Icons.Outlined.Tag
        )
    )

    val methodView = listOf(
        MethodView(
            type = SplitMethodType.PAGE_RANGES,
            title = "Page Ranges",
            description = "Enter page ranges separated by commas (e.g., 1-5, 10-15, 20)"
        ),
        MethodView(
            type = SplitMethodType.SINGLE_PAGE_PER_FILE,
            title = "Single Page Files",
            description = "This will create ${selectedSplitPdf.pagesCount} separate PDF files, one for each page."
        ),
        MethodView(
            type = SplitMethodType.EVERY_N_PAGES,
            title = "Pages Per File",
            description = "Enter how many pages should each file contain"
        )
    )

    Column(
        modifier = Modifier
            .selectableGroup()
            .fillMaxWidth()
    ) {
        radioOptions.forEach { method ->

            MethodCard(
                method = method,
                viewModel = viewModel,
            )
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Colors.Surface.charcoalSlate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            val selectedMethodView = methodView.first { it.type == selectedMethod }

            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = selectedMethodView.title,
                        color = Colors.Text.blue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        text = selectedMethodView.description,
                        color = Colors.Text.secondary,
                        fontSize = 12.sp
                    )

                    if (selectedMethod == SplitMethodType.PAGE_RANGES) {
                        SplitInputField(
                            value = viewModel.splitRangesText,
                            onValueChange = viewModel::updateSplitRangesText,
                            placeholder = "1-5, 10-15, 20",
                            width = 220.dp
                        )
                    } else if (selectedMethod == SplitMethodType.EVERY_N_PAGES) {
                        SplitInputField(
                            value = viewModel.splitPagesPerFileText,
                            onValueChange = viewModel::updateSplitPagesPerFileText,
                            placeholder = "5",
                            width = 80.dp
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun MethodCard(
    method: SplitMethod,
    viewModel: SplitViewModel,
) {
    val selectedMethod = viewModel.selectedSplitMethod

    val isSelected = method.type == selectedMethod
    val surfaceColor = if (isSelected) Colors.Surface.selectedCard else Colors.Surface.card
    val borderColor = if (isSelected) Colors.Border.lightBlue else Colors.Border.darkGray
    val iconBGColor = if (isSelected) Colors.Icon.blue else Colors.Icon.darkGray
    val iconTint = if (isSelected) Colors.Icon.white else Colors.Icon.default

    Card(
        onClick = { viewModel.selectSplitMethod(method.type) },
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
            .padding(bottom = 10.dp)
    ) {
        ListItem(
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconBGColor)
                ) {
                    Icon(
                        imageVector = method.icon,
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
                    text = method.title,
                    color = Colors.Text.primary,
                    fontWeight = FontWeight.SemiBold
                )
            },
            supportingContent = {
                Text(
                    text = method.description,
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
/* ------------------------------------------------------- */

@Composable
private fun SplitPlanSummaryCard(
    splitPlanResult: SplitPlanResult?
) {
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
                text = "Output Summary",
                color = Colors.Text.blue,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            when (splitPlanResult) {
                is SplitPlanResult.Ready -> {
                    Text(
                        text = buildString {
                            append("${splitPlanResult.plan.outputFileCount} output ")
                            append(if (splitPlanResult.plan.outputFileCount == 1) "file" else "files")
                            append(" will be created")
                        },
                        color = Colors.Text.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "${splitPlanResult.plan.totalPagesCovered} pages will be included in total",
                        color = Colors.Text.secondary,
                        fontSize = 12.sp
                    )

                    splitPlanResult.plan.chunks
                        .take(5)
                        .forEachIndexed { index, chunk ->
                            SplitChunkSummaryRow(
                                fileIndex = index + 1,
                                chunk = chunk
                            )
                        }

                    val remainingCount = splitPlanResult.plan.outputFileCount - 5
                    if (remainingCount > 0) {
                        Text(
                            text = "...and $remainingCount more output files",
                            color = Colors.Text.secondary,
                            fontSize = 12.sp
                        )
                    }
                }

                is SplitPlanResult.Error -> {
                    Text(
                        text = splitPlanResult.message,
                        color = Colors.Button.red,
                        fontSize = 12.sp
                    )
                }

                null -> {
                    Text(
                        text = "Select a PDF to see the split summary.",
                        color = Colors.Text.secondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SplitChunkSummaryRow(
    fileIndex: Int,
    chunk: SplitChunk
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "File $fileIndex",
                color = Colors.Text.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = chunk.title,
                color = Colors.Text.secondary,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = chunk.summaryLine,
            color = Colors.Text.secondary,
            fontSize = 11.sp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SplitInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    width: Dp
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            color = Colors.Text.primary,
            textAlign = TextAlign.Start
        ),
        cursorBrush = SolidColor(Colors.Primary.blue),
        modifier = Modifier
            .width(width)
            .height(40.dp)
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    coroutineScope.launch {
                        delay(250)
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            }
            .clip(RoundedCornerShape(10.dp))
            .background(Colors.Primary.darkSlate)
            .padding(horizontal = 10.dp),
        decorationBox = { innerTextField ->
            TextFieldDefaults.DecorationBox(
                value = value,
                visualTransformation = VisualTransformation.None,
                innerTextField = innerTextField,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = Colors.Text.secondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                singleLine = true,
                enabled = true,
                interactionSource = remember { MutableInteractionSource() },
                contentPadding = PaddingValues(0.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Colors.Primary.darkSlate,
                    unfocusedContainerColor = Colors.Primary.darkSlate,
                    disabledContainerColor = Colors.Primary.darkSlate,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                )
            )
        }
    )
}
