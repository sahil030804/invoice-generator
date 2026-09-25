package com.kjbilling.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageSizingTest {

    @Test
    fun sampleSize_smallImage_returnsOne() {
        assertEquals(1, ImageSizing.sampleSize(400, 300, maxDimension = 512))
    }

    @Test
    fun sampleSize_largeImage_returnsPowerOfTwoKeepingAtLeastTarget() {
        // 4000 / 4 = 1000 >= 512, 4000 / 8 = 500 < 512 → 4
        assertEquals(4, ImageSizing.sampleSize(4000, 3000, maxDimension = 512))
    }

    @Test
    fun sampleSize_invalidDimensions_returnsOne() {
        assertEquals(1, ImageSizing.sampleSize(0, -1, maxDimension = 512))
    }

    @Test
    fun scaleToMax_withinLimit_unchanged() {
        assertEquals(200 to 100, ImageSizing.scaleToMax(200, 100, maxDimension = 512))
    }

    @Test
    fun scaleToMax_wideImage_limitsWidth() {
        assertEquals(512 to 256, ImageSizing.scaleToMax(1024, 512, maxDimension = 512))
    }

    @Test
    fun scaleToMax_tallImage_limitsHeight() {
        assertEquals(128 to 512, ImageSizing.scaleToMax(250, 1000, maxDimension = 512))
    }

    @Test
    fun fitInside_wideLogo_limitedByWidth() {
        val (w, h) = ImageSizing.fitInside(400, 100, boxWidth = 80f, boxHeight = 40f)
        assertEquals(80f, w, 0.001f)
        assertEquals(20f, h, 0.001f)
    }

    @Test
    fun fitInside_squareLogo_limitedByHeight() {
        val (w, h) = ImageSizing.fitInside(300, 300, boxWidth = 80f, boxHeight = 40f)
        assertEquals(40f, w, 0.001f)
        assertEquals(40f, h, 0.001f)
    }

    @Test
    fun fitInside_invalidSize_returnsZero() {
        assertEquals(0f to 0f, ImageSizing.fitInside(0, 100, boxWidth = 80f, boxHeight = 40f))
    }
}
