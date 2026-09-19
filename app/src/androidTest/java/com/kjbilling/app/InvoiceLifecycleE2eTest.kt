package com.kjbilling.app

import androidx.compose.ui.test.*
import org.junit.Test

class InvoiceLifecycleE2eTest : OnboardedBase() {

    @Test
    fun partialThenFullPaymentUpdatesStatusesAndStats() {
        openNewInvoice()
        addManualItem("Steel Fasteners", "10", "120", disc = "0", gst = "18", hsn = "7318")
        waitForText("₹1,416.00")
        generateAndDownload()
        assertPdfOnDisk(exact = 1, containsInvoiceNumber = "INV-0001")

        // open detail: generated + unpaid
        clickText("INV-0001")
        waitForText("Record Payment")
        waitForText("Balance: ₹1,416.00")
        assertTextCount("GENERATED", 1)
        assertTextCount("UNPAID", 1)

        // partial payment of 500
        clickText("Record Payment")
        waitForText("Amount (Balance 1416)")
        setFieldNoScroll("Amount (Balance 1416)", "500")
        clickText("Save")
        waitForText("Balance: ₹916.00")
        assertTextExists("Paid: ₹500.00")
        assertTextExists("PARTIAL")
        assertTextExists("GENERATED")
        assertTextCount("₹500.00", 1)

        // full payment clears the balance
        clickText("Record Payment")
        waitForText("Amount (Balance 916)")
        setFieldNoScroll("Amount (Balance 916)", "916")
        clickText("Save")
        waitForText("Balance: ₹0.00")
        assertTextCount("PAID", 2)
        assertTextCount("₹0.00", 1) // Balance Due row in TotalCard

        // payment + cancel buttons hidden once paid
        assertTextAbsent("Record Payment")
        assertTextAbsent("Cancel Invoice")

        clickBack()
        waitForText("Recent Invoices")
        // today's sales stat + recent card both show the 1416 total
        assertTextCount("₹1,416.00", 2)
        assertTextExists("₹0.00") // Pending amount
    }

    @Test
    fun cancelInvoiceRemovesItFromStatsAndHidesActions() {
        openNewInvoice()
        addManualItem("Packing Tape", "5", "20", disc = "0", gst = "18")
        waitForText("₹118.00")
        generateAndDownload()
        assertPdfOnDisk(exact = 1, containsInvoiceNumber = "INV-0001")

        clickText("INV-0001")
        waitForText("Record Payment")
        clickLast("Cancel Invoice")
        // dialog title + confirm + screen button all share the label; confirm is last
        composeRule.waitUntil(10_000L) {
            composeRule.onAllNodesWithText("Cancel Invoice").fetchSemanticsNodes().size == 3
        }
        composeRule.onAllNodesWithText("Cancel Invoice").onLast().performClick()

        waitForText("CANCELLED")
        assertTextAbsent("Record Payment")
        assertTextAbsent("Cancel Invoice")

        clickBack()
        waitForText("Recent Invoices")
        // cancelled invoice excluded from both stats -> today's and pending both 0
        assertTextCount("₹0.00", 2)
        assertTextExists("₹118.00") // recent card still shows the amount
    }

    @Test
    fun settingsPrefixAndNumberFlowToNewInvoices() {
        openNewInvoice()
        addManualItem("Pipes", "1", "10", disc = "0", gst = "0")
        waitForText("₹10.00")
        generateAndDownload() // INV-0001
        assertPdfOnDisk(exact = 1, containsInvoiceNumber = "INV-0001")

        // switch prefix to BILL- and next number to 3
        clickBottomNav("Settings")
        waitForText("Invoice Prefix")
        setField("Invoice Prefix", "BILL-")
        setField("Starting Number", "3")
        composeRule.waitForIdle()

        pressSystemBack()
        waitForText("Recent Invoices")
        openNewInvoice()
        addManualItem("Valves", "2", "10", disc = "0", gst = "0")
        waitForText("₹20.00")
        generateAndDownload() // BILL-0003
        openNewInvoice()
        addManualItem("Gaskets", "1", "10", disc = "0", gst = "0")
        waitForText("₹10.00")
        generateAndDownload() // BILL-0004

        // INV-0001 is also unpaid today, so stats total all three: 10 + 20 + 10
        assertTextCount("₹40.00", 2)

        // history lists both under the new numbering
        clickBottomNav("History")
        waitForText("Invoice History")
        assertTextExists("BILL-0003")
        assertTextExists("BILL-0004")
        assertTextExists("₹10.00")
        assertTextExists("₹20.00")

        // search narrows the list
        val search = composeRule.onNode(hasSetTextAction())
        search.performTextInput("BILL-0004")
        composeRule.waitForIdle()
        assertTextAbsent("BILL-0003")

        search.performTextClearance()
        search.performTextInput("zzz")
        composeRule.waitForIdle()
        waitForText("No invoices found")
        assertTextAbsent("BILL-0003")
        assertTextAbsent("BILL-0004")
    }
}

class NoGstInvoiceE2eTest : OnboardedBase(gstEnabled = false) {

    @Test
    fun noGstInvoiceHasZeroTax() {
        openNewInvoice()
        addManualItem("Drinks", "5", "40", disc = "0", gst = "0")
        waitForText("₹200.00")
        // header total + subtotal + grand total all equal 200
        assertTextCount("₹200.00", 3)
        assertTextExists("₹0.00") // tax row
        generateAndDownload()
        assertPdfOnDisk(exact = 1, containsInvoiceNumber = "INV-0001")
    }
}