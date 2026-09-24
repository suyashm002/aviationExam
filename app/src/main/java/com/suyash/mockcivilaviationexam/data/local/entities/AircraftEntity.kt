package com.suyash.mockcivilaviationexam.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.suyash.mockcivilaviationexam.domain.model.Aircraft
import com.suyash.mockcivilaviationexam.domain.model.AircraftCategory
import com.suyash.mockcivilaviationexam.domain.model.EngineType

@Entity(tableName = "aircraft")
data class AircraftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val model: String,
    val registration: String,
    val manufacturer: String? = null,
    val category: String,
    val engineType: String,
    val isComplex: Boolean = false,
    val isHighPerformance: Boolean = false,
    val isTailwheel: Boolean = false,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val cruiseSpeedKt: Int? = null,
    val cruiseAltitudeFt: Int? = null
)

fun AircraftEntity.toDomainModel(): Aircraft {
    return Aircraft(
        id = id,
        type = type,
        model = model,
        registration = registration,
        manufacturer = manufacturer,
        category = AircraftCategory.valueOf(category),
        engineType = EngineType.valueOf(engineType),
        isComplex = isComplex,
        isHighPerformance = isHighPerformance,
        isTailwheel = isTailwheel,
        userId = userId,
        createdAt = createdAt,
        cruiseSpeedKt = cruiseSpeedKt,
        cruiseAltitudeFt = cruiseAltitudeFt
    )
}

fun Aircraft.toEntity(): AircraftEntity {
    return AircraftEntity(
        id = id,
        type = type,
        model = model,
        registration = registration,
        manufacturer = manufacturer,
        category = category.name,
        engineType = engineType.name,
        isComplex = isComplex,
        isHighPerformance = isHighPerformance,
        isTailwheel = isTailwheel,
        userId = userId,
        createdAt = createdAt,
        cruiseSpeedKt = cruiseSpeedKt,
        cruiseAltitudeFt = cruiseAltitudeFt
    )
}

