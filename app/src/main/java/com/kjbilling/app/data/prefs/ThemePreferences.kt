package com.kjbilling.app.data.prefs

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import com.kjbilling.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists the Appearance choice in its own SharedPreferences file.
 *
 * Not stored in Room on purpose: no DB migration, and restoring a backup never changes it.
 * Read synchronously on creation so the very first frame already uses the right theme.
 */
class ThemePreferences(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(ThemeMode.fromStorage(prefs.getString(KEY_MODE, null)))
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun setMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
        _mode.value = mode
        applyToSystem(mode)
    }

    /** Re-applies the stored mode to the OS if they drifted apart (e.g. after "clear data"). */
    fun syncOnStartup() {
        val mode = _mode.value
        if (prefs.getString(KEY_SYNCED, null) != mode.name) {
            applyToSystem(mode)
        }
    }

    /**
     * API 31+: tells Android the app's night mode so the launch splash and window background
     * already match the in-app choice. Older versions simply follow the phone setting there.
     */
    private fun applyToSystem(mode: ThemeMode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return
        }

        val uiModeManager = appContext.getSystemService(UiModeManager::class.java) ?: return
        val nightMode = when (mode) {
            ThemeMode.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
            ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
            ThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
        }
        runCatching { uiModeManager.setApplicationNightMode(nightMode) }
            .onSuccess { prefs.edit().putString(KEY_SYNCED, mode.name).apply() }
    }

    private companion object {
        const val PREFS = "appearance_prefs"
        const val KEY_MODE = "theme_mode"
        const val KEY_SYNCED = "synced_night_mode"
    }
}
