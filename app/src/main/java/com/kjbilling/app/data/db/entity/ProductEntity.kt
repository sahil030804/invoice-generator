package com.kjbilling.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kjbilling.app.domain.model.Product
import java.math.BigDecimal

@Entity(
    tableName = "products",
    indices = [
        Index("name"),
        Index("sku"),
        Index("lastUsedAt"),
        Index("useCount")
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sellingPrice: BigDecimal,
    val hsnCode: String?,
    val unit: String,
    val gstRate: BigDecimal?,
    val description: String?,
    val sku: String?,
    val useCount: Int,
    val lastUsedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): Product {
        return Product(
            id = id,
            name = name,
            sellingPrice = sellingPrice,
            hsnCode = hsnCode,
            unit = unit,
            gstRate = gstRate,
            description = description,
            sku = sku,
            useCount = useCount,
            lastUsedAt = lastUsedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(domain: Product): ProductEntity {
            return ProductEntity(
                id = domain.id,
                name = domain.name,
                sellingPrice = domain.sellingPrice,
                hsnCode = domain.hsnCode,
                unit = domain.unit,
                gstRate = domain.gstRate,
                description = domain.description,
                sku = domain.sku,
                useCount = domain.useCount,
                lastUsedAt = domain.lastUsedAt,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
        }
    }
}
