package com.kjbilling.app.domain.calculator

import com.kjbilling.app.domain.model.TaxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class InclusiveItemTest {

    private val calculator = InvoiceCalculator()

    private fun split(amount: String, rate: String, type: TaxType = TaxType.CGST_SGST) =
        calculator.calculateInclusiveItem(BigDecimal(amount), BigDecimal(rate), type)

    private fun assertSplit(amount: String, rate: String, taxable: String, cgst: String, sgst: String) {
        val calc = split(amount, rate)
        assertEquals(BigDecimal(taxable), calc.taxableAmount)
        assertEquals(BigDecimal(cgst), calc.cgstAmount)
        assertEquals(BigDecimal(sgst), calc.sgstAmount)
        assertEquals(BigDecimal(amount).setScale(2), calc.total)
    }

    @Test
    fun hundredRupees_atEachSlab() {
        assertSplit("100", "5", "95.24", "2.38", "2.38")
        assertSplit("100", "12", "89.29", "5.36", "5.35")
        assertSplit("100", "18", "84.75", "7.63", "7.62")
        assertSplit("100", "28", "78.13", "10.94", "10.93")
    }

    @Test
    fun smallAndOddAmounts() {
        assertSplit("1", "18", "0.85", "0.08", "0.07")
        assertSplit("49.99", "18", "42.36", "3.82", "3.81")
    }

    @Test
    fun igst_putsAllTaxInIgst() {
        val calc = split("100", "18", TaxType.IGST)

        assertEquals(BigDecimal("84.75"), calc.taxableAmount)
        assertEquals(BigDecimal("15.25"), calc.igstAmount)
        assertEquals(BigDecimal("0.00"), calc.cgstAmount)
        assertEquals(BigDecimal("100.00"), calc.total)
    }

    @Test
    fun noGstOrZeroRate_taxableIsWholeAmount() {
        listOf(split("250", "18", TaxType.NO_GST), split("250", "0")).forEach { calc ->
            assertEquals(BigDecimal("250.00"), calc.taxableAmount)
            assertEquals(BigDecimal("0.00"), calc.taxAmount)
            assertEquals(BigDecimal("250.00"), calc.total)
        }
    }

    @Test
    fun largeAmount_staysExact() {
        assertEquals(BigDecimal("9999999.99"), split("9999999.99", "28").total)
    }

    @Test
    fun sweep_totalAlwaysEqualsTypedAmount_andHalvesBalance() {
        val rates = listOf("0", "0.25", "3", "5", "12", "18", "28")
        for (paise in 1..2000) {
            val amount = BigDecimal(paise).movePointLeft(2)
            for (rate in rates) {
                for (type in listOf(TaxType.CGST_SGST, TaxType.IGST)) {
                    val c = calculator.calculateInclusiveItem(amount, BigDecimal(rate), type)
                    assertEquals("$amount @$rate $type", amount.setScale(2), c.taxableAmount + c.taxAmount)
                    assertEquals(c.total, c.taxableAmount + c.taxAmount)
                    if (type == TaxType.CGST_SGST) {
                        assertEquals(c.taxAmount, c.cgstAmount + c.sgstAmount)
                        assertTrue((c.cgstAmount - c.sgstAmount).abs() <= BigDecimal("0.01"))
                    }
                }
            }
        }
    }

    @Test
    fun invoiceTotal_mixingInclusiveAndExclusive() {
        val inclusive = split("100", "18")
        val exclusive = calculator.calculateItem(BigDecimal.ONE, BigDecimal("100"), gstRate = BigDecimal("18"))

        val totals = calculator.calculateInvoice(listOf(inclusive, exclusive))

        assertEquals(BigDecimal("218.00"), totals.grandTotal)
    }
}
