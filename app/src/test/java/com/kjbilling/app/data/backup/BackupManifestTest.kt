package com.kjbilling.app.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupManifestTest {

    private val manifest = BackupManifest(
        formatVersion = BackupManifest.FORMAT_VERSION,
        dbVersion = 2,
        appVersion = "1.0.0",
        createdAt = 1_790_000_000_000L,
        hasLogo = true
    )

    @Test
    fun roundTrip_keepsAllFields() {
        val parsed = BackupManifest.parse(manifest.serialize())

        assertEquals(manifest, parsed.getOrThrow())
    }

    @Test
    fun parse_garbage_fails() {
        assertTrue(BackupManifest.parse("not a manifest").isFailure)
        assertTrue(BackupManifest.parse("").isFailure)
    }

    @Test
    fun parse_missingRequiredField_fails() {
        assertTrue(BackupManifest.parse("formatVersion=1\ndbVersion=2").isFailure)
    }

    @Test
    fun restoreCheck_sameOrOlderDb_isAllowed() {
        assertNull(manifest.restoreError(currentDbVersion = 2))
        assertNull(manifest.copy(dbVersion = 1).restoreError(currentDbVersion = 2))
    }

    @Test
    fun restoreCheck_newerDb_isRejected() {
        val error = manifest.copy(dbVersion = 3).restoreError(currentDbVersion = 2)

        assertNotNull(error)
        assertTrue(error!!.contains("newer"))
    }

    @Test
    fun restoreCheck_unknownFormat_isRejected() {
        val error = manifest.copy(formatVersion = 99).restoreError(currentDbVersion = 2)

        assertNotNull(error)
    }
}
