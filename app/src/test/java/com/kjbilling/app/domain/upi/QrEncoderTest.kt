package com.kjbilling.app.domain.upi

import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QrEncoderTest {

    private val payload = "upi://pay?pa=kjplastic@okaxis&pn=KJ%20Plastic&am=118.00&cu=INR&tn=INV-0008"

    @Test
    fun size_isAValidQrVersion() {
        val matrix = QrEncoder.encode(payload)

        assertTrue(matrix.size >= 21)
        assertEquals(0, (matrix.size - 17) % 4)
    }

    @Test
    fun finderPattern_topLeft() {
        val m = QrEncoder.encode(payload)

        (0..6).forEach { i ->
            assertTrue(m.isDark(i, 0)); assertTrue(m.isDark(0, i)); assertTrue(m.isDark(i, 6)); assertTrue(m.isDark(6, i))
        }
        assertFalse(m.isDark(1, 1))
        assertTrue(m.isDark(3, 3))
    }

    @Test
    fun encoding_isDeterministic() {
        val a = QrEncoder.encode(payload)
        val b = QrEncoder.encode(payload)

        assertEquals(a.darkRuns(), b.darkRuns())
    }

    @Test
    fun darkRuns_coverExactlyTheDarkCells_andAreMaximal() {
        val m = QrEncoder.encode(payload)
        val covered = HashSet<Pair<Int, Int>>()
        val runs = m.darkRuns()

        runs.forEach { run ->
            (run.start until run.endExclusive).forEach { x -> assertTrue(covered.add(x to run.row)) }
        }
        (0 until m.size).forEach { y ->
            (0 until m.size).forEach { x -> assertEquals(m.isDark(x, y), (x to y) in covered) }
        }
        runs.groupBy { it.row }.values.forEach { rowRuns ->
            rowRuns.sortedBy { it.start }.zipWithNext().forEach { (a, b) -> assertTrue(a.endExclusive < b.start) }
        }
    }

    @Test
    fun roundTrip_decodesBackToPayload() {
        val m = QrEncoder.encode(payload)
        val scale = 4
        val quiet = 4
        val side = (m.size + 2 * quiet) * scale
        val pixels = IntArray(side * side) { 0xFFFFFFFF.toInt() }
        (0 until m.size).forEach { y ->
            (0 until m.size).forEach { x ->
                if (m.isDark(x, y)) {
                    (0 until scale).forEach { dy ->
                        (0 until scale).forEach { dx ->
                            pixels[((y + quiet) * scale + dy) * side + (x + quiet) * scale + dx] = 0xFF000000.toInt()
                        }
                    }
                }
            }
        }

        val bitmap = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(side, side, pixels)))
        assertEquals(payload, QRCodeReader().decode(bitmap).text)
    }
}
