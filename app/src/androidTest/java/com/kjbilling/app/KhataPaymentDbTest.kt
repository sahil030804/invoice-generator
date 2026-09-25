package com.kjbilling.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kjbilling.app.data.db.AppDatabase
import com.kjbilling.app.data.db.entity.AppSettingsEntity
import com.kjbilling.app.data.repository.InvoiceRepository
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentMethod
import com.kjbilling.app.domain.model.PaymentStatus
import com.kjbilling.app.domain.model.TaxType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

/** In-memory database only; never touches the app's real data. */
@RunWith(AndroidJUnit4::class)
class KhataPaymentDbTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: InvoiceRepository

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        db.appSettingsDao().upsert(
            AppSettingsEntity(
                id = 1, gstEnabled = true, defaultGstRate = BigDecimal("18.00"), defaultTaxType = TaxType.CGST_SGST,
                invoicePrefix = "INV-", nextInvoiceNumber = 1, onboardingCompleted = true, defaultPaymentStatus = PaymentStatus.UNPAID
            )
        )
        repo = InvoiceRepository(db.invoiceDao(), db.appSettingsDao(), db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun udhaarBill(customerId: Long, total: String, date: Long): Long = repo.saveNew(
        Invoice(
            invoiceNumber = "", invoiceDate = date, createdAt = date, customerId = customerId, customerName = "Rakesh",
            grandTotal = BigDecimal(total), status = InvoiceStatus.GENERATED, paymentStatus = PaymentStatus.UNPAID
        )
    )

    @Test
    fun receive_clearsOldestFirst_andWritesStatusTogether() = runBlocking {
        val first = udhaarBill(7, "100.00", date = 10)
        val second = udhaarBill(7, "200.00", date = 20)

        val result = repo.receiveCustomerPayment(7, BigDecimal("150"), PaymentMethod.UPI)

        assertEquals(0, BigDecimal.ZERO.compareTo(result.unapplied))
        val a = repo.getById(first)!!
        assertEquals(PaymentStatus.PAID, a.paymentStatus)
        assertEquals(InvoiceStatus.PAID, a.status)
        assertEquals(PaymentMethod.UPI, a.paymentMethod)
        val b = repo.getById(second)!!
        assertEquals(PaymentStatus.PARTIAL, b.paymentStatus)
        assertEquals(InvoiceStatus.GENERATED, b.status)
        assertEquals(0, BigDecimal("50").compareTo(b.amountPaid))
    }

    @Test
    fun receive_isolatedPerCustomer_andReportsOverpayment() = runBlocking {
        val mine = udhaarBill(7, "100.00", date = 10)
        val other = udhaarBill(8, "100.00", date = 5)

        val result = repo.receiveCustomerPayment(7, BigDecimal("130"), PaymentMethod.CASH)

        assertEquals(0, BigDecimal("30").compareTo(result.unapplied))
        assertEquals(PaymentStatus.PAID, repo.getById(mine)!!.paymentStatus)
        assertEquals(PaymentStatus.UNPAID, repo.getById(other)!!.paymentStatus)
    }
}
