package com.kjbilling.app.domain.calculator

import com.kjbilling.app.domain.model.TaxType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class InvoiceCalculatorTest {

    private val calculator = InvoiceCalculator()

    @Test
    fun testBasicCalculation() {
        val calc = calculator.calculateItem(
            quantity = BigDecimal("10"),
            unitPrice = BigDecimal("120"),
            discountPercent = BigDecimal.ZERO,
            gstRate = BigDecimal("18"),
            taxType = TaxType.CGST_SGST
        )
        
        assertEquals(BigDecimal("1200.00"), calc.itemAmount)
        assertEquals(BigDecimal("0.00"), calc.discountAmount)
        assertEquals(BigDecimal("1200.00"), calc.taxableAmount)
        assertEquals(BigDecimal("108.00"), calc.cgstAmount)
        assertEquals(BigDecimal("108.00"), calc.sgstAmount)
        assertEquals(BigDecimal("0.00"), calc.igstAmount)
        assertEquals(BigDecimal("216.00"), calc.taxAmount)
        assertEquals(BigDecimal("1416.00"), calc.total)
    }

    @Test
    fun testZeroGstRate() {
        val calc = calculator.calculateItem(
            quantity = BigDecimal("10"),
            unitPrice = BigDecimal("120"),
            discountPercent = BigDecimal.ZERO,
            gstRate = BigDecimal.ZERO,
            taxType = TaxType.CGST_SGST
        )
        
        assertEquals(BigDecimal("1200.00"), calc.itemAmount)
        assertEquals(BigDecimal("0.00"), calc.taxAmount)
        assertEquals(BigDecimal("1200.00"), calc.total)
    }

    @Test
    fun testFivePercentGstWithCgstSgst() {
        val calc = calculator.calculateItem(
            quantity = BigDecimal("10"),
            unitPrice = BigDecimal("100"),
            discountPercent = BigDecimal.ZERO,
            gstRate = BigDecimal("5"),
            taxType = TaxType.CGST_SGST
        )
        
        assertEquals(BigDecimal("1000.00"), calc.taxableAmount)
        assertEquals(BigDecimal("25.00"), calc.cgstAmount)
        assertEquals(BigDecimal("25.00"), calc.sgstAmount)
        assertEquals(BigDecimal("50.00"), calc.taxAmount)
        assertEquals(BigDecimal("1050.00"), calc.total)
    }

    @Test
    fun testIgstCalculation() {
        val calc = calculator.calculateItem(
            quantity = BigDecimal("10"),
            unitPrice = BigDecimal("120"),
            discountPercent = BigDecimal.ZERO,
            gstRate = BigDecimal("18"),
            taxType = TaxType.IGST
        )
        
        assertEquals(BigDecimal("1200.00"), calc.taxableAmount)
        assertEquals(BigDecimal("0.00"), calc.cgstAmount)
        assertEquals(BigDecimal("0.00"), calc.sgstAmount)
        assertEquals(BigDecimal("216.00"), calc.igstAmount)
        assertEquals(BigDecimal("216.00"), calc.taxAmount)
        assertEquals(BigDecimal("1416.00"), calc.total)
    }

    @Test
    fun testNoGstTaxType() {
        val calc = calculator.calculateItem(
            quantity = BigDecimal("10"),
            unitPrice = BigDecimal("120"),
            discountPercent = BigDecimal.ZERO,
            gstRate = BigDecimal("18"),
            taxType = TaxType.NO_GST
        )
        
        assertEquals(BigDecimal("1200.00"), calc.taxableAmount)
        assertEquals(BigDecimal("0.00"), calc.taxAmount)
        assertEquals(BigDecimal("1200.00"), calc.total)
    }

    @Test
    fun testDiscount() {
        val calc = calculator.calculateItem(
            quantity = BigDecimal("10"),
            unitPrice = BigDecimal("120"),
            discountPercent = BigDecimal("10"),
            gstRate = BigDecimal("18"),
            taxType = TaxType.CGST_SGST
        )
        
        assertEquals(BigDecimal("1200.00"), calc.itemAmount)
        assertEquals(BigDecimal("120.00"), calc.discountAmount)
        assertEquals(BigDecimal("1080.00"), calc.taxableAmount)
        assertEquals(BigDecimal("194.40"), calc.taxAmount) // 1080 * 18%
    }

    @Test
    fun testHundredPercentDiscount() {
        val calc = calculator.calculateItem(
            quantity = BigDecimal("10"),
            unitPrice = BigDecimal("120"),
            discountPercent = BigDecimal("100"),
            gstRate = BigDecimal("18"),
            taxType = TaxType.CGST_SGST
        )
        
        assertEquals(BigDecimal("120.00").multiply(BigDecimal("10")), calc.itemAmount)
        assertEquals(BigDecimal("1200.00"), calc.discountAmount)
        assertEquals(BigDecimal("0.00"), calc.taxableAmount)
        assertEquals(BigDecimal("0.00"), calc.taxAmount)
        assertEquals(BigDecimal("0.00"), calc.total)
    }
    
    @Test
    fun testDiscountClampedToItemAmount() {
        val calc = calculator.calculateItem(
            quantity = BigDecimal("10"),
            unitPrice = BigDecimal("120"),
            discountPercent = BigDecimal("150"), // > 100%
            gstRate = BigDecimal("18"),
            taxType = TaxType.CGST_SGST
        )
        
        assertEquals(BigDecimal("1200.00"), calc.itemAmount)
        assertEquals(BigDecimal("1200.00"), calc.discountAmount) // Clamped
        assertEquals(BigDecimal("0.00"), calc.taxableAmount)
        assertEquals(BigDecimal("0.00"), calc.taxAmount)
        assertEquals(BigDecimal("0.00"), calc.total)
    }

    @Test
    fun testMultipleItemsInvoiceTotals() {
        val item1 = calculator.calculateItem(
            quantity = BigDecimal("1"), unitPrice = BigDecimal("100"),
            discountPercent = BigDecimal.ZERO, gstRate = BigDecimal("18"), taxType = TaxType.CGST_SGST
        )
        val item2 = calculator.calculateItem(
            quantity = BigDecimal("2"), unitPrice = BigDecimal("50"),
            discountPercent = BigDecimal.ZERO, gstRate = BigDecimal("5"), taxType = TaxType.CGST_SGST
        )
        
        val totals = calculator.calculateInvoice(listOf(item1, item2))
        
        assertEquals(BigDecimal("200.00"), totals.subtotal)
        assertEquals(BigDecimal("0.00"), totals.totalDiscount)
        assertEquals(BigDecimal("23.00"), totals.totalTax) // 18 + 5
        assertEquals(BigDecimal("223.00"), totals.grandTotal)
        assertEquals(2, totals.taxBreakdown.size)
    }

    @Test
    fun testZeroPriceItem() {
        val calc = calculator.calculateItem(
            quantity = BigDecimal("10"), unitPrice = BigDecimal.ZERO,
            discountPercent = BigDecimal.ZERO, gstRate = BigDecimal("18"), taxType = TaxType.CGST_SGST
        )
        assertEquals(BigDecimal("0.00"), calc.total)
    }

    @Test
    fun testDecimalQuantities() {
        val calc = calculator.calculateItem(
            quantity = BigDecimal("2.5"), unitPrice = BigDecimal("100"),
            discountPercent = BigDecimal.ZERO, gstRate = BigDecimal("18"), taxType = TaxType.CGST_SGST
        )
        assertEquals(BigDecimal("250.00"), calc.itemAmount)
        assertEquals(BigDecimal("45.00"), calc.taxAmount)
        assertEquals(BigDecimal("295.00"), calc.total)
    }

    @Test
    fun testDetermineTaxType() {
        assertEquals(TaxType.CGST_SGST, calculator.determineTaxType("Maharashtra", "Maharashtra"))
        assertEquals(TaxType.IGST, calculator.determineTaxType("Maharashtra", "Gujarat"))
        assertEquals(TaxType.CGST_SGST, calculator.determineTaxType("Maharashtra", null))
        assertEquals(TaxType.CGST_SGST, calculator.determineTaxType(null, "Gujarat"))
        assertEquals(TaxType.CGST_SGST, calculator.determineTaxType(null, null))
    }
}
