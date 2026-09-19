package com.kjbilling.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kjbilling.app.data.db.entity.BusinessProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: BusinessProfileEntity)

    @Query("SELECT * FROM business_profiles WHERE id = 1")
    fun getProfile(): Flow<BusinessProfileEntity?>

    @Query("SELECT * FROM business_profiles WHERE id = 1")
    suspend fun getProfileOnce(): BusinessProfileEntity?
}
