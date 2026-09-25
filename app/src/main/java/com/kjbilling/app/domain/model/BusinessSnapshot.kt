package com.kjbilling.app.domain.model

/**
 * Seller details frozen onto an invoice at issue time, so re-downloading an old invoice
 * still shows the name, address and GSTIN it was issued with even if the profile changes later.
 * [address] is pre-joined: "street, city, state, pincode" (blank parts skipped).
 */
data class BusinessSnapshot(
    val name: String,
    val address: String,
    val phone: String,
    val email: String?,
    val gstin: String?,
    val ownerName: String?
) {
    companion object {
        fun from(profile: BusinessProfile): BusinessSnapshot {
            return BusinessSnapshot(
                name = profile.businessName,
                address = listOfNotNull(
                    profile.address.takeIf { it.isNotBlank() },
                    profile.city?.takeIf { it.isNotBlank() },
                    profile.state.takeIf { it.isNotBlank() },
                    profile.pincode?.takeIf { it.isNotBlank() }
                ).joinToString(", "),
                phone = profile.mobile,
                email = profile.email?.takeIf { it.isNotBlank() },
                gstin = profile.gstin?.takeIf { it.isNotBlank() },
                ownerName = profile.ownerName.takeIf { it.isNotBlank() }
            )
        }
    }
}
