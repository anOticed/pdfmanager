/**
 * Top bar for the Images tab.
 */

package me.notanoticed.pdfmanager.feature.images

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.notanoticed.pdfmanager.R
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
    var isActionsMenuExpanded by remember { mutableStateOf(false) }
    val subtitle = pluralStringResource(
        R.plurals.images_selected_count,
        selectedCount,
        selectedCount
    )

    Column {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = stringResource(R.string.images_title),
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
                Box {
                    FilledIconButton(
                        onClick = { isActionsMenuExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Colors.Button.blue,
                            contentColor = Colors.Icon.white
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = stringResource(R.string.action_more),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isActionsMenuExpanded,
                        onDismissRequest = { isActionsMenuExpanded = false },
                        shape = RoundedCornerShape(12.dp),
                        containerColor = Colors.Surface.card
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.images_camera_action),
                                    color = Colors.Text.primary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = null,
                                    tint = Colors.Icon.green
                                )
                            },
                            onClick = {
                                isActionsMenuExpanded = false
                                onCameraClick()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.images_gallery_action),
                                    color = Colors.Text.primary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Image,
                                    contentDescription = null,
                                    tint = Colors.Icon.blue
                                )
                            },
                            onClick = {
                                isActionsMenuExpanded = false
                                onGalleryClick()
                            }
                        )
                    }
                }

                if (isActive) {
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
                            contentDescription = stringResource(R.string.images_close_action),
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
