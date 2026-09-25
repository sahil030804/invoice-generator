package com.kjbilling.app.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.kjbilling.app.domain.upi.QrEncoder
import com.kjbilling.app.domain.upi.QrMatrix

/**
 * Draws a QR code as vector rectangles (one per dark run) inside a white square that
 * includes the standard quiet zone, so it prints crisply and scans from paper.
 */
object QrPdfPainter {

    private val whitePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val darkPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        isAntiAlias = false
    }

    /** [size] is the full square including quiet zone, in PDF points. */
    fun draw(canvas: Canvas, matrix: QrMatrix, left: Float, top: Float, size: Float) {
        val modules = matrix.size + 2 * QrEncoder.QUIET_ZONE_MODULES
        val module = size / modules
        val origin = QrEncoder.QUIET_ZONE_MODULES * module

        canvas.drawRect(left, top, left + size, top + size, whitePaint)
        matrix.darkRuns().forEach { run ->
            canvas.drawRect(
                left + origin + run.start * module,
                top + origin + run.row * module,
                left + origin + run.endExclusive * module,
                top + origin + (run.row + 1) * module,
                darkPaint
            )
        }
    }
}
