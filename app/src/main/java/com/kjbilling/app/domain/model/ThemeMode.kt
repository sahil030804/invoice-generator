package com.kjbilling.app.domain.model

/** App colour mode chosen in Settings → Appearance. SYSTEM follows the phone's dark-mode setting. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    fun isDark(systemDark: Boolean): Boolean {
        return when (this) {
            SYSTEM -> systemDark
            LIGHT -> false
            DARK -> true
        }
    }

    companion object {
        /** Unknown or missing values fall back to SYSTEM, so a bad pref can never break the theme. */
        fun fromStorage(value: String?): ThemeMode {
            return values().firstOrNull { it.name == value } ?: SYSTEM
        }
    }
}
