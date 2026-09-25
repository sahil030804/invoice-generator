package com.kjbilling.app

import androidx.compose.ui.test.*
import org.junit.Test

/** UPI "Scan to pay": save a UPI ID, then see the QR on an unpaid invoice and on the standalone screen. */
class UpiQrE2eTest : OnboardedBase() {

    private fun openBusinessProfile() {
        clickBottomNav("Settings")
        waitForText("Business Profile")
        clickText("Business Profile")
        waitForText("UPI ID")
    }

    private fun saveUpiId(upiId: String) {
        openBusinessProfile()
        setField("UPI ID", upiId)
        clickText("Save")
        waitForText("Invoice Prefix")
    }

    @Test
    fun invalidUpiId_showsError_andBlocksSave() {
        openBusinessProfile()
        setField("UPI ID", "shop@gmail.com")

        waitForText("Enter a valid UPI ID, like name@okaxis")
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
    }

    @Test
    fun unpaidInvoice_showsScanToPayQr_forBalance() {
        saveUpiId("kjtraders@okaxis")
        pressSystemBack()
        waitForText("Recent Invoices")

        openNewInvoice()
        addManualItem("LED Bulb", "1", "100", disc = "0", gst = "18")
        generateAndDownload()
        assertPdfOnDisk(exact = 1, containsInvoiceNumber = "INV-0001")

        clickText("INV-0001")
        waitForText("Show UPI QR")
        clickText("Show UPI QR")
        waitForText("Scan to pay ₹118.00")
        waitForContentDescription("UPI QR code")
        assertTextExists("UPI: kjtraders@okaxis")
        clickText("Close")
    }

    @Test
    fun standaloneQr_forTypedAmount() {
        saveUpiId("kjtraders@okaxis")
        clickText("Business Profile")
        waitForText("Preview my UPI QR")
        clickText("Preview my UPI QR")
        waitForText("Enter amount to collect")

        composeRule.onNodeWithTag("keypad-1").performClick()
        composeRule.onNodeWithTag("keypad-0").performClick()
        composeRule.onNodeWithTag("keypad-0").performClick()
        clickText("Show QR")

        waitForText("Scan to pay ₹100.00")
        waitForContentDescription("UPI QR code")
    }
}
