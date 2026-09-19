package com.kjbilling.app.domain

import com.kjbilling.app.domain.calculator.InvoiceCalculator
import com.kjbilling.app.domain.model.TaxType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class InvoiceDiscountTest {

    private lateinit var calculator: InvoiceCalculator

    @Before
    fun setup() {
        calculator = InvoiceCalculator()
    }

    @Test
    fun testItemPercentDiscount() {
        val calc = calculator.calculateItem(
            quantity = BigDecimal("2"),
            unitPrice = BigDecimal("500.00"),
            discountPercent = BigDecimal("10.00"),
            gstRate = BigDecimal("18.00"),
            taxType = TaxType.CGST_SGST
        )

        assertEquals(BigDecimal("1000.00"), calc.itemAmount)
        assertEquals(BigDecimal("100.00"), calc.discountAmount)
        assertEquals(BigDecimal("900.00"), calc.taxableAmount)
        assertEquals(BigDecimal("162.00"), calc.taxAmount)
        assertEquals(BigDecimal("1062.00"), calc.total)
    }

    @Test
    fun testItemFlatAmountDiscount() {
        val calc = calculator.calculateItem(
            quantity = BigDecimal("2"),
            unitPrice = BigDecimal("500.00"),
            gstRate = BigDecimal("18.00"),
            taxType = TaxType.CGST_SGST,
            discountAmount = BigDecimal("150.00")
        )

        assertEquals(BigDecimal("1000.00"), calc.itemAmount)
        assertEquals(BigDecimal("150.00"), calc.discountAmount)
        assertEquals(BigDecimal("850.00"), calc.taxableAmount)
        assertEquals(BigDecimal("153.00"), calc.taxAmount)
        assertEquals(BigDecimal("1003.00"), calc.total)
    }

    @Test
    fun testInvoiceOverallPercentDiscount() {
        val item = calculator.calculateItem(
            quantity = BigDecimal("1"),
            unitPrice = BigDecimal("1000.00"),
            gstRate = BigDecimal("18.00"),
            taxType = TaxType.CGST_SGST
        )

        val totals = calculator.calculateInvoice(
            items = listOf(item),
            overallDiscountPercent = BigDecimal("10.00")
        )

        assertEquals(BigDecimal("1000.00"), totals.subtotal)
        assertEquals(BigDecimal("100.00"), totals.totalDiscount)
        assertEquals(BigDecimal("162.00"), totals.totalTax)
        assertEquals(BigDecimal("1062.00"), totals.grandTotal)
    }

    @Test
    fun testInvoiceOverallFlatDiscount() {
        val item = calculator.calculateItem(
            quantity = BigDecimal("1"),
            unitPrice = BigDecimal("1000.00"),
            gstRate = BigDecimal("18.00"),
            taxType = TaxType.CGST_SGST
        )

        val totals = calculator.calculateInvoice(
            items = listOf(item),
            overallDiscountAmount = BigDecimal("200.00")
        )

        assertEquals(BigDecimal("1000.00"), totals.subtotal)
        assertEquals(BigDecimal("200.00"), totals.totalDiscount)
        assertEquals(BigDecimal("144.00"), totals.totalTax) // 800 * 18% = 144
        assertEquals(BigDecimal("944.00"), totals.grandTotal)
    }

    @Test
    fun testCombinedItemAndOverallDiscount() {
        // Item 1: 1 x 1000, 100 item discount -> taxable 900
        val item1 = calculator.calculateItem(
            quantity = BigDecimal("1"),
            unitPrice = BigDecimal("1000.00"),
            gstRate = BigDecimal("18.00"),
            taxType = TaxType.CGST_SGST,
            discountAmount = BigDecimal("100.00")
        )
        // Item 2: 1 x 500, 0 item discount -> taxable 500
        val item2 = calculator.calculateItem(
            quantity = BigDecimal("1"),
            unitPrice = BigDecimal("500.00"),
            gstRate = BigDecimal("18.00"),
            taxType = TaxType.CGST_SGST
        )

        // Overall discount 10% on remaining 1400 taxable = 140
        val totals = calculator.calculateInvoice(
            items = listOf(item1, item2),
            overallDiscountPercent = BigDecimal("10.00")
        )

        assertEquals(BigDecimal("1500.00"), totals.subtotal) // gross
        assertEquals(BigDecimal("240.00"), totals.totalDiscount) // 100 + 140
        assertEquals(BigDecimal("226.80"), totals.totalTax) // (1400 - 140) * 18% = 1260 * 18% = 226.80
        assertEquals(BigDecimal("1486.80"), totals.grandTotal) // 1500 - 240 + 226.80 = 1486.80
    }

    @Test
    fun testInvoiceDiscountClampedToTaxableAmount() {
        val item = calculator.calculateItem(
            quantity = BigDecimal("1"),
            unitPrice = BigDecimal("100.00"),
            gstRate = BigDecimal("18.00"),
            taxType = TaxType.CGST_SGST
        )

        val totals = calculator.calculateInvoice(
            items = listOf(item),
            overallDiscountAmount = BigDecimal("250.00") // Greater than 100
        )

        assertEquals(BigDecimal("100.00"), totals.subtotal)
        assertEquals(BigDecimal("100.00"), totals.totalDiscount)
        assertEquals(BigDecimal("0.00"), totals.totalTax)
        assertEquals(BigDecimal("0.00"), totals.grandTotal)
    }
}
