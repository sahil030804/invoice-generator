package com.kjbilling.app.domain.model

import java.math.BigDecimal

data class Product(
    val id: Long = 0,
    val name: String,
    val sellingPrice: BigDecimal,
    val hsnCode: String? = null,
    val unit: String = "PCS",
    val gstRate: BigDecimal? = null,
    val description: String? = null,
    val sku: String? = null,
    val useCount: Int = 0,
    val lastUsedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
