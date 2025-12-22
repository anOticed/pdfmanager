package me.notanoticed.pdfmanager.feature.preview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- ACTIVITY -------------------- */
@Composable
fun PreviewActivity(
    request: PreviewRequest,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Scaffold(
        containerColor = Colors.Background.app,
        topBar = { PreviewTopBar(title = request.topBarTitle, onBack = onBack) },
        bottomBar = { PreviewBottomBar() }
    ) { paddingValues ->
        PreviewScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            request = request
        )
    }
}
/* -------------------------------------------------- */