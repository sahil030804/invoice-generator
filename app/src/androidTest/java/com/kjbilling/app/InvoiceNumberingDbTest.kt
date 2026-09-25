package com.kjbilling.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kjbilling.app.data.db.AppDatabase
import com.kjbilling.app.data.db.entity.AppSettingsEntity
import com.kjbilling.app.data.repository.InvoiceRepository
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceItem
import com.kjbilling.app.domain.model.PaymentStatus
import com.kjbilling.app.domain.model.TaxType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

/**
 * Runs against an in-memory database only; never touches the app's real data.
 */
@RunWith(AndroidJUnit4::class)
class InvoiceNumberingDbTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: InvoiceRepository

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        db.appSettingsDao().upsert(
            AppSettingsEntity(
                id = 1,
                gstEnabled = true,
                defaultGstRate = BigDecimal("18.00"),
                defaultTaxType = TaxType.CGST_SGST,
                invoicePrefix = "INV-",
                nextInvoiceNumber = 1,
                onboardingCompleted = true,
                defaultPaymentStatus = PaymentStatus.UNPAID
            )
        )
        repo = InvoiceRepository(db.invoiceDao(), db.appSettingsDao(), db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun invoice(items: List<InvoiceItem> = emptyList()) = Invoice(
        invoiceNumber = "",
        invoiceDate = 0L,
        customerName = "Walk-in Customer",
        items = items
    )

    private fun item(id: Long = 0) = InvoiceItem(
        id = id,
        itemName = "Wire",
        quantity = BigDecimal.ONE,
        unitPrice = BigDecimal("10.00"),
        gstRate = BigDecimal.ZERO
    )

    private suspend fun counter() = db.appSettingsDao().getSettingsOnce()!!.nextInvoiceNumber

    @Test
    fun saveNew_assignsSequentialNumbers() = runBlocking {
        val id1 = repo.saveNew(invoice())
        val id2 = repo.saveNew(invoice())

        assertEquals("INV-0001", repo.getById(id1)!!.invoiceNumber)
        assertEquals("INV-0002", repo.getById(id2)!!.invoiceNumber)
        assertEquals(3L, counter())
    }

    @Test
    fun saveNew_failedInsert_doesNotBurnNumber() = runBlocking {
        // Two items with the same primary key make the insert fail after the number was picked.
        val broken = invoice(listOf(item(id = 5), item(id = 5)))

        val failed = runCatching { repo.saveNew(broken) }

        assertTrue(failed.isFailure)
        assertEquals(1L, counter())

        val id = repo.saveNew(invoice())
        assertEquals("INV-0001", repo.getById(id)!!.invoiceNumber)
    }

    @Test
    fun saveNew_loweredCounter_skipsUsedNumbers() = runBlocking {
        repo.saveNew(invoice())
        repo.saveNew(invoice())
        db.appSettingsDao().updateInvoicePrefixAndNumber("INV-", 1)

        val id = repo.saveNew(invoice())

        val number = repo.getById(id)!!.invoiceNumber
        assertEquals("INV-0003", number)
        assertNotEquals("INV-0001", number)
    }
}
