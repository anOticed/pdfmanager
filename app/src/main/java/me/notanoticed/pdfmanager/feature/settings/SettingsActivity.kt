/**
 * Preview-only wrapper composable for the Settings tab.
 */

package me.notanoticed.pdfmanager.feature.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.notanoticed.pdfmanager.app.AppBottomBar
import me.notanoticed.pdfmanager.app.Screen
import me.notanoticed.pdfmanager.ui.theme.Colors
import me.notanoticed.pdfmanager.ui.theme.PdfManagerTheme



/* -------------------- ACTIVITY -------------------- */
@Composable
fun SettingsActivity(active: Screen, onSelect: (String) -> Unit) {
    var isDarkModeEnabled by remember { mutableStateOf(true) }

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
            isDarkModeEnabled = isDarkModeEnabled,
            onDarkModeChange = { isDarkModeEnabled = it }
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
