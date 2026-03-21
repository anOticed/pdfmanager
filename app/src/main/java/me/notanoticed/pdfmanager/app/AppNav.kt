/**
 * Top-level app composition.
 *
 * Creates feature ViewModels, performs the initial storage-permission / first-load flow,
 * and renders the main tabbed UI using a HorizontalPager inside a single Scaffold.
 * This file also hosts global overlays (PDF options, PDF details, permission dialog).
 */

package me.notanoticed.pdfmanager.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import me.notanoticed.pdfmanager.feature.merge.MergeActiveScreen
import me.notanoticed.pdfmanager.feature.merge.MergeScreen
import me.notanoticed.pdfmanager.feature.merge.MergeViewModel
import me.notanoticed.pdfmanager.feature.pdflist.PdfListScreen
import me.notanoticed.pdfmanager.feature.pdflist.PdfListViewModel
import me.notanoticed.pdfmanager.feature.settings.SettingsScreen
import me.notanoticed.pdfmanager.feature.settings.SettingsViewModel
import me.notanoticed.pdfmanager.feature.split.SplitActiveScreen
import me.notanoticed.pdfmanager.feature.split.SplitScreen
import me.notanoticed.pdfmanager.feature.split.SplitViewModel
import me.notanoticed.pdfmanager.ui.theme.Colors
import me.notanoticed.pdfmanager.ui.theme.PdfManagerTheme
import kotlinx.coroutines.launch
import me.notanoticed.pdfmanager.feature.images.ImageActiveScreen
import me.notanoticed.pdfmanager.feature.images.ImagesScreen
import me.notanoticed.pdfmanager.feature.images.ImagesViewModel
import me.notanoticed.pdfmanager.feature.pdflist.OptionsOverlay
import me.notanoticed.pdfmanager.feature.pdflist.PdfDetailsOverlay
import me.notanoticed.pdfmanager.feature.pdflist.PdfListSelectionBottomBar

/* -------------------- APP -------------------- */
@Composable
fun App() {
    val settingsViewModel: SettingsViewModel = viewModel()

    PdfManagerTheme(darkTheme = settingsViewModel.isDarkModeEnabled) {
        AppContent(settingsViewModel = settingsViewModel)
    }
}

@Composable
private fun AppContent(settingsViewModel: SettingsViewModel) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { appScreens.size })

    val pdfListViewModel: PdfListViewModel = viewModel()
    val mergeViewModel: MergeViewModel = viewModel()
    val splitViewModel: SplitViewModel = viewModel()
    val imagesViewModel: ImagesViewModel = viewModel()

    val context = LocalContext.current

    ProvideAppPermissions {
        val appPermissions = LocalAppPermissions.current

        LaunchedEffect(Unit) {
            appPermissions.ensureStorageAccess(
                onGranted = {
                    pdfListViewModel.loadAll(context)
                }
            )
        }

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
                        mergeViewModel = mergeViewModel,
                        imagesViewModel = imagesViewModel
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
                containerColor = Colors.Background.app
            ) { paddingValues ->
                HorizontalPager(
                    state = pagerState,
                    key = { appScreens[it] },
                    beyondViewportPageCount = 2,
                    modifier = Modifier
                        .padding(paddingValues)
                        .consumeWindowInsets(paddingValues)
                ) { page ->
                    when (appScreens[page]) {
                        Screen.PdfList.route -> PdfListScreen(viewModel = pdfListViewModel)
                        Screen.Merge.route -> {
                            if (mergeViewModel.isActive) {
                                MergeActiveScreen(viewModel = mergeViewModel)
                            }
                            else {
                                MergeScreen(viewModel = mergeViewModel)
                            }
                        }
                        Screen.Split.route -> {
                            val selected = splitViewModel.selectedSplitPdf
                            if (selected != null) {
                                SplitActiveScreen(viewModel = splitViewModel)
                            }
                            else SplitScreen(viewModel = splitViewModel)
                        }
                        Screen.Images.route -> {
                            if (imagesViewModel.isActive) {
                                ImageActiveScreen(viewModel = imagesViewModel)
                            }
                            else {
                                ImagesScreen(viewModel = imagesViewModel)
                            }
                        }
                        Screen.Settings.route -> SettingsScreen(
                            isDarkModeEnabled = settingsViewModel.isDarkModeEnabled,
                            onDarkModeChange = settingsViewModel::updateDarkModeEnabled
                        )
                    }
                }
            }

            OptionsOverlay(
                visible = pdfListViewModel.optionsPanelVisible,
                pdf = pdfListViewModel.optionsPanelPdf,
                onDismiss = { pdfListViewModel.closeOptions() },
                onAction = {action ->
                    val pdf = pdfListViewModel.optionsPanelPdf ?: return@OptionsOverlay
                    pdfListViewModel.closeOptions()
                    pdfListViewModel.onFileOptionSelected(action, pdf)
                }
            )

            PdfDetailsOverlay(
                visible = pdfListViewModel.detailsPanelVisible,
                pdf = pdfListViewModel.detailsPanelPdf,
                onDismiss = { pdfListViewModel.closeDetails() }
            )
        }
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
