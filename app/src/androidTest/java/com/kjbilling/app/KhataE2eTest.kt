package com.kjbilling.app

import androidx.compose.ui.test.*
import com.kjbilling.app.data.db.AppDatabase
import com.kjbilling.app.data.db.entity.CustomerEntity
import org.junit.Test

/** Udhaar (pay later) → customer's Khata → money received clears the due. */
class KhataE2eTest : OnboardedBase() {

    override suspend fun seed(db: AppDatabase) {
        super.seed(db)
        val now = System.currentTimeMillis()
        db.customerDao().insert(
            CustomerEntity(
                name = "Rakesh Sharma", mobile = "9876500000", email = null, billingAddress = null, state = "Maharashtra",
                pincode = null, gstin = null, businessName = null, notes = null, isWalkIn = false,
                lastUsedAt = now, createdAt = now, updatedAt = now
            )
        )
    }

    private fun openQuickBill() {
        clickText("⚡ QUICK COUNTER BILL")
        waitForText("⚡ Quick Counter Bill")
    }

    private fun billOnUdhaar(vararg digits: Int) {
        openQuickBill()
        clickText("Custom amount")
        waitForText("Add custom amount")
        digits.forEach { composeRule.onNodeWithTag("keypad-$it").performClick() }
        clickText("Add to bill")
        clickText("Rakesh Sharma")
        clickText("Udhaar")
        clickText("Generate Bill")
    }

    @Test
    fun udhaarBill_appearsInKhata_andReceivedClearsIt() {
        billOnUdhaar(1, 1, 8)
        waitForText("₹118.00 (Udhaar - added to Khata)")
        clickText("⚡ START NEXT BILL")
        pressSystemBack()
        waitForText("Recent Invoices")

        // Dashboard Pending and today's sales both show the unpaid ₹118.
        assertTextCount("₹118.00", 3)

        clickBottomNav("Customers")
        waitForText("₹118.00 due")
        clickText("₹118.00 due")
        waitForText("Total due")
        waitForText("Unpaid bills")

        clickText("Received")
        waitForText("Money received")
        clickText("Save")

        waitForText("No pending dues")
        clickBack()
        waitForText("Rakesh Sharma")
        assertTextAbsent("₹118.00 due")
    }

    @Test
    fun customerRow_stillOpensEditCustomer() {
        billOnUdhaar(5, 0)
        waitForText("₹50.00 (Udhaar - added to Khata)")
        clickText("⚡ START NEXT BILL")
        pressSystemBack()
        waitForText("Recent Invoices")

        clickBottomNav("Customers")
        waitForText("₹50.00 due")
        clickText("Rakesh Sharma")
        waitForText("Edit Customer")
    }

    @Test
    fun withDuesFilter_listsOnlyCustomersWhoOwe() {
        billOnUdhaar(5, 0)
        waitForText("₹50.00 (Udhaar - added to Khata)")
        clickText("⚡ START NEXT BILL")
        pressSystemBack()
        waitForText("Recent Invoices")

        clickBottomNav("Customers")
        waitForText("Walk-in Customer")
        composeRule.onNode(hasText("With dues", substring = true) and hasClickAction()).performClick()
        waitForText("Rakesh Sharma")
        assertTextAbsent("Walk-in Customer")
    }
}
