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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    isBlocking: Boolean
): PermissionDialogTexts {
    return when (type) {
        AppPermissionType.STORAGE_ALL_FILES -> {
            PermissionDialogTexts(
                icon = Icons.Outlined.Folder,
                title = if (isBlocking) "Storage access is required" else "Permission required",
                message = if (isBlocking) {
                    "PDF Manager can't function properly without \"All files access\".\n\n" +
                        "Please enable it in system settings to continue using the app."
                } else {
                    "PDF Manager needs access to your device storage to function properly.\n\n" +
                        "Your documents stay private and secure."
                },
                grantButtonText = "Grant All Files Access"
            )
        }
        AppPermissionType.CAMERA -> {
            PermissionDialogTexts(
                icon = Icons.Outlined.CameraAlt,
                title = if (isBlocking) "Camera access is required" else "Permission required",
                message = if (isBlocking) {
                    "PDF Manager can't capture photos without camera access.\n\n" +
                        "Please grant camera permission to continue."
                } else {
                    "PDF Manager needs access to your camera to capture photos."
                },
                grantButtonText = "Grant Camera Access"
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

    val texts = buildPermissionDialogTexts(type = type, isBlocking = isBlocking)

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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Colors.Text.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = texts.message,
                        fontSize = 14.sp,
                        color = Colors.Text.secondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!isBlocking) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = onCancel,
                                modifier = Modifier.weight(0.5f),
                                shape = RoundedCornerShape(50.dp),
                                border = BorderStroke(1.5.dp, Colors.Border.blue),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                )
                            ) {
                                Text(
                                    text = "Cancel",
                                    fontSize = 14.sp,
                                    color = Colors.Text.blue
                                )
                            }

                            Button(
                                onClick = onGrantClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Colors.Button.blue
                                )
                            ) {
                                Text(
                                    text = texts.grantButtonText,
                                    fontSize = 14.sp,
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
                                fontSize = 14.sp,
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
