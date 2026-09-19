package com.kjbilling.app.domain.model

import java.math.BigDecimal

data class Invoice(
    val id: Long = 0,
    val invoiceNumber: String,
    val invoiceDate: Long,
    val dueDate: Long? = null,
    val customerId: Long? = null,
    val customerName: String,
    val customerGstin: String? = null,
    val customerState: String? = null,
    val customerAddress: String? = null,
    val items: List<InvoiceItem> = emptyList(),
    val subtotal: BigDecimal = BigDecimal.ZERO,
    val totalDiscount: BigDecimal = BigDecimal.ZERO,
    val totalTax: BigDecimal = BigDecimal.ZERO,
    val grandTotal: BigDecimal = BigDecimal.ZERO,
    val taxType: TaxType = TaxType.CGST_SGST,
    val status: InvoiceStatus = InvoiceStatus.DRAFT,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val paymentMethod: PaymentMethod? = null,
    val amountPaid: BigDecimal = BigDecimal.ZERO,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val finalizedAt: Long? = null
) {
    val balanceDue: BigDecimal
        get() = grandTotal - amountPaid
}
