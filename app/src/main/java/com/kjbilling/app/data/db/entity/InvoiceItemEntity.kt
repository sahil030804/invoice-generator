package com.kjbilling.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kjbilling.app.domain.model.InvoiceItem
import java.math.BigDecimal

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("invoiceId")
    ]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long,
    val productId: Long?,
    val itemName: String,
    val description: String?,
    val hsnCode: String?,
    val quantity: BigDecimal,
    val unit: String?,
    val unitPrice: BigDecimal,
    val discountPercent: BigDecimal,
    val discountAmount: BigDecimal,
    val gstRate: BigDecimal,
    val taxableAmount: BigDecimal,
    val cgstAmount: BigDecimal?,
    val sgstAmount: BigDecimal?,
    val igstAmount: BigDecimal?,
    val taxAmount: BigDecimal,
    val total: BigDecimal,
    val sortOrder: Int
) {
    fun toDomain(): InvoiceItem {
        return InvoiceItem(
            id = id,
            invoiceId = invoiceId,
            productId = productId,
            itemName = itemName,
            description = description,
            hsnCode = hsnCode,
            quantity = quantity,
            unit = unit,
            unitPrice = unitPrice,
            discountPercent = discountPercent,
            discountAmount = discountAmount,
            gstRate = gstRate,
            taxableAmount = taxableAmount,
            cgstAmount = cgstAmount,
            sgstAmount = sgstAmount,
            igstAmount = igstAmount,
            taxAmount = taxAmount,
            total = total,
            sortOrder = sortOrder
        )
    }

    companion object {
        fun fromDomain(domain: InvoiceItem, invoiceId: Long = domain.invoiceId): InvoiceItemEntity {
            return InvoiceItemEntity(
                id = domain.id,
                invoiceId = invoiceId,
                productId = domain.productId,
                itemName = domain.itemName,
                description = domain.description,
                hsnCode = domain.hsnCode,
                quantity = domain.quantity,
                unit = domain.unit,
                unitPrice = domain.unitPrice,
                discountPercent = domain.discountPercent,
                discountAmount = domain.discountAmount,
                gstRate = domain.gstRate,
                taxableAmount = domain.taxableAmount,
                cgstAmount = domain.cgstAmount,
                sgstAmount = domain.sgstAmount,
                igstAmount = domain.igstAmount,
                taxAmount = domain.taxAmount,
                total = domain.total,
                sortOrder = domain.sortOrder
            )
        }
    }
}
