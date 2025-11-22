package com.example.pdfmanager.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CallMerge
import androidx.compose.material.icons.automirrored.outlined.CallSplit
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pdfmanager.core.pickers.LocalPickers
import com.example.pdfmanager.feature.merge.MergeActiveScreen
import com.example.pdfmanager.feature.merge.MergeScreen
import com.example.pdfmanager.feature.merge.MergeTopBar
import com.example.pdfmanager.feature.merge.MergeViewModel
import com.example.pdfmanager.feature.pdflist.OptionsOverlay
import com.example.pdfmanager.feature.pdflist.PdfListScreen
import com.example.pdfmanager.feature.pdflist.PdfListTopBar
import com.example.pdfmanager.feature.pdflist.PdfListViewModel
import com.example.pdfmanager.feature.settings.SettingsScreen
import com.example.pdfmanager.feature.settings.SettingsTopBar
import com.example.pdfmanager.feature.split.SplitActiveScreen
import com.example.pdfmanager.feature.split.SplitScreen
import com.example.pdfmanager.feature.split.SplitTopBar
import com.example.pdfmanager.feature.split.SplitViewModel
import com.example.pdfmanager.ui.theme.Colors
import com.example.pdfmanager.ui.theme.PdfManagerTheme
import kotlinx.coroutines.launch


/* -------------------- SCREENS & TABS -------------------- */
sealed class Screen(val route: String) {
    data object PdfList : Screen("pdfs")
    data object Merge : Screen("merge")
    data object Split : Screen("split")
    data object Images : Screen("images")
    data object Settings : Screen("settings")
}

private val tabs = listOf(
    Screen.PdfList.route,
    Screen.Merge.route,
    Screen.Split.route,
    Screen.Images.route,
    Screen.Settings.route
)
/* -------------------------------------------------------- */



/* -------------------- APP -------------------- */
@Composable
fun App() = PdfManagerTheme {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    val pdfListViewModel: PdfListViewModel = viewModel()
    val mergeViewModel: MergeViewModel = viewModel()
    val splitViewModel: SplitViewModel = viewModel()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    currentRoute = tabs[pagerState.currentPage],
                    pdfListViewModel = pdfListViewModel,
                    splitViewModel = splitViewModel,
                    mergeViewModel = mergeViewModel
                )
            },
            bottomBar = {
                AppBottomBar(
                    currentRoute = tabs[pagerState.currentPage]
                ) { page ->

                    scope.launch {
                        pagerState.animateScrollToPage(page = tabs.indexOf(page))
                    }
                }
            },
            containerColor = Colors.backgroundColor
        ) { paddingValues ->
            HorizontalPager(
                state = pagerState,
                key = { tabs[it] },
                beyondViewportPageCount = 2,
                modifier = Modifier.padding(paddingValues)
            ) { page ->
                when (tabs[page]) {
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
            onDismiss = { pdfListViewModel.closeOptions() }
        )
    }

}
/* --------------------------------------------- */



/* -------------------- TOP BAR -------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentRoute: String,
    pdfListViewModel: PdfListViewModel,
    splitViewModel: SplitViewModel,
    mergeViewModel: MergeViewModel
) {
    val pickers = LocalPickers.current
    val context = LocalContext.current

    when (currentRoute) {
        Screen.PdfList.route -> PdfListTopBar(pdfListViewModel.pdfFiles.size)
        Screen.Merge.route -> MergeTopBar(
            total = mergeViewModel.pdfMergeFiles.size,
            isActive = mergeViewModel.isActive,
            onAddClick = {
                if (mergeViewModel.isActive) {
                    mergeViewModel.clear()
                }
                else {
                    mergeViewModel.setMergeFiles(mergeViewModel.sampleMergeFiles)
                }
            }
        )
        Screen.Split.route -> SplitTopBar(
            viewModel = splitViewModel,
            onAddClick = { splitViewModel.pickSplitPdf(context, pickers) },
        )
        Screen.Images.route -> null
        Screen.Settings.route -> SettingsTopBar()
    }
}
/* ------------------------------------------------- */



/* -------------------- BOTTOM BAR -------------------- */
data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun AppBottomBar(
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.Gray.copy(alpha = 0.2f)) // TODO: change
        )
        NavigationBar(
            containerColor = Colors.cardColor.copy(alpha = 0.9f)
        ) {
            val items = listOf(
                BottomNavItem(title = "PDFs", route = Screen.PdfList.route, icon = Icons.Outlined.Description),
                BottomNavItem(title = "Merge", route = Screen.Merge.route, icon = Icons.AutoMirrored.Outlined.CallMerge),
                BottomNavItem(title = "Split", route = Screen.Split.route, icon = Icons.AutoMirrored.Outlined.CallSplit),
                BottomNavItem(title = "Images", route = Screen.Images.route, icon = Icons.Outlined.Image),
                BottomNavItem(title = "Settings", route = Screen.Settings.route, icon = Icons.Outlined.Settings)
            )

            items.forEachIndexed { index, item ->

                NavigationBarItem(
                    selected = currentRoute == tabs[index],
                    onClick = { onItemClick(tabs[index]) },
                    icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                    label = { Text(text = item.title) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Colors.blueColor,
                        selectedTextColor = Colors.blueColor,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = Colors.textMutedColor,
                        unselectedTextColor = Colors.textMutedColor
                    )
                )
            }
        }
    }
}
/* ---------------------------------------------------- */