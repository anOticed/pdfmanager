package me.notanoticed.pdfmanager.app

import android.content.Intent
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.notanoticed.pdfmanager.feature.compress.CompressViewModel
import me.notanoticed.pdfmanager.feature.images.ImagesViewModel
import me.notanoticed.pdfmanager.feature.merge.MergeViewModel
import me.notanoticed.pdfmanager.feature.pageeditor.ProvidePageEditor
import me.notanoticed.pdfmanager.feature.pageeditor.PageEditorViewModel
import me.notanoticed.pdfmanager.feature.pdflist.PdfListViewModel
import me.notanoticed.pdfmanager.feature.preview.LocalPreviewNav
import me.notanoticed.pdfmanager.feature.preview.ProvidePreview
import me.notanoticed.pdfmanager.feature.settings.SettingsViewModel
import me.notanoticed.pdfmanager.feature.split.SplitViewModel
import me.notanoticed.pdfmanager.core.system.export.ProvidePdfExportHost
import me.notanoticed.pdfmanager.core.system.pickers.ProvideFilePickers
import me.notanoticed.pdfmanager.core.system.permissions.ProvideAppPermissions
import me.notanoticed.pdfmanager.core.system.toast.ProvideToastHost
import me.notanoticed.pdfmanager.ui.theme.PdfManagerTheme

private data class AppFeatureGraph(
    val pdfList: PdfListViewModel,
    val merge: MergeViewModel,
    val split: SplitViewModel,
    val images: ImagesViewModel,
    val compress: CompressViewModel,
    val pageEditor: PageEditorViewModel,
    val settings: SettingsViewModel
)

@Composable
fun PdfManagerApp(
    externalIntent: Intent?,
    onExternalIntentConsumed: () -> Unit
) {
    val context = LocalContext.current
    val graph = AppFeatureGraph(
        pdfList = viewModel(),
        merge = viewModel(),
        split = viewModel(),
        images = viewModel(),
        compress = viewModel(),
        pageEditor = viewModel(),
        settings = viewModel()
    )
    val darkThemeEnabled = graph.settings.isDarkModeEnabled

    PdfManagerTheme(darkTheme = darkThemeEnabled) {
        ProvideToastHost {
            ProvideFilePickers {
                ProvidePreview {
                    val previewNav = LocalPreviewNav.current

                    ProvideAppPermissions {
                        ProvidePdfExportHost(
                            onExportFinished = { graph.pdfList.loadAll(context) }
                        ) {
                            ProvidePageEditor(viewModel = graph.pageEditor) {
                                val pagerState = rememberPagerState(pageCount = { AppDestination.entries.size })
                                val scope = rememberCoroutineScope()
                                val currentDestination = AppDestination.fromIndex(pagerState.currentPage)
                                val navigateTo: (AppDestination) -> Unit = remember(pagerState, scope) {
                                    { destination ->
                                        scope.launch {
                                            pagerState.animateScrollToPage(destination.ordinal)
                                        }
                                    }
                                }

                                AppStartup(
                                    pdfListViewModel = graph.pdfList,
                                )

                                ExternalPdfIntentHandler(
                                    externalIntent = externalIntent,
                                    onExternalIntentConsumed = onExternalIntentConsumed,
                                    mergeViewModel = graph.merge,
                                    splitViewModel = graph.split,
                                    previewNav = previewNav,
                                    onNavigate = navigateTo
                                )

                                AppEventDispatcher(
                                    pdfListViewModel = graph.pdfList,
                                    compressViewModel = graph.compress,
                                    mergeViewModel = graph.merge,
                                    splitViewModel = graph.split,
                                    previewNav = previewNav,
                                    onNavigate = navigateTo
                                )

                                AppActiveScreen(
                                    darkTheme = darkThemeEnabled,
                                    pagerState = pagerState,
                                    currentDestination = currentDestination,
                                    onNavigate = navigateTo,
                                    pdfListViewModel = graph.pdfList,
                                    mergeViewModel = graph.merge,
                                    splitViewModel = graph.split,
                                    imagesViewModel = graph.images,
                                    settingsViewModel = graph.settings
                                )

                                AppOverlays(
                                    pdfListViewModel = graph.pdfList,
                                    compressViewModel = graph.compress
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
