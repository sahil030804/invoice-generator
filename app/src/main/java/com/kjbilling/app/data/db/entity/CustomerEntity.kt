package com.kjbilling.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kjbilling.app.domain.model.Customer

@Entity(
    tableName = "customers",
    indices = [
        Index("name"),
        Index("mobile"),
        Index("gstin"),
        Index("lastUsedAt")
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mobile: String?,
    val email: String?,
    val billingAddress: String?,
    val state: String?,
    val pincode: String?,
    val gstin: String?,
    val businessName: String?,
    val notes: String?,
    val isWalkIn: Boolean,
    val lastUsedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): Customer {
        return Customer(
            id = id,
            name = name,
            mobile = mobile,
            email = email,
            billingAddress = billingAddress,
            state = state,
            pincode = pincode,
            gstin = gstin,
            businessName = businessName,
            notes = notes,
            isWalkIn = isWalkIn,
            lastUsedAt = lastUsedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(domain: Customer): CustomerEntity {
            return CustomerEntity(
                id = domain.id,
                name = domain.name,
                mobile = domain.mobile,
                email = domain.email,
                billingAddress = domain.billingAddress,
                state = domain.state,
                pincode = domain.pincode,
                gstin = domain.gstin,
                businessName = domain.businessName,
                notes = domain.notes,
                isWalkIn = domain.isWalkIn,
                lastUsedAt = domain.lastUsedAt,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
        }
    }
}
