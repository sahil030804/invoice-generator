package com.kjbilling.app.pdf

import com.kjbilling.app.domain.formatter.CurrencyFormatter
import com.kjbilling.app.domain.formatter.label
import com.kjbilling.app.domain.model.PaymentMethod
import com.kjbilling.app.domain.model.PaymentStatus
import java.math.BigDecimal

/** Payment card text on the PDF. [lines] are drawn one per row (QR layout) or joined (compact). */
data class PaymentSummary(val title: String, val lines: List<String>) {

    fun singleLine(): String = lines.joinToString(SEPARATOR)

    companion object {
        private const val SEPARATOR = "  ·  "

        fun build(
            status: PaymentStatus,
            method: PaymentMethod?,
            amountPaid: BigDecimal,
            balanceDue: BigDecimal
        ): PaymentSummary {
            val title = if (status == PaymentStatus.PAID) "Payment Status: Paid" else "Payment Details"
            val statusLabel = when (status) {
                PaymentStatus.PAID -> "Paid"
                PaymentStatus.PARTIAL -> "Partly paid"
                PaymentStatus.UNPAID -> "Pending"
            }
            val methodSuffix = method?.takeIf { status != PaymentStatus.UNPAID }?.let { " (${it.label()})" } ?: ""

            val lines = buildList {
                add("Status: $statusLabel$methodSuffix")
                if (status == PaymentStatus.PARTIAL) {
                    add("Paid: ${CurrencyFormatter.format(amountPaid)}")
                }
                if (status != PaymentStatus.PAID) {
                    add("Balance: ${CurrencyFormatter.format(balanceDue)}")
                }
            }
            return PaymentSummary(title, lines)
        }
    }
}
