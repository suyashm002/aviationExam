package com.suyash.mockcivilaviationexam.data.local.dao

import androidx.room.*
import com.suyash.mockcivilaviationexam.data.local.entities.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = :userId")
    fun getUserProfile(userId: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = :userId")
    suspend fun getUserProfileSync(userId: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfileEntity)

    @Update
    suspend fun updateUserProfile(userProfile: UserProfileEntity)

    @Delete
    suspend fun deleteUserProfile(userProfile: UserProfileEntity)

    @Query("UPDATE user_profile SET totalHours = :totalHours WHERE id = :userId")
    suspend fun updateTotalHours(userId: String, totalHours: Double)

    @Query("UPDATE user_profile SET picHours = :picHours WHERE id = :userId")
    suspend fun updatePicHours(userId: String, picHours: Double)

    @Query("UPDATE user_profile SET crossCountryHours = :crossCountryHours WHERE id = :userId")
    suspend fun updateCrossCountryHours(userId: String, crossCountryHours: Double)

    @Query("UPDATE user_profile SET nightHours = :nightHours WHERE id = :userId")
    suspend fun updateNightHours(userId: String, nightHours: Double)

    @Query("UPDATE user_profile SET ifrHours = :ifrHours WHERE id = :userId")
    suspend fun updateIfrHours(userId: String, ifrHours: Double)
}