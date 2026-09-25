package com.kjbilling.app.data.backup

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/** Opens the share sheet for a backup zip (Drive, WhatsApp, Files, USB...). Used by Settings and the Dashboard tile. */
object BackupShare {

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Save backup to...").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
