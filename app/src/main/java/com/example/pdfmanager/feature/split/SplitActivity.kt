package com.example.pdfmanager.feature.split


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
fun SplitActivity(
    active: Screen,
    onSelect: (String) -> Unit,
    viewModel: SplitViewModel,
) {
    val selectedSplitPdf = viewModel.selectedSplitPdf

    Scaffold(
        topBar = {
            SplitTopBar(
                viewModel = viewModel,
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

        if (selectedSplitPdf == null) {
            SplitScreen(modifier = Modifier.padding(pads))
        }
        else {
            SplitActiveScreen(
                modifier = Modifier.padding(pads),
                viewModel = viewModel
            )
        }
    }
}
/* -------------------------------------------------- */



@Preview(showBackground = true)
@Composable
fun SplitActivityPreview() {
    PdfManagerTheme {
        SplitActivity(
            active = Screen.Split,
            onSelect = {},
            viewModel = SplitViewModel()
        )
    }
}