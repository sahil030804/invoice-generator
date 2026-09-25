package com.kjbilling.app.data.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.kjbilling.app.data.db.AppDatabase
import com.kjbilling.app.data.storage.LogoStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Local backup and restore. No internet, no account.
 *
 *   backup zip = manifest.properties + kj_invoice_database (+ branding/logo.png)
 *
 * Restore never deletes: the current database files are MOVED to files/pre-restore-<time>/
 * so a wrong restore can always be undone by hand.
 */
class BackupManager(context: Context, private val logoStorage: LogoStorage) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Epoch millis of the last successful backup, or null if never. */
    fun lastBackupAt(): Long? = prefs.getLong(KEY_LAST_BACKUP, 0L).takeIf { it > 0L }

    /** Writes a backup zip into cache/backups (shareable via FileProvider) and returns it. */
    suspend fun createBackup(): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            checkpointDatabase()

            val dbFile = appContext.getDatabasePath(AppDatabase.DB_NAME)
            if (!dbFile.exists()) {
                throw IOException("Nothing to back up yet")
            }

            val now = System.currentTimeMillis()
            val dir = File(appContext.cacheDir, BACKUP_DIR).apply { mkdirs() }
            val stamp = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date(now))
            val target = File(dir, "Parchi-backup-$stamp.zip")
            val logo = logoStorage.logoFile()

            val manifest = BackupManifest(
                formatVersion = BackupManifest.FORMAT_VERSION,
                dbVersion = AppDatabase.DB_VERSION,
                appVersion = APP_VERSION,
                createdAt = now,
                hasLogo = logo != null
            )

            ZipOutputStream(FileOutputStream(target)).use { zip ->
                zip.putNextEntry(ZipEntry(ENTRY_MANIFEST))
                zip.write(manifest.serialize().toByteArray())
                zip.closeEntry()

                zip.putNextEntry(ZipEntry(ENTRY_DB))
                dbFile.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()

                if (logo != null) {
                    zip.putNextEntry(ZipEntry(ENTRY_LOGO))
                    logo.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }

            prefs.edit().putLong(KEY_LAST_BACKUP, now).apply()
            target
        }
    }

    /**
     * Validates the backup at [uri] and swaps it in. On success the caller MUST restart the app,
     * because the open database was closed.
     */
    suspend fun restore(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val work = File(appContext.cacheDir, "restore-${System.currentTimeMillis()}").apply { mkdirs() }
            val manifest = extractBackup(uri, work)

            manifest.restoreError(AppDatabase.DB_VERSION)?.let { throw IOException(it) }
            verifyDatabase(File(work, ENTRY_DB))

            // Safety net first: keep the current data reachable before touching anything.
            checkpointDatabase()
            val safety = File(appContext.filesDir, "pre-restore-${System.currentTimeMillis()}").apply { mkdirs() }
            AppDatabase.closeInstance()

            val dbFile = appContext.getDatabasePath(AppDatabase.DB_NAME)
            listOf(dbFile, File(dbFile.path + "-wal"), File(dbFile.path + "-shm"))
                .filter { it.exists() }
                .forEach { old ->
                    if (!old.renameTo(File(safety, old.name))) {
                        throw IOException("Could not set aside current data; nothing was changed")
                    }
                }

            File(work, ENTRY_DB).copyTo(dbFile, overwrite = false)

            val logoInBackup = File(work, ENTRY_LOGO)
            if (manifest.hasLogo && logoInBackup.exists()) {
                logoStorage.replaceLogo(logoInBackup)
            }
        }
    }

    /** Flushes the WAL into the main DB file so a plain file copy is complete. */
    private fun checkpointDatabase() {
        runCatching {
            AppDatabase.getInstance(appContext).openHelper.writableDatabase
                .query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
        }
    }

    private fun extractBackup(uri: Uri, into: File): BackupManifest {
        var manifestText: String? = null
        val wanted = setOf(ENTRY_MANIFEST, ENTRY_DB, ENTRY_LOGO)

        val stream = appContext.contentResolver.openInputStream(uri)
            ?: throw IOException("Cannot open the selected file")
        ZipInputStream(stream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                // Only known entry names are read, so a crafted zip cannot write elsewhere (zip-slip).
                if (entry.name in wanted) {
                    val out = File(into, entry.name.substringAfterLast('/'))
                    FileOutputStream(out).use { zip.copyTo(it) }
                    if (entry.name == ENTRY_MANIFEST) {
                        manifestText = out.readText()
                    }
                }
                entry = zip.nextEntry
            }
        }

        val text = manifestText ?: throw IOException("This is not a Parchi backup")
        if (!File(into, ENTRY_DB).exists()) {
            throw IOException("Backup has no invoice data")
        }
        return BackupManifest.parse(text).getOrElse { throw IOException("Backup manifest is damaged") }
    }

    private fun verifyDatabase(file: File) {
        val db = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
        try {
            val ok = db.rawQuery("PRAGMA integrity_check", null).use { it.moveToFirst() && it.getString(0) == "ok" }
            if (!ok) {
                throw IOException("Backup file is corrupted")
            }
            val hasInvoices = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='invoices'", null
            ).use { it.moveToFirst() }
            if (!hasInvoices) {
                throw IOException("Backup does not contain invoices")
            }
        } finally {
            db.close()
        }
    }

    companion object {
        const val BACKUP_DIR = "backups"
        const val APP_VERSION = "1.0.0"
        private const val PREFS = "backup_prefs"
        private const val KEY_LAST_BACKUP = "last_backup_at"
        private const val ENTRY_MANIFEST = "manifest.properties"
        private const val ENTRY_DB = "kj_invoice_database"
        private const val ENTRY_LOGO = "branding/logo.png"
    }
}
