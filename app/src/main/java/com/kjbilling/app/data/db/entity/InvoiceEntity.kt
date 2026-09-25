package com.kjbilling.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kjbilling.app.domain.model.BusinessSnapshot
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentMethod
import com.kjbilling.app.domain.model.PaymentStatus
import com.kjbilling.app.domain.model.TaxType
import java.math.BigDecimal

@Entity(
    tableName = "invoices",
    indices = [
        Index("invoiceNumber", unique = true),
        Index("customerId"),
        Index("customerName"),
        Index("createdAt"),
        Index("status")
    ]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val invoiceDate: Long,
    val dueDate: Long?,
    val customerId: Long?,
    val customerName: String,
    val customerGstin: String?,
    val customerState: String?,
    val customerAddress: String?,
    val subtotal: BigDecimal,
    val totalDiscount: BigDecimal,
    val totalTax: BigDecimal,
    val grandTotal: BigDecimal,
    val taxType: TaxType,
    val status: InvoiceStatus,
    val paymentStatus: PaymentStatus,
    val paymentMethod: PaymentMethod?,
    val amountPaid: BigDecimal,
    val notes: String?,
    // Seller snapshot (null only for rows that predate migration 1→2 and had no profile).
    val sellerName: String?,
    val sellerAddress: String?,
    val sellerPhone: String?,
    val sellerEmail: String?,
    val sellerGstin: String?,
    val sellerOwner: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val finalizedAt: Long?
) {
    fun toDomain(): Invoice {
        return Invoice(
            id = id,
            invoiceNumber = invoiceNumber,
            invoiceDate = invoiceDate,
            dueDate = dueDate,
            customerId = customerId,
            customerName = customerName,
            customerGstin = customerGstin,
            customerState = customerState,
            customerAddress = customerAddress,
            items = emptyList(), // Filled by repository
            subtotal = subtotal,
            totalDiscount = totalDiscount,
            totalTax = totalTax,
            grandTotal = grandTotal,
            taxType = taxType,
            status = status,
            paymentStatus = paymentStatus,
            paymentMethod = paymentMethod,
            amountPaid = amountPaid,
            notes = notes,
            seller = sellerName?.let {
                BusinessSnapshot(
                    name = it,
                    address = sellerAddress.orEmpty(),
                    phone = sellerPhone.orEmpty(),
                    email = sellerEmail,
                    gstin = sellerGstin,
                    ownerName = sellerOwner
                )
            },
            createdAt = createdAt,
            updatedAt = updatedAt,
            finalizedAt = finalizedAt
        )
    }

    companion object {
        fun fromDomain(domain: Invoice): InvoiceEntity {
            return InvoiceEntity(
                id = domain.id,
                invoiceNumber = domain.invoiceNumber,
                invoiceDate = domain.invoiceDate,
                dueDate = domain.dueDate,
                customerId = domain.customerId,
                customerName = domain.customerName,
                customerGstin = domain.customerGstin,
                customerState = domain.customerState,
                customerAddress = domain.customerAddress,
                subtotal = domain.subtotal,
                totalDiscount = domain.totalDiscount,
                totalTax = domain.totalTax,
                grandTotal = domain.grandTotal,
                taxType = domain.taxType,
                status = domain.status,
                paymentStatus = domain.paymentStatus,
                paymentMethod = domain.paymentMethod,
                amountPaid = domain.amountPaid,
                notes = domain.notes,
                sellerName = domain.seller?.name,
                sellerAddress = domain.seller?.address,
                sellerPhone = domain.seller?.phone,
                sellerEmail = domain.seller?.email,
                sellerGstin = domain.seller?.gstin,
                sellerOwner = domain.seller?.ownerName,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt,
                finalizedAt = domain.finalizedAt
            )
        }
    }
}
