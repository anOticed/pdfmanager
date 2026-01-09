package me.notanoticed.pdfmanager.feature.images

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
    val selectedCount = viewModel.selectedCount

    Column {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Images to PDF",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val subtitle = when (selectedCount) {
                        1 -> { "1 image selected" }
                        else -> "$selectedCount images selected"
                    }

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
                        color = Colors.Text.primary
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
                        color = Colors.Text.primary,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isActive) {
                    FilledIconButton(
                        onClick = onCloseClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Colors.Button.iconBackground,
                            contentColor = Colors.Icon.white
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close Files",
                            modifier = Modifier.fillMaxSize(0.5f)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Colors.Surface.card,
                titleContentColor = Colors.Text.primary,
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