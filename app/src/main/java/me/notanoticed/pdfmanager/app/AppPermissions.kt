/**
 * App permission coordinator.
 *
 * Centralizes permission flow and UI for:
 * - all-files storage access
 * - camera permission
 */

package me.notanoticed.pdfmanager.app

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

enum class AppPermissionType {
    STORAGE_ALL_FILES,
    CAMERA
}

interface AppPermissions {
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

val LocalAppPermissions = staticCompositionLocalOf<AppPermissions> {
    error("No app permissions provider")
}

private data class PendingPermissionRequest(
    val type: AppPermissionType,
    val isBlocking: Boolean,
    val escalateToBlockingOnDeny: Boolean,
    val onGranted: () -> Unit,
    val onDenied: () -> Unit
)

@Composable
fun ProvideAppPermissions(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var pendingRequest by remember { mutableStateOf<PendingPermissionRequest?>(null) }

    fun isGranted(type: AppPermissionType): Boolean {
        return when (type) {
            AppPermissionType.STORAGE_ALL_FILES -> {
                Environment.isExternalStorageManager()
            }
            AppPermissionType.CAMERA -> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    fun clearWithDenied() {
        val request = pendingRequest ?: return
        pendingRequest = null
        request.onDenied()
    }

    fun escalateOrDeny() {
        val request = pendingRequest ?: return
        if (request.escalateToBlockingOnDeny) {
            pendingRequest = request.copy(isBlocking = true)
            return
        }
        clearWithDenied()
    }

    fun resolveResult(granted: Boolean) {
        val request = pendingRequest ?: return
        if (granted) {
            pendingRequest = null
            request.onGranted()
        } else {
            escalateOrDeny()
        }
    }

    val manageAllFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        resolveResult(isGranted(AppPermissionType.STORAGE_ALL_FILES))
    }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        resolveResult(granted)
    }

    val permissions = object : AppPermissions {
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
                type = AppPermissionType.STORAGE_ALL_FILES,
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
                type = AppPermissionType.CAMERA,
                onGranted = onGranted,
                onDenied = onDenied,
                isBlocking = false,
                escalateToBlockingOnDeny = false
            )
        }
    }

    fun onGrantClick() {
        when (pendingRequest?.type) {
            AppPermissionType.STORAGE_ALL_FILES -> {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = "package:${context.packageName}".toUri()
                }
                manageAllFilesLauncher.launch(intent)
            }
            AppPermissionType.CAMERA -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            null -> Unit
        }
    }

    fun onCancel() {
        escalateOrDeny()
    }

    CompositionLocalProvider(LocalAppPermissions provides permissions) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()

            val currentRequest = pendingRequest
            if (currentRequest != null) {
                AppPermissionDialog(
                    visible = true,
                    type = currentRequest.type,
                    isBlocking = currentRequest.isBlocking,
                    onGrantClick = ::onGrantClick,
                    onCancel = ::onCancel
                )
            }
        }
    }
}
