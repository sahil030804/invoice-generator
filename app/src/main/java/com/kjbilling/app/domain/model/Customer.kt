package com.kjbilling.app.domain.model

data class Customer(
    val id: Long = 0,
    val name: String,
    val mobile: String? = null,
    val email: String? = null,
    val billingAddress: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    val gstin: String? = null,
    val businessName: String? = null,
    val notes: String? = null,
    val isWalkIn: Boolean = false,
    val lastUsedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun walkIn() = Customer(
            id = -1,
            name = "Walk-in Customer",
            isWalkIn = true
        )
    }
}
