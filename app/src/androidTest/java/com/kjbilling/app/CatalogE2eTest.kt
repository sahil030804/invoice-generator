package com.kjbilling.app

import androidx.compose.ui.test.*
import org.junit.Test

class CatalogE2eTest : OnboardedBase() {

    @Test
    fun catalogListStates() {
        // Customers list is seeded with a required walk-in customer.
        clickBottomNav("Customers")
        waitForText("Walk-in Customer")
        pressSystemBack()
        clickBottomNav("Products")
        waitForText("No products yet")
    }

    @Test
    fun customerCrudAndDelete() {
        addCustomer("Acme Global", state = "Maharashtra", gstin = DEFAULT_GSTIN, businessName = "Acme Global Pvt Ltd", mobile = "9876543210")
        assertTextExists("Acme Global Pvt Ltd")

        // edit name
        clickText("Acme Global")
        waitForText("Edit Customer")
        setField("Name*", "Acme Global")
        clickText("Save")
        waitForText("Acme Global")

        // delete via swipe
        swipeToDelete("Acme Global")
        waitForText("Delete Customer")
        clickText("Delete")
        waitForText("Walk-in Customer")
        assertTextAbsent("Acme Global")
    }

    @Test
    fun customerSearchFiltersRows() {
        addCustomer("Acme Global")
        addCustomer("Beta Labs")
        clickBottomNav("Customers")
        waitForText("Acme Global")
        waitForText("Beta Labs")

        composeRule.onNode(hasSetTextAction()).performTextInput("Beta")
        composeRule.waitForIdle()
        waitForText("Beta Labs")
        assertTextAbsent("Acme Global")

        composeRule.onNode(hasSetTextAction()).performTextClearance()
        composeRule.waitForIdle()
        waitForText("Acme Global")
        waitForText("Beta Labs")
    }

    @Test
    fun customerValidationBlocksInvalidGstin() {
        clickBottomNav("Customers")
        waitForText("Walk-in Customer")
        composeRule.onNodeWithContentDescription("Add Customer").performClick()
        waitForText("New Customer")
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        setField("Name*", "Invalid GST Co")
        setField("GSTIN", "12345")
        assertTextExists("Invalid GSTIN format")
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        setField("GSTIN", DEFAULT_GSTIN)
        assertTextAbsent("Invalid GSTIN format")
        clickText("Save")
        waitForText("Invalid GST Co")
    }

    @Test
    fun productCrudAndDelete() {
        addProduct("Steel Rod", "250.00", unit = "KG", gst = "18", hsn = "7214")
        waitForText("₹250.00")
        assertTextExists("per KG")
        assertTextExists("HSN: 7214")

        // raise selling price via edit
        clickText("Steel Rod")
        waitForText("Edit Product")
        setField("Selling Price*", "300")
        clickText("Save")
        waitForText("Steel Rod")
        assertTextExists("₹300.00")
        assertTextAbsent("₹250.00")

        // delete via swipe
        swipeToDelete("Steel Rod")
        waitForText("Delete Product")
        clickText("Delete")
        waitForText("No products yet")
    }

    @Test
    fun productValidationRequiresNameAndPrice() {
        clickBottomNav("Products")
        waitForText("No products yet")
        composeRule.onNodeWithContentDescription("Add Product").performClick()
        waitForText("New Product")
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        setField("Product Name*", "Iron Ingot")
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        setField("Selling Price*", "125.50")
        clickText("Save")
        waitForText("Iron Ingot")
        assertTextExists("per PCS")
    }
}