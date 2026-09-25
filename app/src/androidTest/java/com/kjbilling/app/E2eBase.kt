package com.kjbilling.app

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kjbilling.app.data.db.AppDatabase
import com.kjbilling.app.data.db.entity.AppSettingsEntity
import com.kjbilling.app.data.db.entity.BusinessProfileEntity
import com.kjbilling.app.data.db.entity.CustomerEntity
import com.kjbilling.app.domain.model.PaymentStatus
import com.kjbilling.app.domain.model.TaxType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import java.io.File
import java.math.BigDecimal

const val DEFAULT_GSTIN = "27AAPFU0939F1ZV"

abstract class E2eBase {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val app: Context get() = ApplicationProvider.getApplicationContext()

    @Before
    fun resetData() {
        runBlocking {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(app)
                db.clearAllTables()
                seed(db)
            }
        }
        File(app.filesDir, "invoices").deleteRecursively()
        // No recreate: the app collects settings as a Flow. Clearing + reseeding
        // nulls settings briefly (blank screen), then re-emits the fresh state and
        // AppNavigation builds its nav graph with the correct start destination.
        // (ActivityScenario.recreate() leaves the activity STOPPED on this device.)
        waitForLaunchScreen()
    }

    protected open suspend fun seed(db: AppDatabase) {
        val now = System.currentTimeMillis()
        db.appSettingsDao().upsert(
            AppSettingsEntity(
                id = 1L,
                gstEnabled = true,
                defaultGstRate = BigDecimal("18.00"),
                defaultTaxType = TaxType.CGST_SGST,
                invoicePrefix = "INV-",
                nextInvoiceNumber = 1L,
                onboardingCompleted = false,
                defaultPaymentStatus = PaymentStatus.UNPAID
            )
        )
        db.customerDao().insert(
            CustomerEntity(
                name = "Walk-in Customer",
                mobile = null,
                email = null,
                billingAddress = null,
                state = null,
                pincode = null,
                gstin = null,
                businessName = null,
                notes = null,
                isWalkIn = true,
                lastUsedAt = now,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    protected abstract fun waitForLaunchScreen()

    // ---------- generic UI helpers ----------

protected fun waitForText(text: String, timeoutMillis: Long = 30_000L) {
        composeRule.waitUntil(timeoutMillis) {
            try {
                composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
            } catch (t: IllegalStateException) {
                // Compose hierarchy not registered yet (activity still starting).
                false
            }
        }
    }

    protected fun waitForContentDescription(contentDescription: String, timeoutMillis: Long = 30_000L) {
        composeRule.waitUntil(timeoutMillis) {
            try {
                composeRule.onAllNodesWithContentDescription(contentDescription)
                    .fetchSemanticsNodes().isNotEmpty()
            } catch (t: IllegalStateException) {
                // Compose hierarchy not registered yet (activity still starting).
                false
            }
        }
    }

    protected fun clickContentDescription(contentDescription: String) {
        val node = composeRule.onNodeWithContentDescription(contentDescription)
        scrollIntoViewIfPossible(node)
        node.performClick()
    }

    protected fun assertTextCount(text: String, expected: Int) {
        composeRule.onAllNodesWithText(text).assertCountEquals(expected)
    }

    protected fun assertTextExists(text: String) = assertTextCount(text, 1)

    protected fun assertTextAbsent(text: String) = assertTextCount(text, 0)

    /**
     * Editable node whose (merged or descendant) text matches [label].
     * Covers both OutlinedTextField behaviors: label merged into the field
     * node, or label rendered as a separate descendant text.
     */
    protected fun fieldNode(label: String) = composeRule.onNode(
        hasSetTextAction() and (hasText(label) or hasAnyDescendant(hasText(label)))
    )

    protected fun setField(label: String, value: String) {
        val target = fieldNode(label)
        target.performScrollTo()
        target.performTextClearance()
        target.performTextInput(value)
    }

    /** Like [setField] but for fields inside dialogs with no scrollable ancestor. */
    protected fun setFieldNoScroll(label: String, value: String) {
        val target = fieldNode(label)
        target.performTextClearance()
        target.performTextInput(value)
    }

    protected fun clickText(text: String) {
        val node = composeRule.onNodeWithText(text)
        scrollIntoViewIfPossible(node)
        node.performClick()
    }

    protected fun clickLast(text: String) {
        val node = composeRule.onAllNodesWithText(text).onLast()
        scrollIntoViewIfPossible(node)
        node.performClick()
    }

    private fun scrollIntoViewIfPossible(node: SemanticsNodeInteraction) {
        try {
            node.performScrollTo()
        } catch (t: AssertionError) {
            // Node is already visible without a scrollable ancestor.
        }
    }

    protected fun clickBottomNav(label: String) {
        // Screen titles can repeat the label (e.g. "Customers"); the bottom bar is the last match.
        composeRule.onAllNodesWithText(label).onLast().performClick()
    }

    protected fun clickBack() {
        composeRule.onNodeWithContentDescription("Back").performClick()
    }

    protected fun pressSystemBack() {
        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()
    }

    protected fun swipeToDelete(text: String) {
        composeRule.onNodeWithText(text).performTouchInput { swipeLeft() }
    }

    protected fun openNewInvoice() {
        clickContentDescription("New Invoice")
        waitForText("Generate Invoice")
        waitForText("Walk-in Customer")
    }

    /** Returns index of the newly appended item (used for expand/collapse button index). */
    protected fun itemCount(): Int =
        composeRule.onAllNodesWithContentDescription("Expand").fetchSemanticsNodes().size

    protected fun addManualItem(
        name: String,
        qty: String,
        price: String,
        disc: String = "0",
        gst: String = "18",
        hsn: String = ""
    ) {
        val idx = itemCount()
        clickText("+ Add Empty Item")
        composeRule.waitForIdle()
        setField("Item Name", name)
        setField("Qty", qty)
        setField("Price", price)
        setField("Disc %", disc)
        setField("GST %", gst)
        if (hsn.isNotBlank()) setField("HSN Code", hsn)
        // Collapse so the next add keeps exactly one expanded editor.
        composeRule.onAllNodesWithContentDescription("Expand")[idx].performScrollTo().performClick()
        composeRule.waitForIdle()
    }

    protected fun addProductItem(
        productName: String,
        qty: String? = null,
        disc: String? = null,
        gst: String? = null
    ) {
        val idx = itemCount()
        clickText("+ Add Product")
        waitForText("Select Product")
        clickLast(productName)
        composeRule.waitForIdle()
        // New product-driven editor is collapsed; expand to edit.
        composeRule.onAllNodesWithContentDescription("Expand")[idx].performScrollTo().performClick()
        composeRule.waitForIdle()
        if (!qty.isNullOrBlank()) {
            setField("Qty", qty)
        }
        if (!disc.isNullOrBlank()) {
            setField("Disc %", disc)
        }
        if (!gst.isNullOrBlank()) {
            setField("GST %", gst)
        }
        composeRule.onAllNodesWithContentDescription("Expand")[idx].performScrollTo().performClick()
        composeRule.waitForIdle()
    }

    protected fun addCustomer(
        name: String,
        state: String = "",
        gstin: String = "",
        businessName: String = "",
        mobile: String = ""
    ) {
        clickBottomNav("Customers")
        composeRule.onNodeWithContentDescription("Add Customer").performClick()
        waitForText("New Customer")
        setField("Name*", name)
        if (businessName.isNotBlank()) setField("Business Name", businessName)
        if (mobile.isNotBlank()) setField("Mobile", mobile)
        if (state.isNotBlank()) selectState(state)
        if (gstin.isNotBlank()) setField("GSTIN", gstin)
        clickText("Save")
        waitForText(name)
    }

    /** State is a read-only dropdown: open it, then pick the option. */
    protected fun selectState(state: String) {
        val field = composeRule.onNodeWithText("State")
        field.performScrollTo()
        field.performClick()
        composeRule.onNodeWithText(state).performClick()
    }

    protected fun addProduct(
        name: String,
        price: String,
        unit: String = "PCS",
        gst: String = "Use Default",
        hsn: String = ""
    ) {
        clickBottomNav("Products")
        composeRule.onNodeWithContentDescription("Add Product").performClick()
        waitForText("New Product")
        setField("Product Name*", name)
        setField("Selling Price*", price)
        composeRule.onNodeWithText("Unit").performScrollTo().performClick()
        composeRule.onAllNodesWithText(unit).onLast().performClick()
        composeRule.onNodeWithText("GST Rate (%)").performScrollTo().performClick()
        if (gst != "Use Default") {
            composeRule.onNodeWithText(gst).performClick()
        }
        if (hsn.isNotBlank()) setField("HSN Code", hsn)
        clickText("Save")
        waitForText(name)
    }

    protected fun generateAndDownload(footer: String? = null, timeoutMillis: Long = 45_000L) {
        clickText("Generate Invoice")
        waitForText("Invoice Generated Successfully!")
        footer?.let { waitForText(it) }
        // The success snackbar lives only briefly and the test's virtual clock can
        // skip past its display window. MediaStore finalization (IS_PENDING=0) is the
        // deterministic signal that the PDF actually reached public Downloads, so we
        clickText("Done")
        waitForText("Recent Invoices")
    }

    protected fun assertPdfOnDisk(exact: Int? = null, containsInvoiceNumber: String? = null) {
        val dir = File(app.filesDir, "invoices")
        val files = dir.listFiles()?.filter { it.isFile } ?: emptyList()
        if (exact != null) {
            assertTrue("expected $exact PDF(s) in ${dir.absolutePath}, found ${files.size}: ${files.joinToString { it.name }}", files.size == exact)
        }
        assertTrue("no PDF files generated in ${dir.absolutePath}", files.isNotEmpty())
        files.forEach { f ->
            val bytes = f.readBytes()
            assertTrue("${f.name} missing %PDF header", String(bytes, 0, 5, Charsets.US_ASCII).startsWith("%PDF-"))
            assertTrue("${f.name} suspiciously small (${f.length()} bytes)", f.length() > 800L)
            val tail = String(bytes.takeLast(32).toByteArray(), Charsets.US_ASCII)
            assertTrue("${f.name} missing %%EOF marker", tail.contains("%%EOF"))
        }
        if (containsInvoiceNumber != null) {
            assertTrue(
                "no PDF for $containsInvoiceNumber in ${files.joinToString { it.name }}",
                files.any { it.name.contains(containsInvoiceNumber) }
            )
        }
        try {
            val dest = File("/sdcard/Download/latest_test_invoice.pdf")
            dest.delete()
            files.firstOrNull()?.copyTo(dest, overwrite = true)
        } catch (e: Exception) {
            android.util.Log.e("E2eBase", "Failed to copy to latest_test_invoice.pdf", e)
        }
        try {
            val stampFile = File("/sdcard/Download/corporate_invoice_${System.currentTimeMillis()}.pdf")
            files.firstOrNull()?.copyTo(stampFile, overwrite = true)
        } catch (e: Exception) {
            android.util.Log.e("E2eBase", "Failed to copy timestamped PDF", e)
        }
    }
}

/**
 * Base for tests that skip onboarding by seeding a completed, GST-registered setup
 * (business located in Maharashtra, walk-in customer, free invoice numbers).
 */
abstract class OnboardedBase(private val gstEnabled: Boolean = true) : E2eBase() {

    override fun waitForLaunchScreen() = waitForContentDescription("New Invoice")

    override suspend fun seed(db: AppDatabase) {
        super.seed(db)
        if (!gstEnabled) {
            db.appSettingsDao().updateGstSettings(false, "0", TaxType.NO_GST)
        }
        db.appSettingsDao().updateOnboardingCompleted(true)
        val now = System.currentTimeMillis()
        db.businessProfileDao().upsert(
            BusinessProfileEntity(
                id = 1L,
                businessName = "KJ Traders",
                ownerName = "Sahil",
                mobile = "9876543210",
                address = "42 MG Road, Pune",
                state = "Maharashtra",
                gstin = DEFAULT_GSTIN,
                email = null,
                city = "Pune",
                pincode = "411001",
                createdAt = now,
                updatedAt = now
            )
        )
    }
}