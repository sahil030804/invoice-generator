package com.kjbilling.app.domain.insights

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class BackupStatusTest {

    private val ist = ZoneId.of("Asia/Kolkata")
    private fun at(d: Int, h: Int = 12) = ZonedDateTime.of(2026, 9, d, h, 0, 0, 0, ist).toInstant().toEpochMilli()
    private val now = at(25, 15)

    @Test
    fun neverBackedUp_isStale_whenThereIsData() {
        val s = BackupStatus.describe(null, now, hasData = true, zone = ist)

        assertEquals("Not backed up yet", s.text)
        assertTrue(s.needsAttention)
    }

    @Test
    fun noData_needsNoBackup() {
        val s = BackupStatus.describe(null, now, hasData = false, zone = ist)

        assertEquals("Nothing to back up yet", s.text)
        assertFalse(s.needsAttention)
    }

    @Test
    fun recentBackup_isFine() {
        assertEquals("Backed up today", BackupStatus.describe(at(25, 9), now, true, ist).text)
        assertEquals("Backed up yesterday", BackupStatus.describe(at(24, 22), now, true, ist).text)
        val threeDays = BackupStatus.describe(at(22), now, true, ist)
        assertEquals("Backed up 3 days ago", threeDays.text)
        assertFalse(threeDays.needsAttention)
    }

    @Test
    fun olderThanAWeek_needsAttention() {
        val s = BackupStatus.describe(at(17), now, true, ist)

        assertEquals("Backed up 8 days ago", s.text)
        assertTrue(s.needsAttention)
        assertFalse(BackupStatus.describe(at(18), now, true, ist).needsAttention)
    }
}
