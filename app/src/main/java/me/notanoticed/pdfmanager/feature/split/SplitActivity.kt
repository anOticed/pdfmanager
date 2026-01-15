/**
 * Preview-only wrapper composable for the Split tab.
 */

package me.notanoticed.pdfmanager.feature.split


import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.notanoticed.pdfmanager.app.AppBottomBar
import me.notanoticed.pdfmanager.app.Screen
import me.notanoticed.pdfmanager.ui.theme.Colors
import me.notanoticed.pdfmanager.ui.theme.PdfManagerTheme

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
                onAddClick = {},
                onCloseClick = {}
            )
        },
        bottomBar = {
            AppBottomBar(
                currentRoute = active.route,
                onItemClick = onSelect
            )
        },
        containerColor = Colors.Background.app
    ) { pads ->

        if (selectedSplitPdf == null) {
            SplitScreen(
                modifier = Modifier.padding(pads),
                viewModel = viewModel
            )
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



@SuppressLint("ViewModelConstructorInComposable")
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