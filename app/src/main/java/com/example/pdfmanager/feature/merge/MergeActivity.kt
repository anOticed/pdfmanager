package com.example.pdfmanager.feature.merge

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.pdfmanager.app.AppBottomBar
import com.example.pdfmanager.app.Screen
import com.example.pdfmanager.ui.theme.Colors
import com.example.pdfmanager.ui.theme.PdfManagerTheme


/* -------------------- ACTIVITY -------------------- */
@Composable
fun MergeActivity(
    active: Screen,
    onSelect: (String) -> Unit,
    viewModel: MergeViewModel
) {
    Scaffold(
        topBar = {
            MergeTopBar(
                total = viewModel.pdfMergeFiles.size,
                isActive = viewModel.isActive,
                onAddClick = {}
            )
        },
        bottomBar = {
            AppBottomBar(
                currentRoute = active.route,
                onItemClick = onSelect
            )
        },
        containerColor = Colors.backgroundColor
    ) { pads ->
        if (viewModel.pdfMergeFiles.isEmpty()) {
            MergeScreen(Modifier.padding(pads))
        }
        else {
            MergeActiveScreen(
                Modifier.padding(pads),
                viewModel = viewModel
            )
        }
    }
}
/* -------------------------------------------------- */




@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun MergeActivityPreview() {
    PdfManagerTheme {
        MergeActivity(active = Screen.Merge, onSelect = {}, viewModel = MergeViewModel())
    }
}