package me.notanoticed.pdfmanager.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import me.notanoticed.pdfmanager.core.system.permissions.LocalAppPermissions
import me.notanoticed.pdfmanager.feature.pdflist.PdfListViewModel

@Composable
fun AppStartup(
    pdfListViewModel: PdfListViewModel
) {
    val context = LocalContext.current
    val permissions = LocalAppPermissions.current

    LaunchedEffect(Unit) {
        permissions.ensureStorageAccess(
            onGranted = { pdfListViewModel.loadAll(context) }
        )
    }
}
