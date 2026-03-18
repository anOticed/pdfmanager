/**
 * Top bar for the Images tab.
 */

package me.notanoticed.pdfmanager.feature.images

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- TOP BAR -------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagesTopBar(
    viewModel: ImagesViewModel,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    val isActive = viewModel.isActive
    val subtitle = when (val selectedCount = viewModel.selectedCount) {
        0 -> "No images selected"
        1 -> "1 image selected"
        else -> "$selectedCount images selected"
    }

    Column {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Images to PDF",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Colors.Text.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            actions = {
                Button(
                    onClick = onCameraClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Colors.Button.green
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = "Camera",
                        tint = Colors.Icon.white
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "Camera",
                        color = Colors.Primary.white
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onGalleryClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Colors.Button.blue
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = "Gallery",
                        tint = Colors.Icon.white
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "Gallery",
                        color = Colors.Primary.white
                    )
                }

                if (isActive) {
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = onCloseClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Colors.Button.iconBackground,
                            contentColor = Colors.Icon.default
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close images",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Colors.Surface.card,
                titleContentColor = Colors.Text.primary,
                actionIconContentColor = Colors.Icon.default
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Colors.Border.subtle)
        )
    }
}
/* ------------------------------------------------- */
