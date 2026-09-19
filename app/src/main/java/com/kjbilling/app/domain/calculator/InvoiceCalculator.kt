package com.kjbilling.app.domain.calculator

import com.kjbilling.app.domain.model.TaxBreakdown
import com.kjbilling.app.domain.model.TaxType
import java.math.BigDecimal
import java.math.RoundingMode

data class InvoiceItemCalculation(
    val itemAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val taxableAmount: BigDecimal,
    val cgstAmount: BigDecimal,
    val sgstAmount: BigDecimal,
    val igstAmount: BigDecimal,
    val taxAmount: BigDecimal,
    val total: BigDecimal,
    val gstRate: BigDecimal = BigDecimal.ZERO,
    val taxType: TaxType = TaxType.CGST_SGST
)

data class InvoiceTotals(
    val subtotal: BigDecimal,
    val totalDiscount: BigDecimal,
    val totalTax: BigDecimal,
    val grandTotal: BigDecimal,
    val taxBreakdown: List<TaxBreakdown>
)

class InvoiceCalculator {
    fun calculateItem(
        quantity: BigDecimal,
        unitPrice: BigDecimal,
        discountPercent: BigDecimal,
        gstRate: BigDecimal,
        taxType: TaxType
    ): InvoiceItemCalculation {
        val itemAmount = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP)
        
        var discountAmount = itemAmount.multiply(discountPercent).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        if (discountAmount > itemAmount) {
            discountAmount = itemAmount
        }
        
        var taxableAmount = itemAmount.subtract(discountAmount)
        if (taxableAmount < BigDecimal.ZERO) {
            taxableAmount = BigDecimal.ZERO.setScale(2)
        }
        
        val taxAmount = taxableAmount.multiply(gstRate).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        
        var cgst = BigDecimal.ZERO.setScale(2)
        var sgst = BigDecimal.ZERO.setScale(2)
        var igst = BigDecimal.ZERO.setScale(2)
        var finalTax = taxAmount
        
        when (taxType) {
            TaxType.CGST_SGST -> {
                val halfRate = gstRate.divide(BigDecimal("2"), 2, RoundingMode.HALF_UP)
                cgst = taxableAmount.multiply(halfRate).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
                sgst = cgst
                finalTax = cgst.add(sgst)
            }
            TaxType.IGST -> {
                igst = taxAmount
            }
            TaxType.NO_GST -> {
                finalTax = BigDecimal.ZERO.setScale(2)
            }
        }
        
        val total = taxableAmount.add(finalTax)
        
        return InvoiceItemCalculation(
            itemAmount = itemAmount,
            discountAmount = discountAmount,
            taxableAmount = taxableAmount,
            cgstAmount = cgst,
            sgstAmount = sgst,
            igstAmount = igst,
            taxAmount = finalTax,
            total = total,
            gstRate = gstRate,
            taxType = taxType
        )
    }

    fun calculateInvoice(items: List<InvoiceItemCalculation>): InvoiceTotals {
        var subtotal = BigDecimal.ZERO.setScale(2)
        var totalDiscount = BigDecimal.ZERO.setScale(2)
        var totalTax = BigDecimal.ZERO.setScale(2)
        
        for (item in items) {
            subtotal = subtotal.add(item.taxableAmount)
            totalDiscount = totalDiscount.add(item.discountAmount)
            totalTax = totalTax.add(item.taxAmount)
        }
        
        val grandTotal = subtotal.add(totalTax)
        
        val breakdownMap = mutableMapOf<BigDecimal, TaxBreakdown>()
        
        for (item in items) {
            val rate = item.gstRate.setScale(2, RoundingMode.HALF_UP)
            val existing = breakdownMap[rate] ?: TaxBreakdown(
                gstRate = rate,
                taxableAmount = BigDecimal.ZERO.setScale(2),
                cgstRate = if (item.taxType == TaxType.CGST_SGST) rate.divide(BigDecimal("2"), 2, RoundingMode.HALF_UP) else BigDecimal.ZERO.setScale(2),
                cgstAmount = BigDecimal.ZERO.setScale(2),
                sgstRate = if (item.taxType == TaxType.CGST_SGST) rate.divide(BigDecimal("2"), 2, RoundingMode.HALF_UP) else BigDecimal.ZERO.setScale(2),
                sgstAmount = BigDecimal.ZERO.setScale(2),
                igstRate = if (item.taxType == TaxType.IGST) rate else BigDecimal.ZERO.setScale(2),
                igstAmount = BigDecimal.ZERO.setScale(2),
                totalTax = BigDecimal.ZERO.setScale(2)
            )
            
            breakdownMap[rate] = existing.copy(
                taxableAmount = existing.taxableAmount.add(item.taxableAmount),
                cgstAmount = existing.cgstAmount.add(item.cgstAmount),
                sgstAmount = existing.sgstAmount.add(item.sgstAmount),
                igstAmount = existing.igstAmount.add(item.igstAmount),
                totalTax = existing.totalTax.add(item.taxAmount)
            )
        }
        
        return InvoiceTotals(
            subtotal = subtotal,
            totalDiscount = totalDiscount,
            totalTax = totalTax,
            grandTotal = grandTotal,
            taxBreakdown = breakdownMap.values.toList().sortedBy { it.gstRate }
        )
    }

    fun determineTaxType(businessState: String?, customerState: String?): TaxType {
        if (businessState.isNullOrBlank() || customerState.isNullOrBlank()) {
            return TaxType.CGST_SGST
        }
        return if (businessState.equals(customerState, ignoreCase = true)) {
            TaxType.CGST_SGST
        } else {
            TaxType.IGST
        }
    }
}
