package me.notanoticed.pdfmanager.app

import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
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
import me.notanoticed.pdfmanager.feature.split.SplitActiveScreen
import me.notanoticed.pdfmanager.feature.split.SplitScreen
import me.notanoticed.pdfmanager.feature.split.SplitViewModel
import me.notanoticed.pdfmanager.ui.theme.Colors
import me.notanoticed.pdfmanager.ui.theme.PdfManagerTheme
import kotlinx.coroutines.launch
import me.notanoticed.pdfmanager.feature.images.ImagesScreen
import me.notanoticed.pdfmanager.feature.images.ImagesViewModel
import me.notanoticed.pdfmanager.feature.pdflist.OptionsOverlay
import me.notanoticed.pdfmanager.feature.pdflist.PdfListSelectionBottomBar

/* -------------------- APP -------------------- */
@Composable
fun App() = PdfManagerTheme {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { appScreens.size })

    val pdfListViewModel: PdfListViewModel = viewModel()
    val mergeViewModel: MergeViewModel = viewModel()
    val splitViewModel: SplitViewModel = viewModel()
    val imagesViewModel: ImagesViewModel = viewModel()

    val context = LocalContext.current

    // MANAGE_EXTERNAL_STORAGE launcher (API 30+)
    val manageAllFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            Environment.isExternalStorageManager()
        ) {
            pdfListViewModel.onPermissionGranted()
            pdfListViewModel.loadAll(context)
        } else {
            pdfListViewModel.onPermissionDenied()
        }
    }

    // initial permission check
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                pdfListViewModel.loadAll(context)
            }
            else {
                pdfListViewModel.showPermissionExplanation()
            }
        }
        else {
            pdfListViewModel.loadAll(context)
        }
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
                        else SplitScreen(viewModel = splitViewModel)
                    }
                    Screen.Images.route -> ImagesScreen(viewModel = imagesViewModel)
                    Screen.Settings.route -> SettingsScreen()
                }
            }
        }

        StoragePermissionDialog(
            visible = pdfListViewModel.showPermissionDialog,
            isBlocking = pdfListViewModel.permissionDialogBlocking,
            onGrantClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    pdfListViewModel.requestAllFilesAccess(
                        context = context,
                        manageAllFilesLauncher = manageAllFilesLauncher
                    )
                }
            },
            onCancel = { pdfListViewModel.onPermissionDialogCancel() }
        )

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