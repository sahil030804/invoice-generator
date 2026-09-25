package com.kjbilling.app.util

/**
 * Pure size math for images (no Android deps, unit-testable).
 */
object ImageSizing {

    /**
     * Largest power-of-two BitmapFactory sample size that still keeps the
     * longest side >= [maxDimension]. Returns 1 for invalid or small images.
     */
    fun sampleSize(width: Int, height: Int, maxDimension: Int): Int {
        if (width <= 0 || height <= 0 || maxDimension <= 0) {
            return 1
        }

        val longest = maxOf(width, height)
        var sample = 1
        while (longest / (sample * 2) >= maxDimension) {
            sample *= 2
        }
        return sample
    }

    /**
     * Scales (width, height) down so the longest side is at most [maxDimension],
     * preserving aspect ratio. Never upscales.
     */
    fun scaleToMax(width: Int, height: Int, maxDimension: Int): Pair<Int, Int> {
        val longest = maxOf(width, height)
        if (longest <= maxDimension || width <= 0 || height <= 0) {
            return width to height
        }

        val ratio = maxDimension.toFloat() / longest
        val w = (width * ratio).toInt().coerceAtLeast(1)
        val h = (height * ratio).toInt().coerceAtLeast(1)
        return w to h
    }

    /**
     * Aspect-fit (width, height) inside a box. Returns (0, 0) for invalid input.
     */
    fun fitInside(width: Int, height: Int, boxWidth: Float, boxHeight: Float): Pair<Float, Float> {
        if (width <= 0 || height <= 0 || boxWidth <= 0f || boxHeight <= 0f) {
            return 0f to 0f
        }

        val scale = minOf(boxWidth / width, boxHeight / height)
        return (width * scale) to (height * scale)
    }
}
