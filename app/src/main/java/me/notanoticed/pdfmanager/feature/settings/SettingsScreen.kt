/**
 * Settings tab UI.
 *
 * Currently a minimal placeholder screen used to reserve the tab and demonstrate layout.
 */

package me.notanoticed.pdfmanager.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.notanoticed.pdfmanager.BuildConfig
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- SCREEN -------------------- */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            CardBlock(text = "APP PREFERENCES") {
                SwitchRow(
                    icon = Icons.Outlined.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Use dark theme throughout the app",
                    checked = true
                )

            }
        }

        item {
            CardBlock(text = "SECURITY & PRIVACY") {
                SwitchRow(
                    icon = Icons.Outlined.Notifications,
                    title = "Notifications",
                    subtitle = "Receive notifications from the app"
                )
            }
        }

        item {
            CardBlock(text = "SUPPORT & FEEDBACK") {
                ArrowRow(
                    icon = Icons.Outlined.StarOutline,
                    title = "Rate App",
                    subtitle = "Rate PDF Manager on the Google Play"
                    // TODO: add rate logic
                )
                HorizontalDivider(
                    color = Colors.Border.default,
                    thickness = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                )
                ArrowRow(
                    icon = Icons.Outlined.Share,
                    title = "Share App",
                    subtitle = "Tell others about PDF Manager"
                    // TODO: add share logic
                )
                HorizontalDivider(
                    color = Colors.Border.default,
                    thickness = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                )
                ArrowRow(
                    icon = Icons.Outlined.Info,
                    title = "About",
                    subtitle = "Version info and app details",
                    // TODO: add about logic
                )
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Colors.Surface.card,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box (
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Colors.Primary.blue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            tint = Colors.Icon.white,
                            modifier = Modifier.fillMaxSize(0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "PDF Manager", color = Colors.Text.primary, fontWeight = FontWeight.SemiBold)
                    Text(text = "Version ${BuildConfig.VERSION_NAME}", color = Colors.Text.secondary, fontSize = 12.sp)
                    Text(text = "Your complete PDF toolkit", color = Colors.Text.secondary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        color = Colors.Text.secondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .padding(start = 16.dp)

    )
}

@Composable
fun CardBlock(text: String, content: @Composable ColumnScope.() -> Unit = {}) {
    SectionHeader(text = text)
    Surface(
        color = Colors.Surface.card,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            content = content
        )
    }
}

@Composable
fun SwitchRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean = false) {
    var checked by remember {
        mutableStateOf(checked) // TODO: get from settings
    }

    ListItem(
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Colors.Surface.thumbnail)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Colors.Icon.white,
                    modifier = Modifier
                        .fillMaxSize(0.65f)
                        .align(Alignment.Center)
                )
            }
        },
        headlineContent = {
            Text(text = title, color = Colors.Text.primary, fontWeight = FontWeight.SemiBold) }
        ,
        supportingContent = {
            Text(text = subtitle, color = Colors.Text.secondary, fontSize = 12.sp)
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = { checked = it  /* TODO: save to settings */ },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Colors.Primary.white,
                    checkedTrackColor = Colors.Primary.blue,
                    uncheckedThumbColor = Colors.Primary.white,
                    uncheckedTrackColor = Colors.Primary.slateGray,
                    uncheckedBorderColor = Colors.Border.gray
                ),
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Colors.Surface.card
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
fun ArrowRow(icon: ImageVector, title: String, subtitle: String) {
    ListItem(
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Colors.Surface.thumbnail)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Colors.Icon.white,
                    modifier = Modifier
                        .fillMaxSize(0.65f)
                        .align(Alignment.Center)
                )
            }
        },
        headlineContent = {
            Text(text = title, color = Colors.Text.primary, fontWeight = FontWeight.SemiBold) }
        ,
        supportingContent = {
            Text(text = subtitle, color = Colors.Text.secondary, fontSize = 12.sp)
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Colors.Text.secondary
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Colors.Surface.card
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}
/* ------------------------------------------------ */