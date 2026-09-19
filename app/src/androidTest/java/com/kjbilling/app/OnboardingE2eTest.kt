package com.kjbilling.app

import androidx.compose.ui.test.*
import org.junit.Test

class OnboardingE2eTest : E2eBase() {

    override fun waitForLaunchScreen() = waitForText("Step 1 of 3")

    @Test
    fun emptyStep1ShowsValidationErrors() {
        assertTextExists("Step 1 of 3")
        clickText("Next")
        assertTextExists("Business Name is required")
        assertTextExists("Owner Name is required")
        assertTextExists("Valid 10-digit mobile required")
        assertTextExists("Step 1 of 3")
    }

    @Test
    fun mobileRejectsNonDigitsAndCapsAtTen() {
        setField("Mobile Number *", "1234567890")
        waitForText("1234567890")
        // 11th digit must be rejected
        fieldNode("Mobile Number *").performTextInput("9")
        composeRule.waitForIdle()
        assertTextCount("1234567890", 1)
        assertTextCount("12345678909", 0)
        // letters must be dropped entirely
        fieldNode("Mobile Number *").performTextClearance()
        fieldNode("Mobile Number *").performTextInput("abcdefgh")
        composeRule.waitForIdle()
        assertTextCount("abcdefgh", 0)
        // digits still accepted afterwards
        fieldNode("Mobile Number *").performTextInput("9876543210")
        waitForText("9876543210")
    }

    @Test
    fun completeGstOnboardingNavigatesToDashboard() {
        setField("Business Name *", "KJ Traders Pvt Ltd")
        setField("Owner Name *", "Sahil")
        setField("Mobile Number *", "9876543210")
        setField("Business Address", "42 MG Road, Pune")
        composeRule.onNodeWithText("State").performScrollTo().performClick()
        waitForText("Maharashtra")
        composeRule.onAllNodesWithText("Maharashtra").onLast().performClick()
        clickText("Next")
        waitForText("Step 2 of 3")

        composeRule.onNode(isToggleable()).performClick()
        waitForText("GSTIN")
        setField("GSTIN", DEFAULT_GSTIN)
        composeRule.onNodeWithText("Default GST Rate").performScrollTo().performClick()
        composeRule.onNodeWithText("28%").performClick()
        clickText("Next")
        waitForText("You're ready!")
        clickText("Create First Invoice")
        waitForContentDescription("New Invoice")
        assertTextExists("Recent Invoices")
        assertTextExists("No invoices yet")
    }

    @Test
    fun completeNonGstOnboardingNavigatesToDashboard() {
        setField("Business Name *", "Street Tuck Shop")
        setField("Owner Name *", "Ravi")
        setField("Mobile Number *", "9123456780")
        clickText("Next")
        waitForText("Step 2 of 3")
        // Leave GST switch off, skip GSTIN/rate.
        clickText("Next")
        waitForText("You're ready!")
        clickText("Create First Invoice")
        waitForContentDescription("New Invoice")
    }

    @Test
    fun invalidGstinShowsFieldError() {
        setField("Business Name *", "KJ Traders Pvt Ltd")
        setField("Owner Name *", "Sahil")
        setField("Mobile Number *", "9876543210")
        clickText("Next")
        waitForText("Step 2 of 3")
        composeRule.onNode(isToggleable()).performClick()
        waitForText("GSTIN")
        // 14 chars: too short + bad state code
        setField("GSTIN", "27AAPFU0939F1Z")
        assertTextExists("GSTIN must be 15 characters long")
        // 15 chars, invalid state code "99"
        setField("GSTIN", "99AAPFU0939F1ZV")
        assertTextExists("Invalid state code")
        // 15 chars, bad format (invalid GSTIN number component)
        setField("GSTIN", "27AAPFU0939F1AX")
        assertTextExists("Invalid GSTIN format")
        // valid
        setField("GSTIN", DEFAULT_GSTIN)
        assertTextAbsent("GSTIN must be 15 characters long")
        assertTextAbsent("Invalid state code")
        assertTextAbsent("Invalid GSTIN format")
        // clearing the field shows the blank-state error
        setField("GSTIN", "")
        assertTextExists("GSTIN cannot be empty")
    }
}