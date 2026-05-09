package com.suyash.mockcivilaviationexam.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.suyash.mockcivilaviationexam.domain.model.FlightEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity(tableName = "flight_entries")
@TypeConverters(Converters::class)
data class FlightEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val departureAerodrome: String,
    val arrivalAerodrome: String,
    val aircraftType: String,
    val aircraftModel: String,
    val aircraftRegistration: String,
    val totalFlightTime: Double,
    val dayTime: Double = 0.0,
    val nightTime: Double = 0.0,
    val picTime: Double = 0.0,
    val dualTime: Double = 0.0,
    val coPilotTime: Double = 0.0,
    val instructorTime: Double = 0.0,
    val ifrTime: Double = 0.0,
    val vfrTime: Double = 0.0,
    val crossCountryTime: Double = 0.0,
    val simulatorTime: Double = 0.0,
    val isSimulator: Boolean = false,
    val exerciseNumber: String? = null,
    val lessonNumber: String? = null,
    val remarks: String = "",
    val instructorName: String? = null,
    val instructorLicenseNumber: String? = null,
    val instructorSignature: String? = null,
    val isEndorsed: Boolean = false,
    val endorsementTimestamp: Long? = null,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)

fun FlightEntryEntity.toDomainModel(): FlightEntry {
    return FlightEntry(
        id = id,
        date = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE),
        departureAerodrome = departureAerodrome,
        arrivalAerodrome = arrivalAerodrome,
        aircraftType = aircraftType,
        aircraftModel = aircraftModel,
        aircraftRegistration = aircraftRegistration,
        totalFlightTime = totalFlightTime,
        dayTime = dayTime,
        nightTime = nightTime,
        picTime = picTime,
        dualTime = dualTime,
        coPilotTime = coPilotTime,
        instructorTime = instructorTime,
        ifrTime = ifrTime,
        vfrTime = vfrTime,
        crossCountryTime = crossCountryTime,
        simulatorTime = simulatorTime,
        isSimulator = isSimulator,
        exerciseNumber = exerciseNumber,
        lessonNumber = lessonNumber,
        remarks = remarks,
        instructorName = instructorName,
        instructorLicenseNumber = instructorLicenseNumber,
        instructorSignature = instructorSignature,
        isEndorsed = isEndorsed,
        endorsementTimestamp = endorsementTimestamp,
        userId = userId,
        createdAt = createdAt,
        lastModified = lastModified
    )
}

fun FlightEntry.toEntity(): FlightEntryEntity {
    return FlightEntryEntity(
        id = id,
        date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
        departureAerodrome = departureAerodrome,
        arrivalAerodrome = arrivalAerodrome,
        aircraftType = aircraftType,
        aircraftModel = aircraftModel,
        aircraftRegistration = aircraftRegistration,
        totalFlightTime = totalFlightTime,
        dayTime = dayTime,
        nightTime = nightTime,
        picTime = picTime,
        dualTime = dualTime,
        coPilotTime = coPilotTime,
        instructorTime = instructorTime,
        ifrTime = ifrTime,
        vfrTime = vfrTime,
        crossCountryTime = crossCountryTime,
        simulatorTime = simulatorTime,
        isSimulator = isSimulator,
        exerciseNumber = exerciseNumber,
        lessonNumber = lessonNumber,
        remarks = remarks,
        instructorName = instructorName,
        instructorLicenseNumber = instructorLicenseNumber,
        instructorSignature = instructorSignature,
        isEndorsed = isEndorsed,
        endorsementTimestamp = endorsementTimestamp,
        userId = userId,
        createdAt = createdAt,
        lastModified = lastModified
    )
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }
}