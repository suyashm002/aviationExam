package com.suyash.mockcivilaviationexam.data.local.repository

import com.suyash.mockcivilaviationexam.data.local.dao.UserProfileDao
import com.suyash.mockcivilaviationexam.data.local.entities.toDomainModel
import com.suyash.mockcivilaviationexam.data.local.entities.toEntity
import com.suyash.mockcivilaviationexam.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
class UserProfileRepository(
    private val userProfileDao: UserProfileDao
) {
    fun getUserProfile(userId: String): Flow<UserProfile?> =
        userProfileDao.getUserProfile(userId).map { entity ->
            entity?.toDomainModel()
        }

    suspend fun getUserProfileSync(userId: String): UserProfile? =
        userProfileDao.getUserProfileSync(userId)?.toDomainModel()

    suspend fun insertUserProfile(userProfile: UserProfile) =
        userProfileDao.insertUserProfile(userProfile.toEntity())

    suspend fun updateUserProfile(userProfile: UserProfile) =
        userProfileDao.updateUserProfile(
            userProfile.copy(lastModified = System.currentTimeMillis()).toEntity()
        )

    suspend fun deleteUserProfile(userProfile: UserProfile) =
        userProfileDao.deleteUserProfile(userProfile.toEntity())

    suspend fun updateTotalHours(userId: String, totalHours: Double) =
        userProfileDao.updateTotalHours(userId, totalHours)

    suspend fun updatePicHours(userId: String, picHours: Double) =
        userProfileDao.updatePicHours(userId, picHours)

    suspend fun updateCrossCountryHours(userId: String, crossCountryHours: Double) =
        userProfileDao.updateCrossCountryHours(userId, crossCountryHours)

    suspend fun updateNightHours(userId: String, nightHours: Double) =
        userProfileDao.updateNightHours(userId, nightHours)

    suspend fun updateIfrHours(userId: String, ifrHours: Double) =
        userProfileDao.updateIfrHours(userId, ifrHours)
}