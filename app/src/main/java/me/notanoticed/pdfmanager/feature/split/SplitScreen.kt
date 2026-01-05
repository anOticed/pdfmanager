package me.notanoticed.pdfmanager.feature.split

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CallSplit
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.notanoticed.pdfmanager.core.toast.BindViewModelToasts
import me.notanoticed.pdfmanager.feature.preview.LocalPreviewNav
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- SCREEN -------------------- */
@Composable
fun SplitActiveScreen(
    modifier: Modifier = Modifier,
    viewModel: SplitViewModel
) {
    BindViewModelToasts(viewModel)
    val selectedSplitPdf = viewModel.selectedSplitPdf ?: return
    val selectedSplitMethodId = viewModel.selectedSplitMethodId
    val previewNav = LocalPreviewNav.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(48.dp)
                            .height(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Colors.Surface.thumbnail)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = "",
                            tint = Colors.Icon.gray,
                        )
                    }

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
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { previewNav.openSplit(pdf = selectedSplitPdf, splitMethodId = selectedSplitMethodId) },
                        colors = ButtonDefaults.buttonColors().copy(
                            containerColor = Colors.Button.darkSlate
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = "PDF icon",
                            tint = Colors.Icon.white,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Preview",
                            color = Colors.Text.primary,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = { /* TODO: split */ },
                        colors = ButtonDefaults.buttonColors().copy(
                            containerColor = Colors.Button.red
                        ),
                        modifier = Modifier.weight(1.75f),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = "PDF icon",
                            tint = Colors.Icon.white,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Split PDF",
                            color = Colors.Text.primary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

    }

}

data class SplitMethod(
    val id: Int,
    val title: String,
    val description: String,
    val icon: ImageVector
)

data class MethodView(
    val id: Int,
    val title: String,
    val description: String
)

@Composable
fun RadioButtonSingleSelection(
    viewModel: SplitViewModel
) {
    val selectedSplitPDF = viewModel.selectedSplitPdf ?: return
    val selectedOptionId = viewModel.selectedSplitMethodId

    val radioOptions = listOf(
        SplitMethod(
            id = 0,
            title = "By Page Ranges",
            description = "Split into specific ranges (e.g., 1-5, 10-15)",
            icon = Icons.Outlined.ContentCut
        ),
        SplitMethod(
            id = 1,
            title = "One Page Per File",
            description = "Create a separate file for each page",
            icon = Icons.Outlined.FileCopy
        ),
        SplitMethod(
            id = 2,
            title = "Every N Pages",
            description = "Split into files with N pages each",
            icon = Icons.Outlined.Tag
        )
    )

    val methodView = listOf(
        MethodView(
            id = 0,
            title = "Page Ranges",
            description = "Enter page ranges separated by commas (e.g., 1-5, 10-15, 20)"
        ),
        MethodView(
            id = 1,
            title = "Single Page Files",
            description = "This will create ${selectedSplitPDF.pagesCount} separate PDF files, one for each page."
        ),
        MethodView(
            id = 2,
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
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = methodView[selectedOptionId].title,
                        color = Colors.Text.blue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        text = methodView[selectedOptionId].description,
                        color = Colors.Text.secondary,
                        fontSize = 12.sp
                    )

                    if (selectedOptionId == SPLIT_METHOD_RANGES) {
                        val textFieldState = rememberTextFieldState()

                        BasicTextField(
                            state = textFieldState,
                            lineLimits = TextFieldLineLimits.SingleLine,
                            textStyle = LocalTextStyle.current.copy(
                                color = Colors.Text.primary,
                                textAlign = TextAlign.Start
                            ),
                            cursorBrush = SolidColor(Colors.Primary.white),
                            modifier = Modifier
                                .width(220.dp)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Colors.Primary.darkSlate)
                                .padding(horizontal = 10.dp),
                            decorator = { innerTextField ->

                                TextFieldDefaults.DecorationBox(
                                    value = textFieldState.text.toString(),
                                    visualTransformation = VisualTransformation.None,
                                    innerTextField = innerTextField,
                                    placeholder = {
                                        Text(
                                            text = "1-5, 10-15, 20",
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
                                    colors = TextFieldDefaults.colors().copy(
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
                    else if (selectedOptionId == SPLIT_METHOD_EVERY_N_PAGES) {
                        val textFieldState = rememberTextFieldState()

                        BasicTextField(
                            state = textFieldState,
                            lineLimits = TextFieldLineLimits.SingleLine,
                            textStyle = LocalTextStyle.current.copy(
                                color = Colors.Text.primary,
                                textAlign = TextAlign.Start
                            ),
                            cursorBrush = SolidColor(Colors.Primary.white),
                            modifier = Modifier
                                .width(80.dp)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Colors.Primary.darkSlate)
                                .padding(horizontal = 10.dp),
                            decorator = { innerTextField ->

                                TextFieldDefaults.DecorationBox(
                                    value = textFieldState.text.toString(),
                                    visualTransformation = VisualTransformation.None,
                                    innerTextField = innerTextField,
                                    placeholder = {
                                        Text(
                                            text = "5",
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
                                    colors = TextFieldDefaults.colors().copy(
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
    val selectedOptionId = viewModel.selectedSplitMethodId

    val surfaceColor = if (method.id == selectedOptionId) Colors.Surface.selectedCard else Colors.Surface.card
    val borderColor = if (method.id == selectedOptionId) Colors.Border.lightBlue else Colors.Border.darkGray
    val iconBGColor = if (method.id == selectedOptionId) Colors.Icon.blue else Colors.Icon.darkGray

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(bottom = 10.dp)
            .border(
                border = BorderStroke(
                    width = 2.dp,
                    color = borderColor
                ),
                shape = RoundedCornerShape(10.dp)
            ),
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
                        tint = Colors.Icon.white,
                        modifier = Modifier
                            .fillMaxSize(0.65f)
                            .align(Alignment.Center)
                    )
                }
            },
            headlineContent = {
                Text(text = method.title, color = Colors.Text.primary, fontWeight = FontWeight.SemiBold) }
            ,
            supportingContent = {
                Text(text = method.description, color = Colors.Text.secondary, fontSize = 12.sp)
            },
            trailingContent = {
                RadioButton(
                    selected = (method.id == selectedOptionId),
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Colors.Button.skyBlue
                    )
                )
            },
            colors = ListItemDefaults.colors().copy(
                containerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .selectable(
                    selected = (method.id == selectedOptionId),
                    onClick = { viewModel.selectSplitMethod(method.id) },
                    role = Role.RadioButton
                )
        )

    }
}


@Composable
fun SplitScreen(
    modifier: Modifier = Modifier,
    viewModel: SplitViewModel
) {
    BindViewModelToasts(viewModel)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.CallSplit,
            contentDescription = null,
            tint = Colors.Icon.default,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No PDF selected",
            color = Colors.Text.secondary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Select a PDF to split it into multiple files using different methods.",
            color = Colors.Text.muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 48.dp)
        )
    }
}
/* ------------------------------------------------ */