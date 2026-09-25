package com.kjbilling.app

import androidx.compose.ui.test.*
import com.kjbilling.app.data.db.AppDatabase
import org.junit.Test

/** Quick Bill: GST-inclusive custom amounts and Cash / UPI / Udhaar payment modes. */
class QuickBillModesE2eTest : OnboardedBase() {

    override suspend fun seed(db: AppDatabase) {
        super.seed(db)
        val dao = db.businessProfileDao()
        dao.getProfileOnce()?.let { dao.upsert(it.copy(upiId = "kjtraders@okaxis")) }
    }

    private fun openQuickBill() {
        clickText("⚡ QUICK COUNTER BILL")
        waitForText("⚡ Quick Counter Bill")
    }

    private fun addCustomAmount(vararg digits: Int) {
        clickText("Custom amount")
        waitForText("Add custom amount")
        digits.forEach { composeRule.onNodeWithTag("keypad-$it").performClick() }
        clickText("Add to bill")
        composeRule.waitForIdle()
    }

    @Test
    fun customAmount_isChargedExactly_evenWithGst() {
        openQuickBill()
        addCustomAmount(5, 0)

        waitForText("1 item selected")
        waitForText("Misc item · ₹50.00")
        clickText("Generate Bill")
        waitForText("₹50.00 (Paid in Cash)")
        assertPdfOnDisk(exact = 1, containsInvoiceNumber = "INV-0001")
    }

    @Test
    fun customAmount_plusCatalogProduct() {
        addProduct("Bulb 9W", "100", unit = "PCS", gst = "18", hsn = "8539")
        pressSystemBack()
        waitForText("Recent Invoices")

        openQuickBill()
        waitForText("Bulb 9W")
        clickText("+")
        addCustomAmount(5, 0)

        waitForText("2 items selected")
        clickText("Generate Bill")
        waitForText("₹168.00 (Paid in Cash)")
    }

    @Test
    fun customAmount_canBeRemoved() {
        openQuickBill()
        addCustomAmount(2, 5)
        waitForText("1 item selected")

        clickContentDescription("Remove Misc item")
        waitForText("0 items selected")
        composeRule.onNodeWithText("Generate Bill").assertIsNotEnabled()
    }

    @Test
    fun upiMode_showsScanToPayOnSuccess() {
        openQuickBill()
        addCustomAmount(1, 1, 8)
        clickText("UPI")

        clickText("Generate Bill")
        waitForText("₹118.00 (Paid by UPI)")
        waitForText("Scan to pay ₹118.00")
        waitForContentDescription("UPI QR code")
    }

    @Test
    fun udhaar_needsACustomer() {
        openQuickBill()
        addCustomAmount(1, 0)
        clickText("Udhaar")

        waitForText("Select a customer for Udhaar")
        composeRule.onNodeWithText("Generate Bill").assertIsNotEnabled()
    }
}
