package com.kjbilling.app.domain.model

import java.math.BigDecimal

/**
 * Tax breakdown grouped by GST rate for the tax summary table on invoices.
 */
data class TaxBreakdown(
    val gstRate: BigDecimal,
    val taxableAmount: BigDecimal,
    val cgstRate: BigDecimal = BigDecimal.ZERO,
    val cgstAmount: BigDecimal = BigDecimal.ZERO,
    val sgstRate: BigDecimal = BigDecimal.ZERO,
    val sgstAmount: BigDecimal = BigDecimal.ZERO,
    val igstRate: BigDecimal = BigDecimal.ZERO,
    val igstAmount: BigDecimal = BigDecimal.ZERO,
    val totalTax: BigDecimal
)

data class AppSettings(
    val id: Long = 1,
    val gstEnabled: Boolean = true,
    val defaultGstRate: BigDecimal = BigDecimal("18"),
    val defaultTaxType: TaxType = TaxType.CGST_SGST,
    val invoicePrefix: String = "INV-",
    val nextInvoiceNumber: Long = 1,
    val onboardingCompleted: Boolean = false,
    val defaultPaymentStatus: PaymentStatus = PaymentStatus.UNPAID
)
