package me.notanoticed.pdfmanager.feature.settings

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import me.notanoticed.pdfmanager.BuildConfig
import androidx.compose.ui.res.stringResource
import me.notanoticed.pdfmanager.R
import me.notanoticed.pdfmanager.ui.theme.Colors

/* -------------------- SCREEN -------------------- */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel
) {
    var languageDialogVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            CardBlock(text = stringResource(R.string.settings_section_app_preferences)) {
                SwitchRow(
                    icon = Icons.Outlined.DarkMode,
                    title = stringResource(R.string.settings_dark_mode_title),
                    subtitle = stringResource(R.string.settings_dark_mode_subtitle),
                    checked = viewModel.isDarkModeEnabled,
                    onCheckedChange = viewModel::updateDarkModeEnabled
                )
                SettingsDivider()
                LanguageRow(
                    selectedLanguage = viewModel.selectedLanguage,
                    onClick = { languageDialogVisible = true }
                )
            }
        }

        item {
            CardBlock(text = stringResource(R.string.settings_section_more)) {
                ActionRow(
                    icon = Icons.Outlined.LocalOffer,
                    title = stringResource(R.string.settings_license_title),
                    subtitle = stringResource(R.string.settings_license_subtitle),
                    onClick = viewModel::openLicense
                )
                SettingsDivider()
                ActionRow(
                    icon = Icons.Outlined.OpenInNew,
                    title = stringResource(R.string.settings_github_title),
                    subtitle = PROJECT_REPOSITORY_URL,
                    onClick = viewModel::openRepository
                )
                SettingsDivider()
                ActionRow(
                    icon = Icons.Outlined.Share,
                    title = stringResource(R.string.settings_share_app_title),
                    subtitle = stringResource(R.string.settings_share_app_subtitle),
                    onClick = viewModel::shareApp
                )
            }
        }

        item {
            AppInfoCard()
        }
    }

    LanguageDialog(
        visible = languageDialogVisible,
        selectedLanguage = viewModel.selectedLanguage,
        onDismiss = { languageDialogVisible = false },
        onLanguageClick = { language ->
            viewModel.updateLanguage(language)
            languageDialogVisible = false
        }
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = Colors.Text.secondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp)
    )
}

@Composable
private fun CardBlock(
    text: String,
    content: @Composable ColumnScope.() -> Unit
) {
    SectionHeader(text = text)
    Surface(
        color = Colors.Surface.card,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = Colors.Border.default,
        thickness = 0.5.dp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    BaseSettingsRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Colors.Primary.white,
                    checkedTrackColor = Colors.Primary.blue,
                    uncheckedThumbColor = Colors.Primary.white,
                    uncheckedTrackColor = Colors.Primary.slateGray,
                    uncheckedBorderColor = Colors.Border.gray
                )
            )
        }
    )
}

@Composable
private fun LanguageRow(
    selectedLanguage: AppLanguage,
    onClick: () -> Unit
) {
    BaseSettingsRow(
        icon = Icons.Outlined.Language,
        title = stringResource(R.string.settings_language_title),
        subtitle = stringResource(R.string.settings_language_subtitle),
        clickable = true,
        onClick = onClick,
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(selectedLanguage.labelRes),
                    color = Colors.Text.blue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = Colors.Text.secondary
                )
            }
        }
    )
}

@Composable
private fun AppInfoCard() {
    val context = LocalContext.current
    val appIcon = remember(context) {
        runCatching {
            context.packageManager.getApplicationIcon(context.packageName)
        }.getOrNull()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Colors.Surface.card,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (appIcon != null) {
                AndroidView(
                    factory = { viewContext ->
                        ImageView(viewContext).apply {
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            setImageDrawable(appIcon)
                        }
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.app_name),
                color = Colors.Text.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(
                    R.string.settings_version_format,
                    BuildConfig.VERSION_NAME
                ),
                color = Colors.Text.secondary,
                fontSize = 12.sp
            )
            Text(
                text = stringResource(R.string.settings_app_info_tagline),
                color = Colors.Text.secondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun LanguageDialog(
    visible: Boolean,
    selectedLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onLanguageClick: (AppLanguage) -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Colors.Surface.card,
        title = {
            Text(
                text = stringResource(R.string.settings_language_dialog_title),
                color = Colors.Text.primary,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                AppLanguage.options.forEach { language ->
                    LanguageOptionRow(
                        language = language,
                        selected = language == selectedLanguage,
                        onClick = { onLanguageClick(language) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = Colors.Primary.blue
                )
            }
        }
    )
}

@Composable
private fun LanguageOptionRow(
    language: AppLanguage,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Colors.Primary.blue,
                unselectedColor = Colors.Icon.default
            )
        )
        Text(
            text = stringResource(language.labelRes),
            color = Colors.Text.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    BaseSettingsRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        clickable = true,
        onClick = onClick,
        trailingContent = {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Colors.Text.secondary
            )
        }
    )
}

@Composable
private fun BaseSettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    clickable: Boolean = false,
    onClick: () -> Unit = {},
    trailingContent: @Composable (() -> Unit)? = null
) {
    val rowModifier = if (clickable) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    }

    ListItem(
        leadingContent = {
            SettingsIcon(icon = icon)
        },
        headlineContent = {
            Text(
                text = title,
                color = Colors.Text.primary,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Text(
                text = subtitle,
                color = Colors.Text.secondary,
                fontSize = 12.sp
            )
        },
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = Colors.Surface.card
        ),
        modifier = rowModifier
    )
}

@Composable
private fun SettingsIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Colors.Surface.thumbnail)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Colors.Icon.default,
            modifier = Modifier
                .fillMaxSize(0.65f)
                .align(Alignment.Center)
        )
    }
}
/* ------------------------------------------------ */
