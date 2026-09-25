package com.kjbilling.app.domain.upi

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/** One horizontal stretch of dark modules in a QR row: [start, endExclusive). */
data class QrRun(val row: Int, val start: Int, val endExclusive: Int)

/** Square QR module grid without quiet zone. Drawn as rectangles, so it stays crisp at any size. */
class QrMatrix(val size: Int, private val dark: BooleanArray) {

    fun isDark(x: Int, y: Int): Boolean = dark[y * size + x]

    /** Row-wise runs of dark modules; lets renderers draw one rect per run instead of per module. */
    fun darkRuns(): List<QrRun> {
        val runs = mutableListOf<QrRun>()
        for (y in 0 until size) {
            var x = 0
            while (x < size) {
                if (!isDark(x, y)) {
                    x++
                    continue
                }
                val start = x
                while (x < size && isDark(x, y)) {
                    x++
                }
                runs.add(QrRun(y, start, x))
            }
        }
        return runs
    }
}

/** Encodes text into a QR module matrix (ZXing core, fully offline). */
object QrEncoder {

    /** Standard QR quiet zone, in modules; renderers must leave this much white around the code. */
    const val QUIET_ZONE_MODULES = 4

    fun encode(content: String): QrMatrix {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 0,
            EncodeHintType.CHARACTER_SET to Charsets.UTF_8.name()
        )
        // Width/height 0 = minimal size, i.e. exactly one pixel per module.
        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, hints)
        val size = bits.width
        val dark = BooleanArray(size * size) { i -> bits.get(i % size, i / size) }
        return QrMatrix(size, dark)
    }
}
