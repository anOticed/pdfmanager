package me.notanoticed.pdfmanager.core.system.permissions

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

internal enum class AppPermissionType {
    StorageAllFiles,
    Camera
}

internal interface AppPermissions {
    fun requestPermission(
        type: AppPermissionType,
        onGranted: () -> Unit,
        onDenied: () -> Unit = {},
        isBlocking: Boolean = false,
        escalateToBlockingOnDeny: Boolean = false
    )

    fun ensureStorageAccess(
        onGranted: () -> Unit,
        onDenied: () -> Unit = {}
    )

    fun ensureCameraAccess(
        onGranted: () -> Unit,
        onDenied: () -> Unit = {}
    )
}

internal val LocalAppPermissions = staticCompositionLocalOf<AppPermissions> {
    error("AppPermissions not provided")
}

private data class PendingPermissionRequest(
    val type: AppPermissionType,
    val isBlocking: Boolean,
    val escalateToBlockingOnDeny: Boolean,
    val onGranted: () -> Unit,
    val onDenied: () -> Unit
)

@Composable
fun ProvideAppPermissions(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var pendingRequest by remember { mutableStateOf<PendingPermissionRequest?>(null) }

    fun isGranted(type: AppPermissionType): Boolean {
        return when (type) {
            AppPermissionType.StorageAllFiles -> Environment.isExternalStorageManager()
            AppPermissionType.Camera -> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    fun denyCurrentRequest() {
        val request = pendingRequest ?: return
        pendingRequest = null
        request.onDenied()
    }

    fun escalateOrDeny() {
        val request = pendingRequest ?: return
        if (request.escalateToBlockingOnDeny) {
            pendingRequest = request.copy(isBlocking = true)
        } else {
            denyCurrentRequest()
        }
    }

    fun completeCurrentRequest(granted: Boolean) {
        val request = pendingRequest ?: return
        if (granted) {
            pendingRequest = null
            request.onGranted()
        } else {
            escalateOrDeny()
        }
    }

    val storageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        completeCurrentRequest(isGranted(AppPermissionType.StorageAllFiles))
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        completeCurrentRequest(granted)
    }

    val permissions = remember {
        object : AppPermissions {
            override fun requestPermission(
                type: AppPermissionType,
                onGranted: () -> Unit,
                onDenied: () -> Unit,
                isBlocking: Boolean,
                escalateToBlockingOnDeny: Boolean
            ) {
                if (isGranted(type)) {
                    onGranted()
                    return
                }

                pendingRequest = PendingPermissionRequest(
                    type = type,
                    isBlocking = isBlocking,
                    escalateToBlockingOnDeny = escalateToBlockingOnDeny,
                    onGranted = onGranted,
                    onDenied = onDenied
                )
            }

            override fun ensureStorageAccess(
                onGranted: () -> Unit,
                onDenied: () -> Unit
            ) {
                requestPermission(
                    type = AppPermissionType.StorageAllFiles,
                    onGranted = onGranted,
                    onDenied = onDenied,
                    isBlocking = false,
                    escalateToBlockingOnDeny = true
                )
            }

            override fun ensureCameraAccess(
                onGranted: () -> Unit,
                onDenied: () -> Unit
            ) {
                requestPermission(
                    type = AppPermissionType.Camera,
                    onGranted = onGranted,
                    onDenied = onDenied
                )
            }
        }
    }

    fun launchPermissionRequest() {
        when (pendingRequest?.type) {
            AppPermissionType.StorageAllFiles -> {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = "package:${context.packageName}".toUri()
                }
                storageLauncher.launch(intent)
            }

            AppPermissionType.Camera -> {
                cameraLauncher.launch(Manifest.permission.CAMERA)
            }

            null -> Unit
        }
    }

    CompositionLocalProvider(LocalAppPermissions provides permissions) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()

            pendingRequest?.let { request ->
                AppPermissionDialog(
                    visible = true,
                    type = request.type,
                    isBlocking = request.isBlocking,
                    onGrantClick = ::launchPermissionRequest,
                    onCancel = ::escalateOrDeny
                )
            }
        }
    }
}
