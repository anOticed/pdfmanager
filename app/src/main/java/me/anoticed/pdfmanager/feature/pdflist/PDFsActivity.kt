package me.anoticed.pdfmanager.feature.pdflist

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.anoticed.pdfmanager.app.AppBottomBar
import me.anoticed.pdfmanager.app.Screen
import me.anoticed.pdfmanager.ui.theme.Colors
import me.anoticed.pdfmanager.ui.theme.PdfManagerTheme

/* -------------------- ACTIVITY -------------------- */
@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun PdfListActivity(active: Screen, onSelect: (String) -> Unit) {
    Scaffold(
        topBar = {
            PdfListTopBar(viewModel = PdfListViewModel(), totalDocuments = 0)
        },
        bottomBar = {
            AppBottomBar(
                currentRoute = active.route,
                onItemClick = onSelect
            )
        },
        containerColor = Colors.backgroundColor
    ) { paddingValues ->
        PdfListScreen(
            Modifier.padding(paddingValues),
            viewModel = PdfListViewModel()
        )
    }
}
/* -------------------------------------------------- */



@Preview(showBackground = true)
@Composable
fun PdfListActivityPreview() {
    PdfManagerTheme {
        PdfListActivity(active = Screen.PdfList, onSelect = {})
//        DocumentCard(Document("test.pdf", "meta", "12.12.2023"))
    }
}