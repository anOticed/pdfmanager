/**
 * Shared compact selector for choosing how many pages should be placed on one sheet.
 */

package me.notanoticed.pdfmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.notanoticed.pdfmanager.core.pdf.PagesPerSheetOption
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- SELECTOR -------------------- */
@Composable
fun ExpandablePagesPerSheetSection(
    selectedOption: PagesPerSheetOption,
    onOptionSelected: (PagesPerSheetOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = !expanded }
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sheet Layout",
                color = Colors.Text.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Colors.Surface.card)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = selectedOption.summaryLabel,
                        color = Colors.Text.secondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = if (expanded) "Hide" else "Change",
                    color = Colors.Text.blue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            PagesPerSheetSelector(
                selectedOption = selectedOption,
                onOptionSelected = onOptionSelected
            )
        }
    }
}

@Composable
fun PagesPerSheetSelector(
    selectedOption: PagesPerSheetOption,
    onOptionSelected: (PagesPerSheetOption) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val spacing = 10.dp
        val cardWidth = (maxWidth - spacing * 2) / 3

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            PagesPerSheetOption.entries.forEach { option ->
                SheetLayoutCard(
                    option = option,
                    selected = option == selectedOption,
                    onClick = { onOptionSelected(option) },
                    modifier = Modifier.width(cardWidth)
                )
            }
        }
    }
}
/* ------------------------------------------------- */


/* -------------------- OPTION CARD -------------------- */
@Composable
private fun SheetLayoutCard(
    option: PagesPerSheetOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Colors.Surface.selectedCard else Colors.Surface.card,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) Colors.Border.blue else Colors.Border.darkGray
        ),
        modifier = modifier
            .height(116.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Colors.Background.surface)
                .padding(4.dp)
        ) {
            SheetPreview(option = option)

            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Colors.Primary.blue)
                )
            }
        }
    }
}
/* ----------------------------------------------------- */


/* -------------------- SHEET PREVIEW -------------------- */
@Composable
private fun SheetPreview(
    option: PagesPerSheetOption
) {
    when (option) {
        PagesPerSheetOption.ONE -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                MiniPage(
                    modifier = Modifier
                        .width(48.dp)
                        .height(70.dp)
                )
            }
        }

        PagesPerSheetOption.TWO -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MiniPage(
                    modifier = Modifier
                        .width(34.dp)
                        .height(38.dp)
                )
                MiniPage(
                    modifier = Modifier
                        .width(34.dp)
                        .height(38.dp)
                )
            }
        }

        PagesPerSheetOption.FOUR -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    MiniPage(
                        modifier = Modifier
                            .width(28.dp)
                            .height(32.dp)
                    )
                    MiniPage(
                        modifier = Modifier
                            .width(28.dp)
                            .height(32.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    MiniPage(
                        modifier = Modifier
                            .width(28.dp)
                            .height(32.dp)
                    )
                    MiniPage(
                        modifier = Modifier
                            .width(28.dp)
                            .height(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniPage(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Colors.Surface.thumbnail)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(2.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Colors.Border.subtle)
            )

            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Colors.Border.subtle)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.48f)
                    .height(2.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Colors.Border.subtle)
            )
        }
    }
}
/* ------------------------------------------------------ */
