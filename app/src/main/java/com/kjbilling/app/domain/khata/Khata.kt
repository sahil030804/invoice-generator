package com.kjbilling.app.domain.khata

import com.kjbilling.app.domain.formatter.CurrencyFormatter
import com.kjbilling.app.domain.model.Customer
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.payment.PaymentRules
import com.kjbilling.app.domain.payment.PaymentUpdate
import java.math.BigDecimal

data class Allocation(val invoiceId: Long, val applied: BigDecimal, val update: PaymentUpdate)

data class AllocationResult(val allocations: List<Allocation>, val unapplied: BigDecimal)

data class CustomerDue(
    val customerId: Long,
    val name: String,
    val mobile: String?,
    val due: BigDecimal,
    val openBills: Int,
    val lastBillDate: Long
)

data class KhataSummary(
    val customers: List<CustomerDue>,
    /** Open bills with no real customer (walk-in or none). */
    val unlinked: List<Invoice>,
    val total: BigDecimal
)

/** Khata balances are derived from invoices: nothing extra is stored. */
object KhataCalculator {

    fun isOpen(invoice: Invoice): Boolean =
        invoice.status != InvoiceStatus.CANCELLED && invoice.balanceDue.signum() > 0

    fun openInvoices(invoices: List<Invoice>): List<Invoice> = invoices.filter(::isOpen)

    fun dueFor(customerId: Long, invoices: List<Invoice>): BigDecimal =
        openInvoices(invoices).filter { it.customerId == customerId }.sumDue()

    /** Per-customer dues (highest first) plus bills that can't be tied to a customer. Total = Dashboard Pending. */
    fun summarize(invoices: List<Invoice>, customers: List<Customer>): KhataSummary {
        val byId = customers.associateBy { it.id }
        val open = openInvoices(invoices)
        val (linked, unlinked) = open.partition { invoice ->
            val id = invoice.customerId
            id != null && id > 0 && byId[id]?.isWalkIn != true
        }

        val dues = linked.groupBy { it.customerId!! }.map { (id, bills) ->
            val customer = byId[id]
            CustomerDue(
                customerId = id,
                // A deleted customer still owes money; show the name printed on their bills.
                name = customer?.name ?: bills.maxBy { it.invoiceDate }.customerName,
                mobile = customer?.mobile,
                due = bills.sumDue(),
                openBills = bills.size,
                lastBillDate = bills.maxOf { it.invoiceDate }
            )
        }.sortedWith(compareByDescending<CustomerDue> { it.due }.thenBy { it.name })

        return KhataSummary(dues, unlinked.sortedBy { it.invoiceDate }, open.sumDue())
    }

    private fun List<Invoice>.sumDue(): BigDecimal = fold(BigDecimal.ZERO) { acc, invoice -> acc + invoice.balanceDue }
}

/** Spreads money received from a customer across their open bills, oldest first. */
object KhataAllocator {

    fun allocate(invoices: List<Invoice>, amount: BigDecimal): AllocationResult {
        var remaining = amount
        val allocations = mutableListOf<Allocation>()
        val oldestFirst = KhataCalculator.openInvoices(invoices)
            .sortedWith(compareBy<Invoice>({ it.invoiceDate }, { it.createdAt }, { it.id }))

        for (invoice in oldestFirst) {
            if (remaining.signum() <= 0) {
                break
            }
            val applied = remaining.min(invoice.balanceDue)
            val update = PaymentRules.apply(invoice, applied) ?: continue
            allocations.add(Allocation(invoice.id, applied, update))
            remaining -= applied
        }
        return AllocationResult(allocations, remaining.max(BigDecimal.ZERO))
    }
}

/** Indian mobile → WhatsApp international format ("919876543210"), or null if not a valid mobile. */
object PhoneNumbers {

    private const val COUNTRY_CODE = "91"
    private const val MOBILE_LENGTH = 10
    private val MOBILE_START = '6'..'9'

    fun toWhatsApp(mobile: String?): String? {
        val digits = mobile?.filter { it.isDigit() } ?: return null
        val national = when {
            digits.length == MOBILE_LENGTH -> digits
            digits.length == MOBILE_LENGTH + 1 && digits.startsWith("0") -> digits.drop(1)
            digits.length == MOBILE_LENGTH + 2 && digits.startsWith(COUNTRY_CODE) -> digits.drop(2)
            else -> return null
        }
        return if (national.first() in MOBILE_START) COUNTRY_CODE + national else null
    }
}

/** Polite payment reminder, ready for WhatsApp or SMS. */
object ReminderMessage {

    fun build(customerName: String, due: BigDecimal, bills: Int, businessName: String, upiId: String?): String {
        val billWord = if (bills == 1) "bill" else "bills"
        val pay = upiId?.let { " You can pay by UPI: $it." } ?: ""
        return "Namaste $customerName ji, ${CurrencyFormatter.format(due)} is pending at $businessName " +
            "($bills $billWord).$pay Thank you!"
    }
}
