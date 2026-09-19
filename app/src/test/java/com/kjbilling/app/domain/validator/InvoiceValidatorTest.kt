package com.kjbilling.app.domain.validator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class InvoiceValidatorTest {

    @Test
    fun testValidInput() {
        val items = listOf(
            InvoiceItemInput("Item 1", BigDecimal("1"), BigDecimal("100"), BigDecimal.ZERO, BigDecimal("18"))
        )
        val errors = InvoiceValidator.validate("John Doe", items)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun testEmptyCustomer() {
        val items = listOf(
            InvoiceItemInput("Item 1", BigDecimal("1"), BigDecimal("100"), BigDecimal.ZERO, BigDecimal("18"))
        )
        val errors = InvoiceValidator.validate("", items)
        assertEquals(1, errors.size)
        assertEquals("Customer name cannot be blank", errors[0])
    }

    @Test
    fun testEmptyItems() {
        val errors = InvoiceValidator.validate("John Doe", emptyList())
        assertEquals(1, errors.size)
        assertEquals("Invoice must have at least one item", errors[0])
    }

    @Test
    fun testZeroQuantity() {
        val items = listOf(
            InvoiceItemInput("Item 1", BigDecimal.ZERO, BigDecimal("100"), BigDecimal.ZERO, BigDecimal("18"))
        )
        val errors = InvoiceValidator.validate("John Doe", items)
        assertEquals(1, errors.size)
        assertEquals("Item 1 quantity must be greater than zero", errors[0])
    }

    @Test
    fun testNegativePrice() {
        val items = listOf(
            InvoiceItemInput("Item 1", BigDecimal("1"), BigDecimal("-10"), BigDecimal.ZERO, BigDecimal("18"))
        )
        val errors = InvoiceValidator.validate("John Doe", items)
        assertEquals(1, errors.size)
        assertEquals("Item 1 unit price cannot be negative", errors[0])
    }

    @Test
    fun testDiscountGreaterThan100() {
        val items = listOf(
            InvoiceItemInput("Item 1", BigDecimal("1"), BigDecimal("100"), BigDecimal("110"), BigDecimal("18"))
        )
        val errors = InvoiceValidator.validate("John Doe", items)
        assertEquals(1, errors.size)
        assertEquals("Item 1 discount must be between 0 and 100", errors[0])
    }
}
