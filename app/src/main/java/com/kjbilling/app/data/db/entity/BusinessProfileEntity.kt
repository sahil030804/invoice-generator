package com.kjbilling.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kjbilling.app.domain.model.BusinessProfile

@Entity(tableName = "business_profiles")
data class BusinessProfileEntity(
    @PrimaryKey
    val id: Long = 1,
    val businessName: String,
    val ownerName: String,
    val mobile: String,
    val address: String,
    val state: String,
    val gstin: String?,
    val email: String?,
    val city: String?,
    val pincode: String?,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): BusinessProfile {
        return BusinessProfile(
            id = id,
            businessName = businessName,
            ownerName = ownerName,
            mobile = mobile,
            address = address,
            state = state,
            gstin = gstin,
            email = email,
            city = city,
            pincode = pincode,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(domain: BusinessProfile): BusinessProfileEntity {
            return BusinessProfileEntity(
                id = domain.id.takeIf { it != 0L } ?: 1,
                businessName = domain.businessName,
                ownerName = domain.ownerName,
                mobile = domain.mobile,
                address = domain.address,
                state = domain.state,
                gstin = domain.gstin,
                email = domain.email,
                city = domain.city,
                pincode = domain.pincode,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
        }
    }
}
