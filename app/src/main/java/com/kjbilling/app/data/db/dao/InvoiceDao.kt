package com.kjbilling.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kjbilling.app.data.db.entity.InvoiceEntity
import com.kjbilling.app.data.db.entity.InvoiceItemEntity
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentMethod
import com.kjbilling.app.domain.model.PaymentStatus
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface InvoiceDao {
    @Insert
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert
    suspend fun insertItems(items: List<InvoiceItemEntity>)

    @Transaction
    suspend fun insertInvoiceWithItems(invoice: InvoiceEntity, items: List<InvoiceItemEntity>): Long {
        val invoiceId = insertInvoice(invoice)
        val updatedItems = items.map { it.copy(invoiceId = invoiceId) }
        insertItems(updatedItems)
        return invoiceId
    }

    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAll(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getById(id: Long): InvoiceEntity?

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId ORDER BY sortOrder ASC")
    suspend fun getItemsByInvoiceId(invoiceId: Long): List<InvoiceItemEntity>

    @Transaction
    suspend fun getInvoiceWithItems(id: Long): Pair<InvoiceEntity, List<InvoiceItemEntity>>? {
        val invoice = getById(id) ?: return null
        val items = getItemsByInvoiceId(id)
        return Pair(invoice, items)
    }

    @Query("SELECT * FROM invoices WHERE invoiceNumber LIKE '%' || :query || '%' OR customerName LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun search(query: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE status = :status ORDER BY createdAt DESC")
    fun filterByStatus(status: InvoiceStatus): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE invoiceDate >= :startDate AND invoiceDate <= :endDate ORDER BY createdAt DESC")
    fun filterByDateRange(startDate: Long, endDate: Long): Flow<List<InvoiceEntity>>

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItemsByInvoiceId(invoiceId: Long)

    @Transaction
    suspend fun updateInvoiceWithItems(invoice: InvoiceEntity, items: List<InvoiceItemEntity>) {
        updateInvoice(invoice)
        deleteItemsByInvoiceId(invoice.id)
        val updatedItems = items.map { it.copy(invoiceId = invoice.id) }
        insertItems(updatedItems)
    }

    @Query("UPDATE invoices SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: InvoiceStatus)

    @Query("UPDATE invoices SET paymentStatus = :paymentStatus, paymentMethod = :paymentMethod, amountPaid = :amountPaid WHERE id = :id")
    suspend fun updatePaymentInfo(
        id: Long,
        paymentStatus: PaymentStatus,
        paymentMethod: PaymentMethod?,
        amountPaid: String
    )

    @Query("DELETE FROM invoices WHERE id = :id AND status = 'DRAFT'")
    suspend fun deleteDraft(id: Long)
}
