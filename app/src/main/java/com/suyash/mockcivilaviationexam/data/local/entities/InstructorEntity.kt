package com.suyash.mockcivilaviationexam.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.suyash.mockcivilaviationexam.domain.model.Instructor
import com.suyash.mockcivilaviationexam.domain.model.InstructorCertificate

@Entity(tableName = "instructors")
data class InstructorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val licenseNumber: String,
    val certificateType: String,
    val email: String? = null,
    val phone: String? = null,
    val organization: String? = null,
    val digitalSignature: String? = null,
    val isActive: Boolean = true,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)

fun InstructorEntity.toDomainModel(): Instructor {
    return Instructor(
        id = id,
        name = name,
        licenseNumber = licenseNumber,
        certificateType = InstructorCertificate.valueOf(certificateType),
        email = email,
        phone = phone,
        organization = organization,
        digitalSignature = digitalSignature,
        isActive = isActive,
        userId = userId,
        createdAt = createdAt
    )
}

fun Instructor.toEntity(): InstructorEntity {
    return InstructorEntity(
        id = id,
        name = name,
        licenseNumber = licenseNumber,
        certificateType = certificateType.name,
        email = email,
        phone = phone,
        organization = organization,
        digitalSignature = digitalSignature,
        isActive = isActive,
        userId = userId,
        createdAt = createdAt
    )
}

