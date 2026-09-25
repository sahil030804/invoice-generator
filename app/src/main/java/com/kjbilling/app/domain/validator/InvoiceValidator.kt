package com.kjbilling.app.domain.validator

import java.math.BigDecimal

data class InvoiceItemInput(
    val name: String,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val discountPercent: BigDecimal,
    val gstRate: BigDecimal
)

object InvoiceValidator {
    // Upper bounds keep totals and PDF columns sane (₹10 crore price, 10 lakh units, 40% GST).
    val MAX_QUANTITY = BigDecimal("1000000")
    val MAX_UNIT_PRICE = BigDecimal("100000000")
    val MAX_GST_RATE = BigDecimal("40")

    fun isWithinLimits(quantity: BigDecimal, unitPrice: BigDecimal, gstRate: BigDecimal): Boolean {
        return quantity <= MAX_QUANTITY && unitPrice <= MAX_UNIT_PRICE && gstRate <= MAX_GST_RATE
    }

    fun validate(customerName: String, items: List<InvoiceItemInput>): List<String> {
        val errors = mutableListOf<String>()
        
        if (customerName.isBlank()) {
            errors.add("Customer name cannot be blank")
        }
        
        if (items.isEmpty()) {
            errors.add("Invoice must have at least one item")
        }
        
        items.forEachIndexed { index, item ->
            val itemNum = index + 1
            if (item.name.isBlank()) {
                errors.add("Item $itemNum name cannot be blank")
            }
            if (item.quantity <= BigDecimal.ZERO) {
                errors.add("Item $itemNum quantity must be greater than zero")
            }
            if (item.quantity > MAX_QUANTITY) {
                errors.add("Item $itemNum quantity is too large (max $MAX_QUANTITY)")
            }
            if (item.unitPrice < BigDecimal.ZERO) {
                errors.add("Item $itemNum unit price cannot be negative")
            }
            if (item.unitPrice > MAX_UNIT_PRICE) {
                errors.add("Item $itemNum unit price is too large (max $MAX_UNIT_PRICE)")
            }
            if (item.discountPercent < BigDecimal.ZERO || item.discountPercent > BigDecimal("100")) {
                errors.add("Item $itemNum discount must be between 0 and 100")
            }
            if (item.gstRate < BigDecimal.ZERO) {
                errors.add("Item $itemNum GST rate cannot be negative")
            }
            if (item.gstRate > MAX_GST_RATE) {
                errors.add("Item $itemNum GST rate cannot exceed $MAX_GST_RATE%")
            }
        }
        
        return errors
    }
}
