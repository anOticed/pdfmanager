/**
 * Universal permission dialog UI.
 *
 * One visual component used for:
 * - all-files storage permission
 * - camera permission
 */

package me.notanoticed.pdfmanager.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.ui.theme.Colors
import me.notanoticed.pdfmanager.ui.theme.PdfManagerTheme

/* -------------------- MODEL -------------------- */
data class PermissionDialogTexts(
    val icon: ImageVector,
    val title: String,
    val message: String,
    val grantButtonText: String
)

private fun buildPermissionDialogTexts(
    type: AppPermissionType,
    grantButtonText: String,
    title: String,
    message: String
): PermissionDialogTexts {
    return when (type) {
        AppPermissionType.STORAGE_ALL_FILES -> {
            PermissionDialogTexts(
                icon = Icons.Outlined.Folder,
                title = title,
                message = message,
                grantButtonText = grantButtonText
            )
        }
        AppPermissionType.CAMERA -> {
            PermissionDialogTexts(
                icon = Icons.Outlined.CameraAlt,
                title = title,
                message = message,
                grantButtonText = grantButtonText
            )
        }
    }
}
/* ----------------------------------------------- */


/* -------------------- PERMISSION DIALOG -------------------- */
@Composable
fun AppPermissionDialog(
    visible: Boolean,
    type: AppPermissionType,
    isBlocking: Boolean,
    onGrantClick: () -> Unit,
    onCancel: () -> Unit
) {
    if (!visible) return

    val title = when (type) {
        AppPermissionType.STORAGE_ALL_FILES -> {
            if (isBlocking) {
                stringResource(R.string.permission_storage_required_title)
            } else {
                stringResource(R.string.permission_required_title)
            }
        }
        AppPermissionType.CAMERA -> {
            if (isBlocking) {
                stringResource(R.string.permission_camera_required_title)
            } else {
                stringResource(R.string.permission_required_title)
            }
        }
    }
    val message = when (type) {
        AppPermissionType.STORAGE_ALL_FILES -> {
            if (isBlocking) {
                stringResource(R.string.permission_storage_blocking_message)
            } else {
                stringResource(R.string.permission_storage_message)
            }
        }
        AppPermissionType.CAMERA -> {
            if (isBlocking) {
                stringResource(R.string.permission_camera_blocking_message)
            } else {
                stringResource(R.string.permission_camera_message)
            }
        }
    }
    val grantButtonText = when (type) {
        AppPermissionType.STORAGE_ALL_FILES -> stringResource(R.string.permission_storage_grant)
        AppPermissionType.CAMERA -> stringResource(R.string.permission_camera_grant)
    }
    val texts = buildPermissionDialogTexts(
        type = type,
        grantButtonText = grantButtonText,
        title = title,
        message = message
    )

    Dialog(
        onDismissRequest = {
            if (!isBlocking) onCancel()
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(24.dp),
                color = Colors.Surface.card,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(72.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(
                                    color = Colors.Primary.lightBlue,
                                    shape = CircleShape
                                )
                        )
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = Colors.Primary.blue,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = texts.icon,
                                contentDescription = null,
                                tint = Colors.Icon.white
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = texts.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Colors.Text.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = texts.message,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Colors.Text.secondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!isBlocking) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedButton(
                                onClick = onCancel,
                                shape = RoundedCornerShape(50.dp),
                                border = BorderStroke(1.5.dp, Colors.Border.blue),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                contentPadding = PaddingValues(
                                    horizontal = 20.dp,
                                    vertical = 12.dp
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.action_cancel),
                                    fontSize = 13.sp,
                                    color = Colors.Text.blue
                                )
                            }

                            Spacer(modifier = Modifier.size(12.dp))

                            Button(
                                onClick = onGrantClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Colors.Button.blue
                                ),
                                contentPadding = PaddingValues(
                                    horizontal = 20.dp,
                                    vertical = 12.dp
                                )
                            ) {
                                Text(
                                    text = texts.grantButtonText,
                                    fontSize = 13.sp,
                                    color = Colors.Primary.white
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = onGrantClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Colors.Button.blue
                            )
                        ) {
                            Text(
                                text = texts.grantButtonText,
                                fontSize = 13.sp,
                                color = Colors.Primary.white
                            )
                        }
                    }
                }
            }
        }
    }
}
/* ----------------------------------------------------------- */


@Preview(showBackground = true)
@Composable
fun ShowPermissionDialogPreview() {
    PdfManagerTheme {
        AppPermissionDialog(
            visible = true,
            type = AppPermissionType.CAMERA,
            isBlocking = false,
            onGrantClick = {},
            onCancel = {}
        )
    }
}
