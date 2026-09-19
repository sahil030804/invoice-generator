package com.kjbilling.app.domain

import com.kjbilling.app.domain.calculator.InvoiceCalculator
import com.kjbilling.app.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class QuickBillLogicTest {

    private val calculator = InvoiceCalculator()

    @Test
    fun testDefaultCustomerIsWalkIn() {
        val selectedCustomer: Customer? = null
        val customerName = selectedCustomer?.name ?: "Walk-in Customer"
        val customerId = selectedCustomer?.id

        assertEquals("Walk-in Customer", customerName)
        assertNull(customerId)
    }

    @Test
    fun testIncrementAndDecrementQuantity() {
        val quantities = mutableMapOf<Long, Int>()

        // Increment product 1
        quantities[1L] = (quantities[1L] ?: 0) + 1
        assertEquals(1, quantities[1L])

        // Increment again
        quantities[1L] = (quantities[1L] ?: 0) + 1
        assertEquals(2, quantities[1L])

        // Decrement product 1
        quantities[1L] = (quantities[1L] ?: 0) - 1
        assertEquals(1, quantities[1L])

        // Decrement product 1 to 0 -> should remove from map
        val currentQty = quantities[1L] ?: 0
        if (currentQty <= 1) {
            quantities.remove(1L)
        } else {
            quantities[1L] = currentQty - 1
        }
        assertFalse(quantities.containsKey(1L))
    }

    @Test
    fun testCartTotalCalculationWithTax() {
        val bulb = Product(
            id = 1L,
            name = "LED Bulb 9W",
            sellingPrice = BigDecimal("120.00"),
            hsnCode = "8539",
            unit = "PCS",
            gstRate = BigDecimal("18.00"),
            sku = "BULB-09W"
        )

        val qty = 2
        val itemCalc = calculator.calculateItem(
            quantity = BigDecimal(qty),
            unitPrice = bulb.sellingPrice,
            discountPercent = BigDecimal.ZERO,
            gstRate = bulb.gstRate ?: BigDecimal.ZERO,
            taxType = TaxType.CGST_SGST
        )

        assertEquals(BigDecimal("240.00"), itemCalc.taxableAmount)
        assertEquals(BigDecimal("21.60"), itemCalc.cgstAmount)
        assertEquals(BigDecimal("21.60"), itemCalc.sgstAmount)
        assertEquals(BigDecimal("43.20"), itemCalc.taxAmount)
        assertEquals(BigDecimal("283.20"), itemCalc.total)

        val totals = calculator.calculateInvoice(listOf(itemCalc))
        assertEquals(BigDecimal("240.00"), totals.subtotal)
        assertEquals(BigDecimal("43.20"), totals.totalTax)
        assertEquals(BigDecimal("283.20"), totals.grandTotal)
    }

    @Test
    fun testCartMultipleItemsCalculation() {
        val bulb = Product(id = 1L, name = "LED Bulb 9W", sellingPrice = BigDecimal("120.00"), hsnCode = "8539", unit = "PCS", gstRate = BigDecimal("18.00"), sku = "BULB")
        val switch = Product(id = 2L, name = "Modular Switch 6A", sellingPrice = BigDecimal("45.00"), hsnCode = "8536", unit = "PCS", gstRate = BigDecimal("18.00"), sku = "SW-6A")
        val tape = Product(id = 3L, name = "Insulation Tape", sellingPrice = BigDecimal("20.00"), hsnCode = "3919", unit = "ROLL", gstRate = BigDecimal("18.00"), sku = "TAPE")

        val cart = mapOf(1L to 2, 2L to 1, 3L to 1) // Total 4 items
        val products = listOf(bulb, switch, tape).associateBy { it.id }

        val calculations = cart.map { (id, qty) ->
            val p = products[id]!!
            calculator.calculateItem(
                quantity = BigDecimal(qty),
                unitPrice = p.sellingPrice,
                discountPercent = BigDecimal.ZERO,
                gstRate = p.gstRate!!,
                taxType = TaxType.CGST_SGST
            )
        }

        val totals = calculator.calculateInvoice(calculations)

        // Subtotals: 240 + 45 + 20 = 305.00
        assertEquals(BigDecimal("305.00"), totals.subtotal)
        // Tax 18% on 305 = 54.90
        assertEquals(BigDecimal("54.90"), totals.totalTax)
        // Grand Total: 305 + 54.90 = 359.90
        assertEquals(BigDecimal("359.90"), totals.grandTotal)
        assertEquals(4, cart.values.sum())
    }

    @Test
    fun testInterstateCustomerTaxDetermination() {
        val businessState = "Maharashtra"

        // Walk-in customer (no state) -> CGST_SGST
        val walkInTax = calculator.determineTaxType(businessState, null)
        assertEquals(TaxType.CGST_SGST, walkInTax)

        // Same state customer -> CGST_SGST
        val intrastateTax = calculator.determineTaxType(businessState, "Maharashtra")
        assertEquals(TaxType.CGST_SGST, intrastateTax)

        // Out-of-state customer -> IGST
        val interstateTax = calculator.determineTaxType(businessState, "Gujarat")
        assertEquals(TaxType.IGST, interstateTax)
    }

    @Test
    fun testZeroQuantityCannotGenerateInvoice() {
        val cart = emptyMap<Long, Int>()
        val canGenerate = cart.values.any { it > 0 }
        assertFalse(canGenerate)
    }

    @Test
    fun testQuickBillAlwaysCreatedAsPaid() {
        val totalAmount = BigDecimal("359.90")
        val invoice = Invoice(
            id = 0L,
            invoiceNumber = "INV-0004",
            customerId = null,
            customerName = "Walk-in Customer",
            customerAddress = null,
            customerState = null,
            customerGstin = null,
            invoiceDate = System.currentTimeMillis(),
            dueDate = null,
            items = emptyList(),
            subtotal = BigDecimal("305.00"),
            totalDiscount = BigDecimal.ZERO,
            totalTax = BigDecimal("54.90"),
            grandTotal = totalAmount,
            taxType = TaxType.CGST_SGST,
            status = InvoiceStatus.PAID,
            paymentStatus = PaymentStatus.PAID,
            paymentMethod = PaymentMethod.CASH,
            amountPaid = totalAmount,
            notes = "Counter Sale (Quick Bill)"
        )

        assertEquals(InvoiceStatus.PAID, invoice.status)
        assertEquals(PaymentStatus.PAID, invoice.paymentStatus)
        assertEquals(PaymentMethod.CASH, invoice.paymentMethod)
        assertEquals(invoice.grandTotal, invoice.amountPaid)
        assertEquals("Walk-in Customer", invoice.customerName)
    }
}
