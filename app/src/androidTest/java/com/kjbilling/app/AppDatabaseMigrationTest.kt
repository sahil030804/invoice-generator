package com.kjbilling.app

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kjbilling.app.data.db.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies real user databases survive upgrades: every row kept, new columns defaulted.
 * Uses the exported schemas in app/schemas (wired as androidTest assets).
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    private fun seedV1(db: SupportSQLiteDatabase) {
        db.execSQL(
            "INSERT INTO business_profiles (id, businessName, ownerName, mobile, address, state, gstin, email, city, pincode, createdAt, updatedAt) " +
                "VALUES (1, 'KJ Plastic', 'Sahil', '9876543210', 'Shop 12', 'Maharashtra', NULL, NULL, 'Pune', '411001', 1, 1)"
        )
        db.execSQL(
            "INSERT INTO invoices (id, invoiceNumber, invoiceDate, dueDate, customerId, customerName, customerGstin, customerState, customerAddress, " +
                "subtotal, totalDiscount, totalTax, grandTotal, taxType, status, paymentStatus, paymentMethod, amountPaid, notes, createdAt, updatedAt, finalizedAt) " +
                "VALUES (1, 'INV-0001', 1, NULL, NULL, 'Walk-in Customer', NULL, NULL, NULL, '100.00', '0.00', '18.00', '118.00', 'CGST_SGST', 'PAID', 'PAID', 'CASH', '118.00', NULL, 1, 1, NULL)"
        )
        db.execSQL(
            "INSERT INTO invoice_items (id, invoiceId, productId, itemName, description, hsnCode, quantity, unit, unitPrice, discountPercent, discountAmount, " +
                "gstRate, taxableAmount, cgstAmount, sgstAmount, igstAmount, taxAmount, total, sortOrder) " +
                "VALUES (1, 1, NULL, 'Bulb', NULL, NULL, '1', 'PCS', '100.00', '0', '0.00', '18', '100.00', '9.00', '9.00', NULL, '18.00', '118.00', 0)"
        )
    }

    @Test
    fun migrate1To3_keepsRowsAndDefaultsNewColumns() {
        helper.createDatabase(TEST_DB, 1).use { seedV1(it) }

        val db = helper.runMigrationsAndValidate(
            TEST_DB, 3, true, AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3
        )

        db.query("SELECT businessName, upiId FROM business_profiles WHERE id = 1").use { c ->
            c.moveToFirst()
            assertEquals("KJ Plastic", c.getString(0))
            assertNull(c.getString(1))
        }
        db.query("SELECT invoiceNumber, sellerName FROM invoices").use { c ->
            assertEquals(1, c.count)
            c.moveToFirst()
            assertEquals("INV-0001", c.getString(0))
            assertEquals("KJ Plastic", c.getString(1))
        }
        db.query("SELECT itemName, priceIncludesTax FROM invoice_items").use { c ->
            c.moveToFirst()
            assertEquals("Bulb", c.getString(0))
            assertEquals(0, c.getInt(1))
        }
        db.close()
    }

    @Test
    fun migrate2To3_addsColumnsWithoutTouchingData() {
        helper.createDatabase(TEST_DB, 2).use { db ->
            seedV1(db)
            db.execSQL("UPDATE invoices SET sellerName = 'KJ Plastic'")
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, AppDatabase.MIGRATION_2_3)

        db.query("SELECT COUNT(*) FROM invoice_items WHERE priceIncludesTax = 0").use { c ->
            c.moveToFirst()
            assertEquals(1, c.getInt(0))
        }
        db.query("SELECT upiId FROM business_profiles").use { c ->
            c.moveToFirst()
            assertNull(c.getString(0))
        }
        db.close()
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
