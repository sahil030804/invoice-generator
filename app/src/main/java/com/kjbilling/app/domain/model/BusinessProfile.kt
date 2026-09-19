package com.kjbilling.app.domain.model

data class BusinessProfile(
    val id: Long = 1,
    val businessName: String,
    val ownerName: String,
    val mobile: String,
    val address: String,
    val state: String,
    val gstin: String? = null,
    val email: String? = null,
    val city: String? = null,
    val pincode: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
