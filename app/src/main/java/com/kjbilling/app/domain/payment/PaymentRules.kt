package com.kjbilling.app.domain.payment

import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentStatus
import java.math.BigDecimal

/** New payment state of one invoice after money is received. */
data class PaymentUpdate(
    val amountPaid: BigDecimal,
    val paymentStatus: PaymentStatus,
    val status: InvoiceStatus
)

/**
 * The single source of truth for recording a payment against an invoice
 * (used by "Record Payment" on an invoice and by Khata "Received").
 */
object PaymentRules {

    /** Null when the payment is not allowed: cancelled invoice, amount ≤ 0, or more than the balance. */
    fun apply(invoice: Invoice, amount: BigDecimal): PaymentUpdate? {
        if (invoice.status == InvoiceStatus.CANCELLED) {
            return null
        }
        if (amount.signum() <= 0 || amount > invoice.balanceDue) {
            return null
        }

        // Payments accumulate; never exceed the grand total.
        val newAmountPaid = (invoice.amountPaid + amount).min(invoice.grandTotal)
        val paymentStatus = if (newAmountPaid >= invoice.grandTotal) PaymentStatus.PAID else PaymentStatus.PARTIAL
        val status = when {
            paymentStatus == PaymentStatus.PAID -> InvoiceStatus.PAID
            invoice.status == InvoiceStatus.PAID -> InvoiceStatus.GENERATED
            else -> invoice.status
        }
        return PaymentUpdate(newAmountPaid, paymentStatus, status)
    }
}
