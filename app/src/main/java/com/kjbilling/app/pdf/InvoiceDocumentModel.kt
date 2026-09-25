package com.kjbilling.app.pdf

import com.kjbilling.app.domain.formatter.AmountInWords
import com.kjbilling.app.domain.formatter.CurrencyFormatter
import com.kjbilling.app.domain.model.BusinessProfile
import com.kjbilling.app.domain.model.BusinessSnapshot
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.TaxBreakdown
import com.kjbilling.app.domain.model.TaxType
import com.kjbilling.app.domain.upi.UpiPayment
import java.math.BigDecimal

data class InvoiceDocumentModel(
    // Business
    val businessName: String,
    val businessAddress: String,
    val businessPhone: String,
    val businessEmail: String?,
    val businessGstin: String?,
    val businessOwner: String?,
    // Invoice
    val invoiceTitle: String,
    val invoiceNumber: String,
    val invoiceDate: String,
    val dueDate: String?,
    // Customer
    val customerName: String,
    val customerAddress: String?,
    val customerGstin: String?,
    val customerState: String?,
    // Items
    val items: List<DocumentLineItem>,
    // HSN summary (HSN x Rate x Taxable x Tax)
    val hsnSummary: List<DocumentHsnRow>,
    // Totals
    val subtotal: String,
    val totalDiscount: String,
    val showDiscount: Boolean,
    val taxSummary: List<DocumentTaxRow>,
    val grandTotal: String,
    val amountInWords: String,
    // Tax type
    val taxType: TaxType,
    // Payment card text, and optional UPI "Scan to pay" QR (only when something is payable)
    val payment: PaymentSummary,
    val upiQr: DocumentUpiQr?,
    // Footer
    val notes: String?,
    val terms: String?,
    val signatureName: String?,
    val showTaxSummary: Boolean
) {
    companion object {
        private const val DECLARATION = "We declare that this invoice shows the actual price of the goods described and that all particulars are true and correct."

        private fun formatDate(timestamp: Long): String {
            val formatter = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            return formatter.format(java.util.Date(timestamp))
        }

        fun fromInvoice(
            invoice: Invoice,
            business: BusinessProfile,
            taxBreakdown: List<TaxBreakdown>
        ): InvoiceDocumentModel {
            val hasGst = invoice.items.any { it.gstRate > BigDecimal.ZERO }
            val title = if (hasGst) "Tax Invoice" else "Invoice"

            val documentItems = invoice.items.mapIndexed { index, item ->
                DocumentLineItem(
                    index = index + 1,
                    name = item.itemName,
                    hsnCode = item.hsnCode?.takeIf { it.isNotBlank() },
                    quantity = item.quantity.stripTrailingZeros().toPlainString(),
                    unit = item.unit?.takeIf { it.isNotBlank() },
                    rate = CurrencyFormatter.format(item.unitPrice),
                    discount = if (item.discountAmount > BigDecimal.ZERO) CurrencyFormatter.format(item.discountAmount) else null,
                    taxableAmount = CurrencyFormatter.format(item.taxableAmount),
                    gstRate = if (item.gstRate > BigDecimal.ZERO) "${item.gstRate.stripTrailingZeros().toPlainString()}%" else "0%",
                    total = CurrencyFormatter.format(item.total)
                )
            }

            val taxSummaryRows = mutableListOf<DocumentTaxRow>()
            val hsnRows = invoice.items
                .groupBy { Pair(it.hsnCode?.takeIf { code -> code.isNotBlank() } ?: "-", it.gstRate.stripTrailingZeros().toPlainString()) }
                .map { (key, group) ->
                    val taxable = group.fold(BigDecimal.ZERO) { acc, item -> acc + item.taxableAmount }
                    val tax = group.fold(BigDecimal.ZERO) { acc, item -> acc + item.taxAmount }
                    DocumentHsnRow(
                        hsnCode = key.first,
                        gstRate = "${key.second}%",
                        taxableAmount = CurrencyFormatter.format(taxable),
                        taxAmount = CurrencyFormatter.format(tax),
                        total = CurrencyFormatter.format(taxable + tax)
                    )
                }
                .sortedWith(compareBy({ it.hsnCode }, { it.gstRate }))
            if (invoice.taxType != TaxType.NO_GST) {
                taxBreakdown.forEach { tb ->
                    if (tb.totalTax > BigDecimal.ZERO) {
                        val rateLabel = tb.gstRate.stripTrailingZeros().toPlainString()
                        val taxableLabel = CurrencyFormatter.format(tb.taxableAmount)
                        if (invoice.taxType == TaxType.CGST_SGST) {
                            val halfRate = tb.gstRate.divide(BigDecimal("2"), 2, java.math.RoundingMode.HALF_UP)
                            val halfLabel = halfRate.stripTrailingZeros().toPlainString()
                            // Actual per-line split: halving the total is a paisa off when the tax is odd.
                            taxSummaryRows.add(DocumentTaxRow("CGST @ $halfLabel% on $taxableLabel", CurrencyFormatter.format(tb.cgstAmount)))
                            taxSummaryRows.add(DocumentTaxRow("SGST @ $halfLabel% on $taxableLabel", CurrencyFormatter.format(tb.sgstAmount)))
                        } else {
                            taxSummaryRows.add(DocumentTaxRow("IGST @ $rateLabel% on $taxableLabel", CurrencyFormatter.format(tb.totalTax)))
                        }
                    }
                }
            }

            val hasTax = taxBreakdown.any { it.totalTax > BigDecimal.ZERO }
            val upi = UpiPayment.forInvoice(invoice, business)

            // Frozen at issue time; the live profile is only a fallback for invoices without a snapshot.
            val seller = invoice.seller ?: BusinessSnapshot.from(business)

            return InvoiceDocumentModel(
                businessName = seller.name,
                businessAddress = seller.address,
                businessPhone = seller.phone,
                businessEmail = seller.email,
                businessGstin = seller.gstin,
                businessOwner = seller.ownerName,
                invoiceTitle = title,
                invoiceNumber = invoice.invoiceNumber,
                invoiceDate = formatDate(invoice.invoiceDate),
                dueDate = invoice.dueDate?.let { formatDate(it) },
                customerName = invoice.customerName,
                customerAddress = invoice.customerAddress?.takeIf { it.isNotBlank() },
                customerGstin = invoice.customerGstin?.takeIf { it.isNotBlank() },
                customerState = invoice.customerState?.takeIf { it.isNotBlank() },
                items = documentItems,
                hsnSummary = hsnRows,
                subtotal = CurrencyFormatter.format(invoice.subtotal),
                totalDiscount = CurrencyFormatter.format(invoice.totalDiscount),
                showDiscount = invoice.totalDiscount > BigDecimal.ZERO,
                taxSummary = taxSummaryRows,
                grandTotal = CurrencyFormatter.format(invoice.grandTotal),
                amountInWords = AmountInWords.convert(invoice.grandTotal),
                taxType = invoice.taxType,
                payment = PaymentSummary.build(invoice.paymentStatus, invoice.paymentMethod, invoice.amountPaid, invoice.balanceDue),
                upiQr = upi?.let {
                    DocumentUpiQr(payload = it.uri, caption = "Scan to pay ${it.amountLabel}", vpaLine = "UPI: ${it.vpa}")
                },
                notes = invoice.notes?.takeIf { it.isNotBlank() },
                terms = DECLARATION,
                signatureName = seller.ownerName,
                showTaxSummary = hasTax
            )
        }
    }
}

data class DocumentLineItem(
    val index: Int,
    val name: String,
    val hsnCode: String?,
    val quantity: String,
    val unit: String?,
    val rate: String,
    val discount: String?,
    val taxableAmount: String,
    val gstRate: String,
    val total: String
)

data class DocumentTaxRow(
    val label: String,
    val amount: String
)

data class DocumentHsnRow(
    val hsnCode: String,
    val gstRate: String,
    val taxableAmount: String,
    val taxAmount: String,
    val total: String
)

data class DocumentUpiQr(
    val payload: String,
    val caption: String,
    val vpaLine: String
)
