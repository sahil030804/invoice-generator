package com.kjbilling.app.ui.khata

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kjbilling.app.domain.khata.PhoneNumbers

/** Opens WhatsApp (or WhatsApp Business) on the customer's chat with the reminder typed in; else a share sheet. */
object WhatsAppReminder {

    private val WHATSAPP_PACKAGES = listOf("com.whatsapp", "com.whatsapp.w4b")

    fun send(context: Context, mobile: String?, message: String) {
        val phone = PhoneNumbers.toWhatsApp(mobile)
        if (phone != null) {
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(message)}")
            for (pkg in WHATSAPP_PACKAGES) {
                val intent = Intent(Intent.ACTION_VIEW, uri).setPackage(pkg).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(intent)
                    return
                } catch (_: ActivityNotFoundException) {
                    // Try the next WhatsApp flavour, then fall back to the share sheet.
                }
            }
        }
        val share = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, message)
        context.startActivity(Intent.createChooser(share, "Send reminder").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
