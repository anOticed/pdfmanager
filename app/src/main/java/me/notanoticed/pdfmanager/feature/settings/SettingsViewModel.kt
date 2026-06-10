package me.notanoticed.pdfmanager.feature.settings

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import me.notanoticed.pdfmanager.R

private const val SETTINGS_PREFS = "app_settings"
private const val DARK_MODE_KEY = "dark_mode_enabled"
private const val LANGUAGE_CODE_KEY = "language_code"

const val PROJECT_REPOSITORY_URL = "https://github.com/anOticed/pdfmanager"
const val PROJECT_LICENSE_URL = "https://github.com/anOticed/pdfmanager/blob/main/LICENSE"

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)

    var isDarkModeEnabled by mutableStateOf(
        prefs.getBoolean(DARK_MODE_KEY, true)
    )
        private set

    var selectedLanguage by mutableStateOf(
        resolveSelectedLanguage()
    )
        private set

    init {
        applyLanguage(selectedLanguage)
    }

    fun updateDarkModeEnabled(enabled: Boolean) {
        if (isDarkModeEnabled == enabled) return

        isDarkModeEnabled = enabled
        prefs.edit {
            putBoolean(DARK_MODE_KEY, enabled)
        }
    }

    fun updateLanguage(language: AppLanguage) {
        if (selectedLanguage == language) return

        selectedLanguage = language
        prefs.edit {
            putString(LANGUAGE_CODE_KEY, language.code)
        }
        applyLanguage(language)
    }

    fun openRepository() {
        openUrl(PROJECT_REPOSITORY_URL)
    }

    fun openLicense() {
        openUrl(PROJECT_LICENSE_URL)
    }

    fun shareApp() {
        val app = getApplication<Application>()
        val shareText = app.getString(
            R.string.settings_share_app_text,
            PROJECT_REPOSITORY_URL
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        launchExternalIntent(
            Intent.createChooser(
                shareIntent,
                app.getString(R.string.settings_share_app_chooser_title)
            )
        )
    }

    private fun openUrl(url: String) {
        launchExternalIntent(
            Intent(Intent.ACTION_VIEW, url.toUri())
        )
    }

    private fun launchExternalIntent(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching {
            getApplication<Application>().startActivity(intent)
        }
    }

    private fun resolveSelectedLanguage(): AppLanguage {
        val savedCode = prefs.getString(LANGUAGE_CODE_KEY, null)
        if (savedCode != null) {
            return AppLanguage.fromCode(savedCode)
        }

        val systemLanguageTag = LocaleListCompat
            .getAdjustedDefault()
            .get(0)
            ?.toLanguageTag()

        return AppLanguage.fromLanguageTag(systemLanguageTag)
    }

    private fun applyLanguage(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language.code)
        )
    }
}
