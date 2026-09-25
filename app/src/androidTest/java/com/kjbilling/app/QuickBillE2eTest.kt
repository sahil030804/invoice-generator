package com.kjbilling.app

import androidx.compose.ui.test.*
import org.junit.Test

/** Counter-sale flow: product tap → Generate Bill → success screen → next bill. */
class QuickBillE2eTest : OnboardedBase() {

    private fun openQuickBill() {
        clickText("⚡ QUICK COUNTER BILL")
        waitForText("⚡ Quick Counter Bill")
    }

    @Test
    fun quickBillCreatesPaidInvoiceAndNextBillGetsNextNumber() {
        addProduct("Bulb 9W", "100", unit = "PCS", gst = "18", hsn = "8539")
        pressSystemBack()
        waitForText("Recent Invoices")

        openQuickBill()
        waitForText("Bulb 9W")
        clickText("+")
        waitForText("1 item selected")
        clickText("Generate Bill")

        waitForText("Bill INV-0001 Ready!")
        waitForText("₹118.00 (Paid in Cash)")
        assertPdfOnDisk(exact = 1, containsInvoiceNumber = "INV-0001")

        // second bill continues the numbering
        clickText("⚡ START NEXT BILL")
        waitForText("⚡ Quick Counter Bill")
        waitForText("Bulb 9W")
        clickText("+")
        clickText("+")
        waitForText("2 items selected")
        clickText("Generate Bill")
        waitForText("Bill INV-0002 Ready!")
        waitForText("₹236.00 (Paid in Cash)")
    }

    @Test
    fun quickBillWithoutItemsCannotGenerate() {
        openQuickBill()
        waitForText("0 items selected")
        composeRule.onNodeWithText("Generate Bill").assertIsNotEnabled()
    }
}
