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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.notanoticed.pdfmanager.ui.theme.Colors
import me.notanoticed.pdfmanager.ui.theme.PdfManagerTheme

/* -------------------- PERMISSION DIALOG -------------------- */
@Composable
fun StoragePermissionDialog(
    visible: Boolean,
    isBlocking: Boolean,
    onGrantClick: () -> Unit,
    onCancel: () -> Unit
) {
    if (!visible) return

    Dialog(
        onDismissRequest = {
            if (!isBlocking) {
                onCancel()
            }
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
                                imageVector = Icons.Outlined.Folder,
                                contentDescription = null,
                                tint = Colors.Icon.white,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val title = if (isBlocking) {
                        "Storage access is required"
                    } else {
                        "Permission required"
                    }

                    val message = if (isBlocking) {
                        "PDF Manager can't function properly without \"All files access\". \n\n" +
                                "Please enable it in system settings to continue using the app."
                    } else {
                        "PDF Manager needs access to your device storage to function properly. \n\n" +
                                "Your documents stay private and secure."
                    }

                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Colors.Text.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = message,
                        fontSize = 14.sp,
                        color = Colors.Text.secondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!isBlocking) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton (
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
                                    text = "Grant All Files Access",
                                    fontSize = 14.sp,
                                    color = Colors.Text.primary
                                )
                            }
                        }
                    }
                    else {
                        Button(
                            onClick = onGrantClick,
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Colors.Button.blue
                            )
                        ) {
                            Text(
                                text = "Grant All Files Access",
                                fontSize = 14.sp,
                                color = Colors.Text.primary
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
        StoragePermissionDialog(
            visible = true,
            isBlocking = false,
            onGrantClick = {},
            onCancel = {}
        )
    }
}