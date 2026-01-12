package me.notanoticed.pdfmanager.feature.merge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CallMerge
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.notanoticed.pdfmanager.core.toast.BindViewModelToasts
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- SCREEN -------------------- */
@Composable
fun MergeScreen(
    modifier: Modifier = Modifier,
    viewModel: MergeViewModel
) {
    BindViewModelToasts(viewModel)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.CallMerge,
            contentDescription = null,
            tint = Colors.Icon.default,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No PDFs selected",
            color = Colors.Text.secondary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Add PDFs to merge them into a single document.",
            color = Colors.Text.muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "You can reorder them by dragging.",
            color = Colors.Text.muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
/* ------------------------------------------------ */