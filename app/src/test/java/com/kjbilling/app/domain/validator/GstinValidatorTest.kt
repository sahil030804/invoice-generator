package com.kjbilling.app.domain.validator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GstinValidatorTest {

    @Test
    fun testValidGstin() {
        val result = GstinValidator.validate("27AAPFU0939F1ZV")
        assertTrue(result.isValid)
    }

    @Test
    fun testEmptyGstin() {
        val result = GstinValidator.validate("")
        assertFalse(result.isValid)
        assertEquals("GSTIN cannot be empty", result.error)
    }

    @Test
    fun testTooShortGstin() {
        val result = GstinValidator.validate("27AAPFU")
        assertFalse(result.isValid)
        assertEquals("GSTIN must be 15 characters long", result.error)
    }

    @Test
    fun testInvalidStateCode() {
        val result = GstinValidator.validate("99AAPFU0939F1ZV")
        assertFalse(result.isValid)
        assertEquals("Invalid state code", result.error)
    }

    @Test
    fun testInvalidCharacters() {
        val result = GstinValidator.validate("27AAPFU0939F1Z@")
        assertFalse(result.isValid)
    }
}
