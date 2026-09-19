package com.kjbilling.app.domain

import com.kjbilling.app.domain.calculator.InvoiceCalculator
import com.kjbilling.app.domain.model.*
import com.kjbilling.app.domain.validator.InvoiceItemInput
import com.kjbilling.app.domain.validator.InvoiceValidator
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class InvoiceUpdateLogicTest {

    private val calculator = InvoiceCalculator()

    private fun createSampleInvoice(
        id: Long = 100L,
        invoiceNumber: String = "INV-0001",
        customerState: String = "Maharashtra",
        taxType: TaxType = TaxType.CGST_SGST,
        amountPaid: BigDecimal = BigDecimal.ZERO,
        paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
        status: InvoiceStatus = InvoiceStatus.GENERATED
    ): Invoice {
        val item1 = calculator.calculateItem(
            quantity = BigDecimal("2"),
            unitPrice = BigDecimal("100"),
            discountPercent = BigDecimal.ZERO,
            gstRate = BigDecimal("18"),
            taxType = taxType
        )
        val domainItem = InvoiceItem(
            id = 1L,
            invoiceId = id,
            productId = 10L,
            itemName = "Item 1",
            hsnCode = "8539",
            quantity = BigDecimal("2"),
            unit = "PCS",
            unitPrice = BigDecimal("100"),
            discountPercent = BigDecimal.ZERO,
            discountAmount = item1.discountAmount,
            gstRate = item1.gstRate,
            taxableAmount = item1.taxableAmount,
            cgstAmount = item1.cgstAmount,
            sgstAmount = item1.sgstAmount,
            igstAmount = item1.igstAmount,
            taxAmount = item1.taxAmount,
            total = item1.total,
            sortOrder = 0
        )
        val totals = calculator.calculateInvoice(listOf(item1))

        return Invoice(
            id = id,
            invoiceNumber = invoiceNumber,
            invoiceDate = 1700000000000L,
            dueDate = 1700604800000L,
            customerId = 1L,
            customerName = "Original Customer",
            customerGstin = "27AABCS1429B1ZX",
            customerState = customerState,
            customerAddress = "123 Street, Pune",
            items = listOf(domainItem),
            subtotal = totals.subtotal,
            totalDiscount = totals.totalDiscount,
            totalTax = totals.totalTax,
            grandTotal = totals.grandTotal,
            taxType = taxType,
            status = status,
            paymentStatus = paymentStatus,
            paymentMethod = PaymentMethod.CASH,
            amountPaid = amountPaid,
            notes = "Original notes",
            createdAt = 1700000000000L,
            updatedAt = 1700000000000L
        )
    }

    @Test
    fun testPreserveInvoiceIdentityOnUpdate() {
        val original = createSampleInvoice(id = 42L, invoiceNumber = "INV-0042")
        
        // Simulating an update where items change
        val updatedTime = System.currentTimeMillis()
        val updated = original.copy(
            notes = "Updated notes",
            updatedAt = updatedTime
        )

        assertEquals(42L, updated.id)
        assertEquals("INV-0042", updated.invoiceNumber)
        assertEquals(original.invoiceDate, updated.invoiceDate)
        assertEquals(original.createdAt, updated.createdAt)
        assertEquals("Updated notes", updated.notes)
        assertEquals(updatedTime, updated.updatedAt)
    }

    @Test
    fun testModifyItemQuantityRecalculatesTotals() {
        val original = createSampleInvoice()
        assertEquals(BigDecimal("236.00"), original.grandTotal) // 200 + 36 tax

        // Update quantity from 2 to 5
        val newItemCalc = calculator.calculateItem(
            quantity = BigDecimal("5"),
            unitPrice = BigDecimal("100"),
            discountPercent = BigDecimal.ZERO,
            gstRate = BigDecimal("18"),
            taxType = original.taxType
        )
        val newTotals = calculator.calculateInvoice(listOf(newItemCalc))

        assertEquals(BigDecimal("500.00"), newTotals.subtotal)
        assertEquals(BigDecimal("90.00"), newTotals.totalTax) // 18% of 500
        assertEquals(BigDecimal("590.00"), newTotals.grandTotal)
    }

    @Test
    fun testModifyItemUnitPriceAndDiscount() {
        val original = createSampleInvoice()

        // Change price to 200, discount to 10%
        val itemCalc = calculator.calculateItem(
            quantity = BigDecimal("2"),
            unitPrice = BigDecimal("200"),
            discountPercent = BigDecimal("10"),
            gstRate = BigDecimal("18"),
            taxType = original.taxType
        )

        assertEquals(BigDecimal("400.00"), itemCalc.itemAmount)
        assertEquals(BigDecimal("40.00"), itemCalc.discountAmount)
        assertEquals(BigDecimal("360.00"), itemCalc.taxableAmount)
        assertEquals(BigDecimal("32.40"), itemCalc.cgstAmount)
        assertEquals(BigDecimal("32.40"), itemCalc.sgstAmount)
        assertEquals(BigDecimal("64.80"), itemCalc.taxAmount)
        assertEquals(BigDecimal("424.80"), itemCalc.total)
    }

    @Test
    fun testAddNewItemToExistingInvoice() {
        val original = createSampleInvoice()

        val item1 = calculator.calculateItem(
            quantity = BigDecimal("2"),
            unitPrice = BigDecimal("100"),
            discountPercent = BigDecimal.ZERO,
            gstRate = BigDecimal("18"),
            taxType = original.taxType
        )
        val item2 = calculator.calculateItem(
            quantity = BigDecimal("1"),
            unitPrice = BigDecimal("500"),
            discountPercent = BigDecimal.ZERO,
            gstRate = BigDecimal("18"),
            taxType = original.taxType
        )
        val newTotals = calculator.calculateInvoice(listOf(item1, item2))

        assertEquals(BigDecimal("700.00"), newTotals.subtotal)
        assertEquals(BigDecimal("126.00"), newTotals.totalTax)
        assertEquals(BigDecimal("826.00"), newTotals.grandTotal)
    }

    @Test
    fun testRemoveItemFromInvoice() {
        val item1 = calculator.calculateItem(
            quantity = BigDecimal("2"),
            unitPrice = BigDecimal("100"),
            discountPercent = BigDecimal.ZERO,
            gstRate = BigDecimal("18"),
            taxType = TaxType.CGST_SGST
        )
        val item2 = calculator.calculateItem(
            quantity = BigDecimal("1"),
            unitPrice = BigDecimal("500"),
            discountPercent = BigDecimal.ZERO,
            gstRate = BigDecimal("18"),
            taxType = TaxType.CGST_SGST
        )
        // Two items initially
        val initialTotals = calculator.calculateInvoice(listOf(item1, item2))
        assertEquals(BigDecimal("826.00"), initialTotals.grandTotal)

        // Remove item 2
        val afterRemovalTotals = calculator.calculateInvoice(listOf(item1))
        assertEquals(BigDecimal("236.00"), afterRemovalTotals.grandTotal)
    }

    @Test
    fun testTaxTypeChangesWhenCustomerStateChanges() {
        val businessState = "Maharashtra"
        
        // Intra-state customer
        val intraTaxType = calculator.determineTaxType(businessState, "Maharashtra")
        assertEquals(TaxType.CGST_SGST, intraTaxType)

        // Customer updated to Inter-state (Gujarat)
        val interTaxType = calculator.determineTaxType(businessState, "Gujarat")
        assertEquals(TaxType.IGST, interTaxType)

        val itemWithIgst = calculator.calculateItem(
            quantity = BigDecimal("2"),
            unitPrice = BigDecimal("100"),
            discountPercent = BigDecimal.ZERO,
            gstRate = BigDecimal("18"),
            taxType = interTaxType
        )
        assertEquals(BigDecimal("0.00"), itemWithIgst.cgstAmount)
        assertEquals(BigDecimal("0.00"), itemWithIgst.sgstAmount)
        assertEquals(BigDecimal("36.00"), itemWithIgst.igstAmount)
        assertEquals(BigDecimal("36.00"), itemWithIgst.taxAmount)
        assertEquals(BigDecimal("236.00"), itemWithIgst.total)
    }

    @Test
    fun testValidationFailsWhenAllItemsRemoved() {
        val errors = InvoiceValidator.validate("Test Customer", emptyList())
        assertTrue(errors.contains("Invoice must have at least one item"))
    }

    @Test
    fun testValidationFailsOnInvalidItemValues() {
        val invalidItems = listOf(
            InvoiceItemInput(
                name = "Bad Item",
                quantity = BigDecimal.ZERO, // invalid
                unitPrice = BigDecimal("-50"), // invalid
                discountPercent = BigDecimal("120"), // invalid (>100)
                gstRate = BigDecimal("-5") // invalid
            )
        )
        val errors = InvoiceValidator.validate("Test Customer", invalidItems)
        assertEquals(4, errors.size)
        assertTrue(errors.any { it.contains("quantity must be greater than zero") })
        assertTrue(errors.any { it.contains("unit price cannot be negative") })
        assertTrue(errors.any { it.contains("discount must be between 0 and 100") })
        assertTrue(errors.any { it.contains("GST rate cannot be negative") })
    }

    @Test
    fun testPaymentStatusTransitionWhenGrandTotalIncreases() {
        // Original: grandTotal = 236.00, amountPaid = 236.00, status = PAID
        val original = createSampleInvoice(
            amountPaid = BigDecimal("236.00"),
            paymentStatus = PaymentStatus.PAID,
            status = InvoiceStatus.PAID
        )

        // User edits items: grand total increases to 590.00
        val newGrandTotal = BigDecimal("590.00")
        val amountPaid = original.amountPaid // 236.00

        val newPaymentStatus = when {
            amountPaid >= newGrandTotal -> PaymentStatus.PAID
            amountPaid > BigDecimal.ZERO -> PaymentStatus.PARTIAL
            else -> PaymentStatus.UNPAID
        }

        val newInvoiceStatus = if (newPaymentStatus == PaymentStatus.PAID) {
            InvoiceStatus.PAID
        } else if (original.status == InvoiceStatus.PAID) {
            InvoiceStatus.GENERATED
        } else {
            original.status
        }

        assertEquals(PaymentStatus.PARTIAL, newPaymentStatus)
        assertEquals(InvoiceStatus.GENERATED, newInvoiceStatus)
    }

    @Test
    fun testPaymentStatusTransitionWhenGrandTotalDecreasesBelowAmountPaid() {
        // Original: grandTotal = 590.00, amountPaid = 300.00, status = GENERATED, paymentStatus = PARTIAL
        val original = createSampleInvoice(
            amountPaid = BigDecimal("300.00"),
            paymentStatus = PaymentStatus.PARTIAL,
            status = InvoiceStatus.GENERATED
        )

        // User edits items: grand total decreases to 236.00
        val newGrandTotal = BigDecimal("236.00")
        val amountPaid = original.amountPaid // 300.00

        val adjustedAmountPaid = amountPaid.min(newGrandTotal)
        val newPaymentStatus = when {
            adjustedAmountPaid >= newGrandTotal -> PaymentStatus.PAID
            adjustedAmountPaid > BigDecimal.ZERO -> PaymentStatus.PARTIAL
            else -> PaymentStatus.UNPAID
        }

        val newInvoiceStatus = if (newPaymentStatus == PaymentStatus.PAID && original.status != InvoiceStatus.CANCELLED) {
            InvoiceStatus.PAID
        } else {
            original.status
        }

        assertEquals(BigDecimal("236.00"), adjustedAmountPaid)
        assertEquals(PaymentStatus.PAID, newPaymentStatus)
        assertEquals(InvoiceStatus.PAID, newInvoiceStatus)
    }

    @Test
    fun testPaymentStatusRemainsUnpaidIfZeroAmountPaid() {
        val original = createSampleInvoice(
            amountPaid = BigDecimal.ZERO,
            paymentStatus = PaymentStatus.UNPAID,
            status = InvoiceStatus.GENERATED
        )

        val newGrandTotal = BigDecimal("1000.00")
        val amountPaid = original.amountPaid

        val newPaymentStatus = when {
            amountPaid >= newGrandTotal -> PaymentStatus.PAID
            amountPaid > BigDecimal.ZERO -> PaymentStatus.PARTIAL
            else -> PaymentStatus.UNPAID
        }

        assertEquals(PaymentStatus.UNPAID, newPaymentStatus)
    }

    @Test
    fun testCancelledInvoiceCannotBeEdited() {
        val cancelledInvoice = createSampleInvoice(status = InvoiceStatus.CANCELLED)
        val canEdit = cancelledInvoice.status != InvoiceStatus.CANCELLED
        assertFalse(canEdit)
    }
}
