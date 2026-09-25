package com.kjbilling.app.domain.validator

import com.kjbilling.app.domain.calculator.InvoiceCalculator
import com.kjbilling.app.domain.model.TaxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StateResolverTest {

    private val calculator = InvoiceCalculator()

    @Test
    fun sameState_differentSpellingAndSpacing_isNotIgst() {
        assertTrue(StateResolver.isSameState("Tamil Nadu", "TamilNadu"))
        assertTrue(StateResolver.isSameState(" maharashtra ", "Maharashtra"))
        assertTrue(StateResolver.isSameState("Orissa", "Odisha"))
    }

    @Test
    fun differentStates_areNotSame() {
        assertEquals(false, StateResolver.isSameState("Gujarat", "Maharashtra"))
    }

    @Test
    fun fromGstin_returnsStateName() {
        assertEquals("Maharashtra", StateResolver.fromGstin("27AABCS1429B1ZX"))
        assertEquals("Gujarat", StateResolver.fromGstin("24AABCS1429B1ZX"))
        assertEquals("Ladakh", StateResolver.fromGstin("38AABCS1429B1ZX"))
    }

    @Test
    fun fromGstin_invalidOrBlank_returnsNull() {
        assertNull(StateResolver.fromGstin(null))
        assertNull(StateResolver.fromGstin(""))
        assertNull(StateResolver.fromGstin("XX"))
        assertNull(StateResolver.fromGstin("00AABCS1429B1ZX"))
    }

    @Test
    fun taxType_misspeltButSameState_staysCgstSgst() {
        assertEquals(TaxType.CGST_SGST, calculator.determineTaxType("Tamil Nadu", "TamilNadu"))
    }

    @Test
    fun taxType_blankCustomerState_usesGstinState() {
        val inter = calculator.determineTaxType("Maharashtra", null, customerGstin = "24AABCS1429B1ZX")
        val intra = calculator.determineTaxType("Maharashtra", "", customerGstin = "27AABCS1429B1ZX")

        assertEquals(TaxType.IGST, inter)
        assertEquals(TaxType.CGST_SGST, intra)
    }

    @Test
    fun taxType_gstinStateWinsOverTypedState() {
        val type = calculator.determineTaxType("Maharashtra", "Maharashtra", customerGstin = "24AABCS1429B1ZX")

        assertEquals(TaxType.IGST, type)
    }

    @Test
    fun taxType_noStateInfoAtAll_defaultsToCgstSgst() {
        assertEquals(TaxType.CGST_SGST, calculator.determineTaxType("Maharashtra", null))
    }
}
