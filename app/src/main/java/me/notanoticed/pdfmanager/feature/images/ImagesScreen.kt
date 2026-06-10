package me.notanoticed.pdfmanager.feature.images

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.core.system.toast.BindViewModelToasts
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- SCREEN -------------------- */
@Composable
fun ImagesScreen(
    modifier: Modifier = Modifier,
    viewModel: ImagesViewModel
) {
    BindViewModelToasts(viewModel)

    if (viewModel.isActive) {
        ImagesActiveScreen(
            modifier = modifier,
            viewModel = viewModel
        )
        return
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = Colors.Icon.default,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.images_empty_title),
            color = Colors.Text.secondary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.images_empty_subtitle),
            color = Colors.Text.muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 48.dp)
        )
    }
}
/* ------------------------------------------------ */
