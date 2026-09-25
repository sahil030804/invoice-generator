package com.kjbilling.app.pdf

import android.content.Context
import com.kjbilling.app.data.storage.LogoStorage
import com.kjbilling.app.domain.model.BusinessProfile
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.TaxBreakdown
import com.kjbilling.app.pdf.template.ClassicTemplateRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class InvoicePdfGenerator(
    private val context: Context,
    private val logoStorage: LogoStorage
) {

    /**
     * Generate PDF file for the given invoice and business profile.
     * Returns the File on success.
     * Invoice must be saved to DB BEFORE calling this — PDF failure must not lose data.
     */
    suspend fun generate(
        invoice: Invoice,
        business: BusinessProfile,
        taxBreakdown: List<TaxBreakdown>
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val documentModel = InvoiceDocumentModel.fromInvoice(invoice, business, taxBreakdown)
            val renderer = ClassicTemplateRenderer()
            // Logo is optional: a missing/corrupt file falls back to the initials badge.
            val logo = logoStorage.loadBitmap()
            val pdfDocument = try {
                renderer.render(documentModel, logo)
            } finally {
                logo?.recycle()
            }
            try {
                val dir = File(context.filesDir, "invoices")
                if (!dir.exists()) {
                    dir.mkdirs()
                }

                val baseName = sanitizeFilename("${documentModel.invoiceNumber}-${documentModel.customerName}")
                // Avoid overwriting on regenerate: suffix with timestamp when collision.
                var file = File(dir, "$baseName.pdf")
                if (file.exists()) {
                    val stamp = System.currentTimeMillis()
                    file = File(dir, "${baseName.take(80)}-$stamp.pdf")
                }

                FileOutputStream(file).use { out ->
                    pdfDocument.writeTo(out)
                }

                file
            } finally {
                try {
                    pdfDocument.close()
                } catch (_: Exception) {
                    // Best effort close, write already attempted.
                }
            }
        }
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("""[/\\:*?"<>|]"""), "-")
            .replace(Regex("\\s+"), "-")
            .take(100)
    }
}
