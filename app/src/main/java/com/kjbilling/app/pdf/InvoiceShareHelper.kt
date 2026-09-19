package com.kjbilling.app.pdf

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object InvoiceShareHelper {
    fun shareFile(
        context: Context,
        file: File,
        invoiceNumber: String,
        customerName: String,
        grandTotal: String,
        targetPackage: String? = null
    ) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Invoice $invoiceNumber")
            putExtra(Intent.EXTRA_TEXT, "Hello $customerName,\n\nPlease find attached invoice $invoiceNumber for $grandTotal.\n\nThank you for your business.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (targetPackage != null) {
                setPackage(targetPackage)
            }
        }

        // createChooser discards setPackage, so launch the target directly and
        // fall back to a chooser only if the target app is not installed.
        if (targetPackage != null) {
            try {
                context.startActivity(intent)
                return
            } catch (_: ActivityNotFoundException) {
                intent.setPackage(null)
            }
        }

        context.startActivity(Intent.createChooser(intent, "Share Invoice"))
    }
}
