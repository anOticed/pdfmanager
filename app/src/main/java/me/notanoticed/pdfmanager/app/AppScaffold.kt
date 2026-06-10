package me.notanoticed.pdfmanager.app

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CallMerge
import androidx.compose.material.icons.automirrored.outlined.CallSplit
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.feature.images.ImagesScreen
import me.notanoticed.pdfmanager.feature.images.ImagesTopBar
import me.notanoticed.pdfmanager.feature.images.ImagesViewModel
import me.notanoticed.pdfmanager.feature.merge.MergeScreen
import me.notanoticed.pdfmanager.feature.merge.MergeTopBar
import me.notanoticed.pdfmanager.feature.merge.MergeViewModel
import me.notanoticed.pdfmanager.feature.pdflist.PdfListScreen
import me.notanoticed.pdfmanager.feature.pdflist.PdfListTopBar
import me.notanoticed.pdfmanager.feature.pdflist.PdfListViewModel
import me.notanoticed.pdfmanager.feature.settings.SettingsScreen
import me.notanoticed.pdfmanager.feature.settings.SettingsTopBar
import me.notanoticed.pdfmanager.feature.settings.SettingsViewModel
import me.notanoticed.pdfmanager.feature.split.SplitScreen
import me.notanoticed.pdfmanager.feature.split.SplitTopBar
import me.notanoticed.pdfmanager.feature.split.SplitViewModel
import me.notanoticed.pdfmanager.core.system.permissions.LocalAppPermissions
import me.notanoticed.pdfmanager.core.system.pickers.LocalFilePickers
import me.notanoticed.pdfmanager.feature.pdflist.PdfListSelectionBar
import me.notanoticed.pdfmanager.ui.theme.Colors

@Composable
fun AppScaffold(
    pagerState: PagerState,
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    pdfListViewModel: PdfListViewModel,
    mergeViewModel: MergeViewModel,
    splitViewModel: SplitViewModel,
    imagesViewModel: ImagesViewModel,
    settingsViewModel: SettingsViewModel
) {
    Scaffold(
        topBar = {
            AppTopBar(
                destination = currentDestination,
                pdfListViewModel = pdfListViewModel,
                mergeViewModel = mergeViewModel,
                splitViewModel = splitViewModel,
                imagesViewModel = imagesViewModel
            )
        },
        bottomBar = {
            if (currentDestination == AppDestination.PdfList && pdfListViewModel.isSelectionMode) {
                PdfListSelectionBar(viewModel = pdfListViewModel)
            } else {
                AppBottomBar(
                    currentDestination = currentDestination,
                    onNavigate = onNavigate
                )
            }
        },
        containerColor = Colors.Background.app
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            key = { AppDestination.fromIndex(it).route },
            beyondViewportPageCount = 2,
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) { page ->
            when (AppDestination.fromIndex(page)) {
                AppDestination.PdfList -> PdfListScreen(viewModel = pdfListViewModel)
                AppDestination.Merge -> MergeScreen(viewModel = mergeViewModel)
                AppDestination.Split -> SplitScreen(viewModel = splitViewModel)
                AppDestination.Images -> ImagesScreen(viewModel = imagesViewModel)
                AppDestination.Settings -> SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}

@Composable
private fun AppTopBar(
    destination: AppDestination,
    pdfListViewModel: PdfListViewModel,
    mergeViewModel: MergeViewModel,
    splitViewModel: SplitViewModel,
    imagesViewModel: ImagesViewModel
) {
    val context = LocalContext.current
    val filePickers = LocalFilePickers.current
    val appPermissions = LocalAppPermissions.current

    when (destination) {
        AppDestination.PdfList -> PdfListTopBar(
            viewModel = pdfListViewModel,
            totalDocuments = pdfListViewModel.pdfFiles.size
        )

        AppDestination.Merge -> MergeTopBar(
            viewModel = mergeViewModel,
            onAddClick = { mergeViewModel.pickMergePdfs(context, filePickers) },
            onCloseClick = mergeViewModel::clear
        )

        AppDestination.Split -> SplitTopBar(
            viewModel = splitViewModel,
            onAddClick = { splitViewModel.pickSplitPdf(context, filePickers) },
            onCloseClick = splitViewModel::closeSelectedSplitPdf
        )

        AppDestination.Images -> ImagesTopBar(
            viewModel = imagesViewModel,
            onCameraClick = {
                appPermissions.ensureCameraAccess(
                    onGranted = {
                        filePickers.takePhoto(
                            onCaptured = { uri -> imagesViewModel.addCapturedPhoto(context, uri) },
                            onError = { imagesViewModel.onCameraLaunchFailed(context) }
                        )
                    },
                    onDenied = { imagesViewModel.onCameraPermissionDenied(context) }
                )
            },
            onGalleryClick = {
                filePickers.pickImages { uris ->
                    imagesViewModel.addFromGallery(context, uris)
                }
            },
            onCloseClick = imagesViewModel::clear
        )

        AppDestination.Settings -> SettingsTopBar()
    }
}

private data class BottomNavItem(
    val destination: AppDestination,
    val label: String,
    val icon: ImageVector
)

@Composable
private fun AppBottomBar(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit
) {
    NavigationBar(containerColor = Colors.Background.surface) {
        bottomNavItems().forEach { item ->
            NavigationBarItem(
                selected = currentDestination == item.destination,
                onClick = { onNavigate(item.destination) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(text = item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Colors.Icon.blue,
                    selectedTextColor = Colors.Text.blue,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = Colors.Icon.default,
                    unselectedTextColor = Colors.Text.secondary
                )
            )
        }
    }
}

@Composable
private fun bottomNavItems(): List<BottomNavItem> {
    return listOf(
        BottomNavItem(AppDestination.PdfList, stringResource(R.string.tab_pdfs), Icons.Outlined.Description),
        BottomNavItem(AppDestination.Merge, stringResource(R.string.tab_merge), Icons.AutoMirrored.Outlined.CallMerge),
        BottomNavItem(AppDestination.Split, stringResource(R.string.tab_split), Icons.AutoMirrored.Outlined.CallSplit),
        BottomNavItem(AppDestination.Images, stringResource(R.string.tab_images), Icons.Outlined.Image),
        BottomNavItem(AppDestination.Settings, stringResource(R.string.tab_settings), Icons.Outlined.Settings)
    )
}
