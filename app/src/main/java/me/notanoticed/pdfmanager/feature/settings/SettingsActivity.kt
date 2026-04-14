/**
 * Preview-only wrapper composable for the Settings tab.
 */

package me.notanoticed.pdfmanager.feature.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import me.notanoticed.pdfmanager.app.AppBottomBar
import me.notanoticed.pdfmanager.app.Screen
import me.notanoticed.pdfmanager.ui.theme.Colors
import me.notanoticed.pdfmanager.ui.theme.PdfManagerTheme



/* -------------------- ACTIVITY -------------------- */
@Composable
fun SettingsActivity(active: Screen, onSelect: (String) -> Unit) {
    val settingsViewModel: SettingsViewModel = viewModel()

    Scaffold(
        topBar = {
            SettingsTopBar()
        },
        bottomBar = {
            AppBottomBar(
                currentRoute = active.route,
                onItemClick = onSelect
            )
        },
        containerColor = Colors.Background.app
    ) { pads ->
        SettingsScreen(
            modifier = Modifier.padding(pads),
            viewModel = settingsViewModel
        )
    }
}
/* -------------------------------------------------- */



@Preview(showBackground = true)
@Composable
fun SettingsActivityPreview() {
    PdfManagerTheme {
        SettingsActivity(active = Screen.Settings, onSelect = {})
    }
}
