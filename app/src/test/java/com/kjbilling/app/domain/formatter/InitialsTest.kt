package com.kjbilling.app.domain.formatter

import org.junit.Assert.assertEquals
import org.junit.Test

class InitialsTest {

    @Test
    fun twoWords_firstLetters() {
        assertEquals("AG", Initials.of("Acme Global"))
        assertEquals("RS", Initials.of("  rakesh   sharma  kumar "))
    }

    @Test
    fun oneWord_firstTwoLetters() {
        assertEquals("RA", Initials.of("Rakesh"))
        assertEquals("A", Initials.of("a"))
    }

    @Test
    fun blank_usesFallback() {
        assertEquals("?", Initials.of("   "))
        assertEquals("KJ", Initials.of("", fallback = "KJ"))
    }

    @Test
    fun nonLatinNames_work() {
        assertEquals("रा", Initials.of("राम"))
    }
}
