/**
 * Preview overlay container.
 *
 * A Scaffold that swaps the main app bars with preview-specific bars and renders
 * PreviewScreen as its content.
 */

package me.notanoticed.pdfmanager.feature.preview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- ACTIVITY -------------------- */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreviewActivity(
    request: PreviewRequest,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val canShowSearch = when (request) {
        is PreviewRequest.Single -> request.allowSearch
        is PreviewRequest.Split -> true
    }
    val searchToggleRequestNonce = remember(request) { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Colors.Background.app,
        topBar = {
            PreviewTopBar(
                title = request.topBarTitle,
                onBack = onBack,
                showSearch = canShowSearch,
                onSearchClick = {
                    searchToggleRequestNonce.intValue += 1
                }
            )
        },
        bottomBar = { PreviewBottomBar() }
    ) { paddingValues ->
        PreviewScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding(),
            request = request,
            searchToggleRequestNonce = searchToggleRequestNonce.intValue
        )
    }
}
/* -------------------------------------------------- */
