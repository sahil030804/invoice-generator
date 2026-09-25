package com.kjbilling.app

import androidx.compose.ui.test.*
import org.junit.Test

/** History: filter chips and day grouping. */
class HistoryFilterE2eTest : OnboardedBase() {

    private fun chip(label: String) = composeRule.onNode(hasText(label) and hasClickAction())

    private fun makePaidQuickBillAndUnpaidInvoice() {
        clickText("⚡ QUICK COUNTER BILL")
        waitForText("⚡ Quick Counter Bill")
        clickText("Custom amount")
        waitForText("Add custom amount")
        composeRule.onNodeWithTag("keypad-5").performClick()
        composeRule.onNodeWithTag("keypad-0").performClick()
        clickText("Add to bill")
        clickText("Generate Bill")
        waitForText("Bill INV-0001 Ready!")
        clickText("⚡ START NEXT BILL")
        pressSystemBack()
        waitForText("Recent Invoices")

        openNewInvoice()
        addManualItem("Pipes", "1", "20", disc = "0", gst = "0")
        generateAndDownload()
    }

    @Test
    fun filters_unpaidHidesPaid_allBringsItBack() {
        makePaidQuickBillAndUnpaidInvoice()
        clickBottomNav("History")
        waitForText("Invoice History")
        waitForText("INV-0001")
        waitForText("INV-0002")

        chip("Unpaid").performClick()
        waitForText("INV-0002")
        assertTextAbsent("INV-0001")

        chip("All").performClick()
        waitForText("INV-0001")
        waitForText("INV-0002")
    }

    @Test
    fun todayFilter_listsTodaysBills_andGroupsUnderTodayHeader() {
        makePaidQuickBillAndUnpaidInvoice()
        clickBottomNav("History")
        waitForText("Invoice History")

        // The header "Today" is plain text; the chip is the clickable one.
        composeRule.onAllNodesWithText("Today").assertCountEquals(2)
        chip("Today").performClick()
        waitForText("INV-0001")
        waitForText("INV-0002")
    }

    @Test
    fun emptyFilterResult_showsNoInvoicesFound() {
        clickBottomNav("History")
        waitForText("No invoices yet")
        chip("Unpaid").performClick()
        waitForText("No invoices found")
    }
}
