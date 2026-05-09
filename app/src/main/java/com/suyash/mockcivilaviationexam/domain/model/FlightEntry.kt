package com.suyash.mockcivilaviationexam.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class FlightEntry(
    val id: Long = 0,
    val date: LocalDate,
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

data class Aircraft(
    val id: Long = 0,
    val type: String,
    val model: String,
    val registration: String,
    val manufacturer: String? = null,
    val category: AircraftCategory,
    val engineType: EngineType,
    val isComplex: Boolean = false,
    val isHighPerformance: Boolean = false,
    val isTailwheel: Boolean = false,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class Instructor(
    val id: Long = 0,
    val name: String,
    val licenseNumber: String,
    val certificateType: InstructorCertificate,
    val email: String? = null,
    val phone: String? = null,
    val organization: String? = null,
    val digitalSignature: String? = null,
    val isActive: Boolean = true,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class UserProfile(
    val id: String,
    val name: String,
    val licenseNumber: String? = null,
    val certificateType: PilotCertificate? = null,
    val email: String,
    val phone: String? = null,
    val dateOfBirth: LocalDate? = null,
    val address: String? = null,
    val medicalExpiry: LocalDate? = null,
    val bifrExpiry: LocalDate? = null,
    val totalHours: Double = 0.0,
    val picHours: Double = 0.0,
    val crossCountryHours: Double = 0.0,
    val nightHours: Double = 0.0,
    val ifrHours: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)

data class Endorsement(
    val id: Long = 0,
    val flightEntryId: Long,
    val instructorId: Long,
    val type: EndorsementType,
    val digitalSignature: String,
    val timestamp: Long,
    val remarks: String? = null
)

enum class AircraftCategory {
    AIRPLANE_SINGLE_ENGINE_LAND,
    AIRPLANE_MULTI_ENGINE_LAND,
    AIRPLANE_SINGLE_ENGINE_SEA,
    AIRPLANE_MULTI_ENGINE_SEA,
    HELICOPTER,
    GLIDER,
    BALLOON,
    AIRSHIP
}

enum class EngineType {
    PISTON,
    TURBOPROP,
    TURBOJET,
    TURBOFAN,
    ELECTRIC
}

enum class InstructorCertificate {
    CFI,
    CFII,
    MEI,
    FLIGHT_INSTRUCTOR,
    GROUND_INSTRUCTOR
}

enum class PilotCertificate {
    STUDENT,
    PRIVATE,
    COMMERCIAL,
    AIRLINE_TRANSPORT,
    RECREATIONAL,
    SPORT
}

enum class EndorsementType {
    SOLO,
    CHECKRIDE,
    BFR,
    IPC,
    TYPE_RATING,
    HIGH_PERFORMANCE,
    COMPLEX,
    TAILWHEEL,
    NIGHT,
    CROSS_COUNTRY,
    INSTRUMENT
}

data class LogbookSummary(
    val totalTime: Double = 0.0,
    val picTime: Double = 0.0,
    val dualTime: Double = 0.0,
    val coPilotTime: Double = 0.0,
    val instructorTime: Double = 0.0,
    val crossCountryTime: Double = 0.0,
    val nightTime: Double = 0.0,
    val ifrTime: Double = 0.0,
    val vfrTime: Double = 0.0,
    val simulatorTime: Double = 0.0,
    val totalLandings: Int = 0,
    val nightLandings: Int = 0,
    val dayLandings: Int = 0
)