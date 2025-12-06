package me.anoticed.pdfmanager.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import me.anoticed.pdfmanager.feature.merge.MergeActiveScreen
import me.anoticed.pdfmanager.feature.merge.MergeScreen
import me.anoticed.pdfmanager.feature.merge.MergeViewModel
import me.anoticed.pdfmanager.feature.pdflist.PdfListScreen
import me.anoticed.pdfmanager.feature.pdflist.PdfListViewModel
import me.anoticed.pdfmanager.feature.settings.SettingsScreen
import me.anoticed.pdfmanager.feature.split.SplitActiveScreen
import me.anoticed.pdfmanager.feature.split.SplitScreen
import me.anoticed.pdfmanager.feature.split.SplitViewModel
import me.anoticed.pdfmanager.ui.theme.Colors
import me.anoticed.pdfmanager.ui.theme.PdfManagerTheme
import kotlinx.coroutines.launch
import me.anoticed.pdfmanager.feature.pdflist.OptionsOverlay
import me.anoticed.pdfmanager.feature.pdflist.PdfListSelectionBottomBar

/* -------------------- APP -------------------- */
@Composable
fun App() = PdfManagerTheme {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { appScreens.size })

    val pdfListViewModel: PdfListViewModel = viewModel()
    val mergeViewModel: MergeViewModel = viewModel()
    val splitViewModel: SplitViewModel = viewModel()

    PdfListEventHandler(
        pdfListViewModel = pdfListViewModel,
        mergeViewModel = mergeViewModel,
        splitViewModel = splitViewModel,
        pagerState = pagerState,
        tabs = appScreens
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    currentRoute = appScreens[pagerState.currentPage],
                    pdfListViewModel = pdfListViewModel,
                    splitViewModel = splitViewModel,
                    mergeViewModel = mergeViewModel
                )
            },
            bottomBar = {
                if (appScreens[pagerState.currentPage] == Screen.PdfList.route && pdfListViewModel.isSelectionMode) {
                    PdfListSelectionBottomBar(viewModel = pdfListViewModel)
                }
                else {
                    AppBottomBar(
                        currentRoute = appScreens[pagerState.currentPage]
                    ) { page ->

                        scope.launch {
                            pagerState.animateScrollToPage(page = appScreens.indexOf(page))
                        }
                    }
                }
            },
            containerColor = Colors.backgroundColor
        ) { paddingValues ->
            HorizontalPager(
                state = pagerState,
                key = { appScreens[it] },
                beyondViewportPageCount = 2,
                modifier = Modifier.padding(paddingValues)
            ) { page ->
                when (appScreens[page]) {
                    Screen.PdfList.route -> PdfListScreen(viewModel = pdfListViewModel)
                    Screen.Merge.route -> {
                        if (mergeViewModel.isActive) {
                            MergeActiveScreen(viewModel = mergeViewModel)
                        }
                        else {
                            MergeScreen()
                        }
                    }
                    Screen.Split.route -> {
                        val selected = splitViewModel.selectedSplitPdf
                        if (selected != null) {
                            SplitActiveScreen(viewModel = splitViewModel)
                        }
                        else SplitScreen()
                    }
                    Screen.Images.route -> null
                    Screen.Settings.route -> SettingsScreen()
                }
            }
        }


        OptionsOverlay(
            visible = pdfListViewModel.optionsPanelVisible,
            pdf = pdfListViewModel.optionsPanelPdf,
            onDismiss = { pdfListViewModel.closeOptions() },
            onAction = {action ->
                val pdf = pdfListViewModel.optionsPanelPdf ?: return@OptionsOverlay
                pdfListViewModel.onFileOptionSelected(action, pdf)
            }
        )
    }

}
/* --------------------------------------------- */



/* -------------------- SCREENS -------------------- */
sealed class Screen(val route: String) {
    data object PdfList : Screen("pdfs")
    data object Merge : Screen("merge")
    data object Split : Screen("split")
    data object Images : Screen("images")
    data object Settings : Screen("settings")
}

val appScreens = listOf(
    Screen.PdfList.route,
    Screen.Merge.route,
    Screen.Split.route,
    Screen.Images.route,
    Screen.Settings.route
)
/* ------------------------------------------------- */