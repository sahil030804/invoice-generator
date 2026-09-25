package com.kjbilling.app.domain.insights

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ChartScalingTest {

    private fun f(vararg v: String) = ChartScaling.fractions(v.map { BigDecimal(it) })

    @Test
    fun tallestBarIsFull_othersProportional() {
        val bars = f("0", "50", "100")

        assertEquals(0f, bars[0], 0.0001f)
        assertEquals(0.5f, bars[1], 0.0001f)
        assertEquals(1f, bars[2], 0.0001f)
    }

    @Test
    fun allZero_meansNoBars() {
        assertEquals(listOf(0f, 0f, 0f), f("0", "0", "0"))
        assertEquals(emptyList<Float>(), f())
    }

    @Test
    fun tinyNonZeroValues_stayVisible() {
        val bars = f("1", "100000")

        assertTrue(bars[0] >= ChartScaling.MIN_VISIBLE_FRACTION)
        assertEquals(1f, bars[1], 0.0001f)
    }
}
