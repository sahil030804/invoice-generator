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
            if (item.unitPrice < BigDecimal.ZERO) {
                errors.add("Item $itemNum unit price cannot be negative")
            }
            if (item.discountPercent < BigDecimal.ZERO || item.discountPercent > BigDecimal("100")) {
                errors.add("Item $itemNum discount must be between 0 and 100")
            }
            if (item.gstRate < BigDecimal.ZERO) {
                errors.add("Item $itemNum GST rate cannot be negative")
            }
        }
        
        return errors
    }
}
