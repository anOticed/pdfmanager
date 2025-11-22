package com.example.pdfmanager.feature.pdflist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pdfmanager.core.pdf.model.PdfFile
import com.example.pdfmanager.core.pdf.model.createdDate
import com.example.pdfmanager.core.pdf.model.metaLine
import com.example.pdfmanager.ui.theme.Colors

/* -------------------- SCREEN -------------------- */
@Composable
fun PdfListScreen(
    modifier: Modifier = Modifier,
    viewModel: PdfListViewModel,
) {
    val context = LocalContext.current

    var isLoading = viewModel.isLoading
    var errorText = viewModel.errorText
    val pdfFiles = viewModel.pdfFiles

    // Launcher for MANAGE_EXTERNAL_STORAGE intent (Android 11+)
    val manageAllFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Environment.isExternalStorageManager()) {
            viewModel.loadAll(context)
        } else {
            errorText = "All files access was not granted"
        }
    }

    // API <= 32
    val readStorageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.loadAll(context)
        } else {
            errorText = "Storage permission denied"
        }
    }

    LaunchedEffect(Unit) {
        when {
            // API 33+ (Android 13+)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (!Environment.isExternalStorageManager()) {
                    isLoading = false
                    viewModel.requestAllFilesAccess(context, manageAllFilesLauncher)
                } else {
                    viewModel.loadAll(context)
                }
            }

            // API 30–32 (Android 10, 11, 12)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                val granted = context.checkSelfPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

                if (!granted) {
                    isLoading = false
                    readStorageLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                } else {
                    viewModel.loadAll(context)
                }
            }
        }
    }

    when {
        isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Colors.blueColor)
            }
        }

        errorText != null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorText, color = Colors.textMutedColor)
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(pdfFiles) { file ->
                    val document = Document(
                        name = file.name,
                        meta = file.metaLine(),
                        time = file.createdDate(),
                        bitmap = file.bitmap,
                        locked = file.isLocked
                    )
                    DocumentCard(document = document, onMoreClick = { viewModel.openOptions(pdf = file) })
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}
/* ------------------------------------------------ */



/* -------------------- OPTIONS PANEL -------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsOverlay(
    visible: Boolean,
    pdf: PdfFile?,
    onDismiss: () -> Unit
) {
    if (!visible || pdf == null) return

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(2f / 3f)
                .padding(16.dp)
        ) {
            Text(
                text = pdf.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            Text("Open")
            Spacer(Modifier.height(8.dp))
            Text("Rename")
            Spacer(Modifier.height(8.dp))
            Text("Delete")
        }
    }
}
/* ------------------------------------------------------- */