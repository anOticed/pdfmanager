package com.example.pdfmanager.feature.settings

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
fun SettingsActivity(active: Screen, onSelect: (String) -> Unit) {
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
        containerColor = Colors.backgroundColor
    ) { pads -> SettingsScreen(Modifier.padding(pads)) }
}
/* -------------------------------------------------- */



@Preview(showBackground = true)
@Composable
fun SettingsActivityPreview() {
    PdfManagerTheme {
        SettingsActivity(active = Screen.Settings, onSelect = {})
    }
}