package com.suyash.mockcivilaviationexam.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.suyash.mockcivilaviationexam.domain.model.PilotCertificate
import com.suyash.mockcivilaviationexam.domain.model.UserProfile
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val licenseNumber: String? = null,
    val certificateType: String? = null,
    val email: String,
    val phone: String? = null,
    val dateOfBirth: String? = null,
    val address: String? = null,
    val medicalExpiry: String? = null,
    val bifrExpiry: String? = null,
    val totalHours: Double = 0.0,
    val picHours: Double = 0.0,
    val crossCountryHours: Double = 0.0,
    val nightHours: Double = 0.0,
    val ifrHours: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)

fun UserProfileEntity.toDomainModel(): UserProfile {
    return UserProfile(
        id = id,
        name = name,
        licenseNumber = licenseNumber,
        certificateType = certificateType?.let { PilotCertificate.valueOf(it) },
        email = email,
        phone = phone,
        dateOfBirth = dateOfBirth?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) },
        address = address,
        medicalExpiry = medicalExpiry?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) },
        bifrExpiry = bifrExpiry?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) },
        totalHours = totalHours,
        picHours = picHours,
        crossCountryHours = crossCountryHours,
        nightHours = nightHours,
        ifrHours = ifrHours,
        createdAt = createdAt,
        lastModified = lastModified
    )
}

fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        id = id,
        name = name,
        licenseNumber = licenseNumber,
        certificateType = certificateType?.name,
        email = email,
        phone = phone,
        dateOfBirth = dateOfBirth?.format(DateTimeFormatter.ISO_LOCAL_DATE),
        address = address,
        medicalExpiry = medicalExpiry?.format(DateTimeFormatter.ISO_LOCAL_DATE),
        bifrExpiry = bifrExpiry?.format(DateTimeFormatter.ISO_LOCAL_DATE),
        totalHours = totalHours,
        picHours = picHours,
        crossCountryHours = crossCountryHours,
        nightHours = nightHours,
        ifrHours = ifrHours,
        createdAt = createdAt,
        lastModified = lastModified
    )
}

