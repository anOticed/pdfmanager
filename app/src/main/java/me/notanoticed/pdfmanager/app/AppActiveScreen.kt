package me.notanoticed.pdfmanager.app

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import me.notanoticed.pdfmanager.feature.images.ImagesViewModel
import me.notanoticed.pdfmanager.feature.merge.MergeViewModel
import me.notanoticed.pdfmanager.feature.pdflist.PdfListViewModel
import me.notanoticed.pdfmanager.feature.settings.SettingsViewModel
import me.notanoticed.pdfmanager.feature.split.SplitViewModel
import me.notanoticed.pdfmanager.ui.theme.PdfManagerTheme

@Composable
fun AppActiveScreen(
    darkTheme: Boolean,
    pagerState: PagerState,
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    pdfListViewModel: PdfListViewModel,
    mergeViewModel: MergeViewModel,
    splitViewModel: SplitViewModel,
    imagesViewModel: ImagesViewModel,
    settingsViewModel: SettingsViewModel
) {
    Crossfade(
        targetState = darkTheme,
        animationSpec = tween(durationMillis = 220),
        label = "app_theme_crossfade"
    ) { targetDarkTheme ->
        PdfManagerTheme(
            darkTheme = targetDarkTheme,
            applySystemBars = false
        ) {
            AppScaffold(
                pagerState = pagerState,
                currentDestination = currentDestination,
                onNavigate = onNavigate,
                pdfListViewModel = pdfListViewModel,
                mergeViewModel = mergeViewModel,
                splitViewModel = splitViewModel,
                imagesViewModel = imagesViewModel,
                settingsViewModel = settingsViewModel
            )
        }
    }
}
