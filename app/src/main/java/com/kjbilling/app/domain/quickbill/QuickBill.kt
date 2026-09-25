package com.kjbilling.app.domain.quickbill

import com.kjbilling.app.domain.model.Customer
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentMethod
import com.kjbilling.app.domain.model.PaymentStatus
import java.math.BigDecimal

/** How a counter bill is settled. Udhaar = pay later, recorded in the customer's Khata. */
enum class QuickPaymentMode { CASH, UPI, UDHAAR }

/** A keypad-entered item that isn't in the catalog. [amount] already includes GST. */
data class CustomCartItem(val id: Long, val name: String, val amount: BigDecimal)

data class QuickPaymentFields(
    val status: InvoiceStatus,
    val paymentStatus: PaymentStatus,
    val method: PaymentMethod?,
    val amountPaid: BigDecimal
)

/** Pure Quick Bill rules, shared by the ViewModel and tests. */
object QuickBill {

    const val DEFAULT_CUSTOM_NAME = "Misc item"

    /** Udhaar must be tied to a real customer so it shows up in their Khata. */
    fun canUseUdhaar(customer: Customer?): Boolean = customer != null && !customer.isWalkIn

    /**
     * Items that will actually be billed: quantities of products that still exist plus custom amounts.
     * A product deleted while it sits in the cart is ignored, so an all-ghost cart can't produce a ₹0 bill.
     */
    fun billableCount(quantities: Map<Long, Int>, customItemCount: Int, existingProductIds: Set<Long>): Int {
        val productUnits = quantities
            .filter { (id, qty) -> qty > 0 && id in existingProductIds }
            .values
            .sum()
        return productUnits + customItemCount
    }

    fun canGenerate(itemCount: Int, mode: QuickPaymentMode, customer: Customer?): Boolean {
        if (itemCount <= 0) {
            return false
        }
        return mode != QuickPaymentMode.UDHAAR || canUseUdhaar(customer)
    }

    fun paymentFields(mode: QuickPaymentMode, total: BigDecimal): QuickPaymentFields = when (mode) {
        QuickPaymentMode.CASH -> QuickPaymentFields(InvoiceStatus.PAID, PaymentStatus.PAID, PaymentMethod.CASH, total)
        QuickPaymentMode.UPI -> QuickPaymentFields(InvoiceStatus.PAID, PaymentStatus.PAID, PaymentMethod.UPI, total)
        QuickPaymentMode.UDHAAR -> QuickPaymentFields(InvoiceStatus.GENERATED, PaymentStatus.UNPAID, null, BigDecimal.ZERO)
    }

    /** Shown next to the total on the success screen, e.g. "₹118.00 (Paid in Cash)". */
    fun successLabel(invoice: Invoice): String = when {
        invoice.paymentStatus != PaymentStatus.PAID -> "(Udhaar - added to Khata)"
        invoice.paymentMethod == PaymentMethod.UPI -> "(Paid by UPI)"
        else -> "(Paid in Cash)"
    }

    fun customItemName(raw: String): String = raw.trim().ifEmpty { DEFAULT_CUSTOM_NAME }
}
