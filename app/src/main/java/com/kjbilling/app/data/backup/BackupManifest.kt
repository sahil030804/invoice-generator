package com.kjbilling.app.data.backup

import java.io.StringReader
import java.util.Properties

/**
 * Describes a backup zip. Stored as manifest.properties (plain key=value, no Android deps).
 */
data class BackupManifest(
    val formatVersion: Int,
    val dbVersion: Int,
    val appVersion: String,
    val createdAt: Long,
    val hasLogo: Boolean
) {
    fun serialize(): String {
        return listOf(
            "formatVersion=$formatVersion",
            "dbVersion=$dbVersion",
            "appVersion=$appVersion",
            "createdAt=$createdAt",
            "hasLogo=$hasLogo"
        ).joinToString("\n")
    }

    /** Null when this backup can be restored by an app whose DB is [currentDbVersion]; else the reason. */
    fun restoreError(currentDbVersion: Int): String? {
        if (formatVersion != FORMAT_VERSION) {
            return "Unrecognised backup format."
        }
        if (dbVersion > currentDbVersion) {
            return "This backup was made by a newer version of the app. Update the app first."
        }
        return null
    }

    companion object {
        const val FORMAT_VERSION = 1

        fun parse(text: String): Result<BackupManifest> = runCatching {
            val props = Properties().apply { load(StringReader(text)) }
            fun required(key: String) = props.getProperty(key)?.trim()
                ?: throw IllegalArgumentException("Missing $key")

            BackupManifest(
                formatVersion = required("formatVersion").toInt(),
                dbVersion = required("dbVersion").toInt(),
                appVersion = required("appVersion"),
                createdAt = required("createdAt").toLong(),
                hasLogo = required("hasLogo").toBooleanStrict()
            )
        }
    }
}
