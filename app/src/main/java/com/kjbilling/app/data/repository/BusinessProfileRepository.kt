package com.kjbilling.app.data.repository

import com.kjbilling.app.data.db.dao.BusinessProfileDao
import com.kjbilling.app.data.db.entity.BusinessProfileEntity
import com.kjbilling.app.domain.model.BusinessProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BusinessProfileRepository(private val dao: BusinessProfileDao) {

    fun getProfile(): Flow<BusinessProfile?> {
        return dao.getProfile().map { it?.toDomain() }
    }

    suspend fun getProfileOnce(): BusinessProfile? {
        return dao.getProfileOnce()?.toDomain()
    }

    suspend fun saveProfile(profile: BusinessProfile) {
        dao.upsert(BusinessProfileEntity.fromDomain(profile))
    }
}
