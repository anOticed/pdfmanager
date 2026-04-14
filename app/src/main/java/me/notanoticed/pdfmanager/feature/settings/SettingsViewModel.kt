/**
 * Settings ViewModel.
 *
 * Handles persistent application settings
 */

package me.notanoticed.pdfmanager.feature.settings

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel

private const val SETTINGS_PREFS = "app_settings"
private const val DARK_MODE_KEY = "dark_mode_enabled"
private const val LANGUAGE_CODE_KEY = "language_code"

const val PROJECT_REPOSITORY_URL = "https://github.com/anOticed/pdfmanager"
const val PROJECT_LICENSE_URL = "https://github.com/anOticed/pdfmanager/blob/main/LICENSE"

enum class AppLanguage(
    val code: String,
    val displayName: String
) {
    ENGLISH("en", "English"),
    FRENCH("fr", "French"),
    GERMAN("de", "German"),
    JAPANESE("ja", "Japanese"),
    MANDARIN("zh", "Mandarin"),
    SLOVAK("sk", "Slovak"),
    SPANISH("es", "Spanish"),
    UKRAINIAN("uk", "Ukrainian");

    companion object {
        val options: List<AppLanguage> = entries.sortedBy { it.displayName }

        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code == code } ?: ENGLISH
        }
    }
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)

    var isDarkModeEnabled by mutableStateOf(
        prefs.getBoolean(DARK_MODE_KEY, true)
    )
        private set

    var selectedLanguage by mutableStateOf(
        AppLanguage.fromCode(prefs.getString(LANGUAGE_CODE_KEY, AppLanguage.ENGLISH.code))
    )
        private set

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
    }

    fun openRepository() {
        openUrl(PROJECT_REPOSITORY_URL)
    }

    fun openLicense() {
        openUrl(PROJECT_LICENSE_URL)
    }

    fun shareApp() {
        val shareText = "PDF Manager for Android\n$PROJECT_REPOSITORY_URL"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        launchExternalIntent(
            Intent.createChooser(shareIntent, "Share PDF Manager")
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
}
