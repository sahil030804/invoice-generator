package com.kjbilling.app.domain.upi

import com.kjbilling.app.domain.formatter.CurrencyFormatter
import com.kjbilling.app.domain.model.BusinessProfile
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceStatus
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URLEncoder

/** Everything needed to show one "Scan to pay" QR. */
data class UpiQrRequest(
    val uri: String,
    val amount: BigDecimal,
    val amountLabel: String,
    val vpa: String,
    val payeeName: String
)

/**
 * Builds standard UPI deep links (NPCI "upi://pay") that every UPI app can scan. Pure, offline.
 *
 *   upi://pay?pa=shop@okaxis&pn=KJ%20Plastic&am=118.00&cu=INR&tn=INV-0008
 */
object UpiPayment {

    private const val CURRENCY = "INR"
    private val VPA_REGEX = Regex("^[a-z0-9][a-z0-9._-]{1,255}@[a-z][a-z0-9]{1,63}$")

    /** Trimmed, lower-case UPI ID, or null when blank. */
    fun normalizeVpa(raw: String?): String? {
        return raw?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
    }

    /** name@handle. Rejects e-mail look-alikes such as a@gmail.com (handles never contain dots). */
    fun isValidVpa(vpa: String?): Boolean {
        return vpa != null && VPA_REGEX.matches(vpa)
    }

    fun formatAmount(amount: BigDecimal): String {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString()
    }

    fun buildUri(vpa: String, payeeName: String, amount: BigDecimal, note: String?): String {
        val params = buildList {
            add("pa=$vpa")
            add("pn=${encode(payeeName)}")
            add("am=${formatAmount(amount)}")
            add("cu=$CURRENCY")
            note?.takeIf { it.isNotBlank() }?.let { add("tn=${encode(it.trim())}") }
        }
        return "upi://pay?" + params.joinToString("&")
    }

    /**
     * QR for an invoice's outstanding balance. Uses the live profile's UPI ID so money always goes
     * to the shop's current account. Null when nothing is payable or no valid UPI ID is set.
     */
    fun forInvoice(invoice: Invoice, profile: BusinessProfile?): UpiQrRequest? {
        if (invoice.status == InvoiceStatus.CANCELLED) {
            return null
        }
        return forAmount(invoice.balanceDue, profile, note = invoice.invoiceNumber)
    }

    /** QR for any amount (standalone "UPI QR" screen and cash-counter use). */
    fun forAmount(amount: BigDecimal, profile: BusinessProfile?, note: String? = null): UpiQrRequest? {
        if (profile == null || amount <= BigDecimal.ZERO) {
            return null
        }
        val vpa = normalizeVpa(profile.upiId)?.takeIf { isValidVpa(it) } ?: return null
        val payee = profile.businessName.ifBlank { vpa }
        return UpiQrRequest(
            uri = buildUri(vpa, payee, amount, note),
            amount = amount.setScale(2, RoundingMode.HALF_UP),
            amountLabel = CurrencyFormatter.format(amount),
            vpa = vpa,
            payeeName = payee
        )
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")
    }
}
