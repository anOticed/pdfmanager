/**
 * Settings ViewModel.
 *
 * Handles persistent application settings
 */

package me.notanoticed.pdfmanager.feature.settings

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.core.content.edit

private const val SETTINGS_PREFS = "app_settings"
private const val DARK_MODE_KEY = "dark_mode_enabled"

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)

    var isDarkModeEnabled by mutableStateOf(
        prefs.getBoolean(DARK_MODE_KEY, true)
    )
        private set

    fun updateDarkModeEnabled(enabled: Boolean) {
        if (isDarkModeEnabled == enabled) return

        isDarkModeEnabled = enabled
        prefs.edit {
            putBoolean(DARK_MODE_KEY, enabled)
        }
    }
}
