package com.kjbilling.app

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kjbilling.app.data.backup.BackupManifest
import com.kjbilling.app.data.storage.LogoStorage
import com.kjbilling.app.data.backup.BackupManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.zip.ZipFile

/**
 * Creates a real backup from the (isolated e2e) app database and checks the zip.
 * Restore is not run in-process: it swaps the DB under the running app.
 */
@RunWith(AndroidJUnit4::class)
class BackupCreateE2eTest {

    @Test
    fun backupZipHasManifestAndConsistentDatabase() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.content.Context>()
        // make sure the database file exists
        com.kjbilling.app.data.db.AppDatabase.getInstance(app).openHelper.writableDatabase

        val manager = BackupManager(app, LogoStorage(app))
        val zipFile = manager.createBackup().getOrThrow()

        assertTrue(zipFile.name.startsWith("Parchi-backup-"))
        ZipFile(zipFile).use { zip ->
            val names = zip.entries().toList().map { it.name }
            assertTrue(names.contains("manifest.properties"))
            assertTrue(names.contains("kj_invoice_database"))

            val manifest = BackupManifest.parse(
                zip.getInputStream(zip.getEntry("manifest.properties")).bufferedReader().readText()
            ).getOrThrow()
            assertEquals(com.kjbilling.app.data.db.AppDatabase.DB_VERSION, manifest.dbVersion)
            assertTrue(manager.lastBackupAt() != null)
        }
    }
}
