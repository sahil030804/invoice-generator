package com.kjbilling.app.data.repository

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.kjbilling.app.data.db.dao.AppSettingsDao
import com.kjbilling.app.data.db.dao.InvoiceDao
import com.kjbilling.app.data.db.entity.InvoiceEntity
import com.kjbilling.app.data.db.entity.InvoiceItemEntity
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentMethod
import com.kjbilling.app.domain.model.PaymentStatus
import com.kjbilling.app.domain.numbering.InvoiceNumbering
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal

class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val appSettingsDao: AppSettingsDao,
    private val database: RoomDatabase
) {

    fun getAll(): Flow<List<Invoice>> {
        return invoiceDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    suspend fun getById(id: Long): Invoice? {
        val pair = invoiceDao.getInvoiceWithItems(id) ?: return null
        val invoice = pair.first.toDomain()
        val items = pair.second.map { it.toDomain() }
        return invoice.copy(items = items)
    }

    fun search(query: String): Flow<List<Invoice>> {
        return invoiceDao.search(query).map { list -> list.map { it.toDomain() } }
    }

    suspend fun save(invoice: Invoice): Long {
        val invoiceEntity = InvoiceEntity.fromDomain(invoice)
        val itemEntities = invoice.items.map { InvoiceItemEntity.fromDomain(it, invoice.id) }
        
        return if (invoice.id == 0L) {
            invoiceDao.insertInvoiceWithItems(invoiceEntity, itemEntities)
        } else {
            invoiceDao.updateInvoiceWithItems(invoiceEntity, itemEntities)
            invoice.id
        }
    }

    suspend fun updateStatus(id: Long, status: InvoiceStatus) {
        invoiceDao.updateStatus(id, status)
    }

    suspend fun updatePayment(
        id: Long,
        paymentStatus: PaymentStatus,
        paymentMethod: PaymentMethod?,
        amountPaid: BigDecimal
    ) {
        invoiceDao.updatePaymentInfo(id, paymentStatus, paymentMethod, amountPaid.toString())
    }

    suspend fun deleteDraft(id: Long) {
        invoiceDao.deleteDraft(id)
    }

    /**
     * Saves a NEW invoice and assigns its number in one transaction: if the insert fails,
     * the counter rolls back too, so no invoice number is ever burned. Numbers already in use
     * are skipped, so a lowered counter can never produce a duplicate.
     * [invoice].invoiceNumber is ignored and replaced.
     */
    suspend fun saveNew(invoice: Invoice): Long {
        return database.withTransaction {
            val settings = appSettingsDao.getSettingsOnce()
            val prefix = settings?.invoicePrefix ?: DEFAULT_PREFIX
            val start = settings?.nextInvoiceNumber ?: 1L

            val next = InvoiceNumbering.nextFree(prefix, start) { number ->
                invoiceDao.countByNumber(number) > 0
            }
            appSettingsDao.updateInvoicePrefixAndNumber(prefix, next.nextCounter)

            save(invoice.copy(invoiceNumber = next.number))
        }
    }

    private companion object {
        const val DEFAULT_PREFIX = "INV-"
    }
}
