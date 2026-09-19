package com.kjbilling.app.domain.model

import java.math.BigDecimal

data class InvoiceItem(
    val id: Long = 0,
    val invoiceId: Long = 0,
    val productId: Long? = null,
    val itemName: String,
    val description: String? = null,
    val hsnCode: String? = null,
    val quantity: BigDecimal,
    val unit: String? = "PCS",
    val unitPrice: BigDecimal,
    val discountPercent: BigDecimal = BigDecimal.ZERO,
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    val gstRate: BigDecimal,
    val taxableAmount: BigDecimal = BigDecimal.ZERO,
    val cgstAmount: BigDecimal? = null,
    val sgstAmount: BigDecimal? = null,
    val igstAmount: BigDecimal? = null,
    val taxAmount: BigDecimal = BigDecimal.ZERO,
    val total: BigDecimal = BigDecimal.ZERO,
    val sortOrder: Int = 0
)
