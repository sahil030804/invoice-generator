package com.kjbilling.app.domain

import com.kjbilling.app.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {

    @Test
    fun fromStorage_knownNames_roundTrip() {
        ThemeMode.values().forEach { assertEquals(it, ThemeMode.fromStorage(it.name)) }
    }

    @Test
    fun fromStorage_missingOrUnknown_defaultsToSystem() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage(""))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage("PURPLE"))
    }

    @Test
    fun isDark_followsSystemOnlyForSystemMode() {
        assertTrue(ThemeMode.SYSTEM.isDark(systemDark = true))
        assertFalse(ThemeMode.SYSTEM.isDark(systemDark = false))
        assertFalse(ThemeMode.LIGHT.isDark(systemDark = true))
        assertTrue(ThemeMode.DARK.isDark(systemDark = false))
    }
}
