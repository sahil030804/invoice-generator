package com.kjbilling.app

import androidx.compose.ui.test.*
import org.junit.Test

class InvoiceGenerationE2eTest : OnboardedBase() {

    @Test
    fun singleItemGstInvoiceTotalsAndPdf() {
        openNewInvoice()
        addManualItem("Premium Widgets", "7", "650.25", disc = "0", gst = "18", hsn = "6204")
        waitForText("₹5,371.07")
        // item header total + subtotal row both equal 7 * 650.25
        assertTextCount("₹4,551.75", 2)
        assertTextExists("₹819.32")
        assertTextExists("₹5,371.07")

        generateAndDownload("INV-0001 • Walk-in Customer • ₹5,371.07")
        assertPdfOnDisk(exact = 1, containsInvoiceNumber = "INV-0001")
    }

    @Test
    fun createScreenValidationEdges() {
        openNewInvoice()
        composeRule.onNodeWithText("Generate Invoice").assertIsNotEnabled()
        assertTextExists("No items added")

        clickText("+ Add Empty Item")
        composeRule.waitForIdle()
        clickText("Generate Invoice")
        waitForText("Please fill name and price for all items")

        setField("Item Name", "Widget")
        setField("Price", "10")
        setField("Disc %", "150")
        clickText("Generate Invoice")
        waitForText("Check qty (>0), price (>0), discount (0-100), GST (≥0) for all items")

        setField("Disc %", "0")
        setField("Qty", "0")
        clickText("Generate Invoice")
        waitForText("Check qty (>0), price (>0), discount (0-100), GST (≥0) for all items")

        setField("Qty", "1")
        setField("Price", "0")
        clickText("Generate Invoice")
        waitForText("Check qty (>0), price (>0), discount (0-100), GST (≥0) for all items")

        setField("Price", "10")
        clickText("Generate Invoice")
        waitForText("Invoice Generated Successfully!")
        assertPdfOnDisk(exact = 1, containsInvoiceNumber = "INV-0001")
    }

    @Test
    fun multiItemMixedGstRatesWithCatalogProduct() {
        addProduct("Cables (per meter)", "500", unit = "MTR", gst = "5", hsn = "8544")
        pressSystemBack()
        openNewInvoice()
        addManualItem("Software License", "3", "1000", disc = "0", gst = "18", hsn = "8527")
        addManualItem("Setup Consulting", "10", "250.50", disc = "10", gst = "18", hsn = "9983")
        addProductItem("Cables (per meter)", qty = "0.5", disc = "0", gst = "5")
        addManualItem("Warranty", "1", "300", disc = "100", gst = "18")
        addManualItem("Training", "2", "750.25", disc = "0", gst = "28")

        // Tax rounds per line, hence .46 rather than .45.
        waitForText("₹8,383.46")
        assertTextExists("₹7,005.00")
        assertTextExists("-₹550.50")
        assertTextExists("₹1,378.46")
        assertTextExists("₹8,383.46")

        generateAndDownload("INV-0001 • Walk-in Customer • ₹8,383.46")
        assertPdfOnDisk(exact = 1, containsInvoiceNumber = "INV-0001")
    }

    @Test
    fun igstForInterstateCustomer() {
        addCustomer("Gujarat Buyer", state = "Gujarat", gstin = "24AAPFU0939F1ZV")
        pressSystemBack()
        openNewInvoice()
        clickText("Change")
        waitForText("Select Customer")
        clickLast("Gujarat Buyer")
        composeRule.waitForIdle()
        waitForText("Gujarat Buyer")

        addManualItem("Machinery Spares", "2", "100", disc = "0", gst = "18", hsn = "8409")
        waitForText("₹236.00")
        generateAndDownload()
        assertPdfOnDisk(exact = 1, containsInvoiceNumber = "INV-0001")
    }

    @Test
    fun largeAmountUsesIndianGrouping() {
        openNewInvoice()
        addManualItem("Heavy Machinery", "2", "500000", disc = "0", gst = "18", hsn = "8428")
        waitForText("₹11,80,000.00")
        generateAndDownload("INV-0001 • Walk-in Customer • ₹11,80,000.00")
        assertPdfOnDisk(exact = 1, containsInvoiceNumber = "INV-0001")
    }

    @Test
    fun tenItemsSinglePageInvoicePdf() {
        openNewInvoice()
        for (i in 1..10) {
            addManualItem("Product Item $i", "$i", "100.00", disc = "0", gst = "18", hsn = "840$i")
        }
        clickText("Generate Invoice")
        waitForText("Invoice Generated Successfully!")
        clickText("Done")
        waitForText("Recent Invoices")
        assertPdfOnDisk(exact = 1)
    }
}