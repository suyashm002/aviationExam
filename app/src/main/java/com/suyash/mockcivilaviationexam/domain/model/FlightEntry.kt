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
    // Clock times, as written on the flight strip. Optional: older entries and
    // quick entries may only carry the decimal totals below.
    val offBlockTime: LocalTime? = null,
    val takeoffTime: LocalTime? = null,
    val landingTime: LocalTime? = null,
    val onBlockTime: LocalTime? = null,
    /** Off-blocks to on-blocks, in decimal hours. Includes taxi. */
    val blockTime: Double = 0.0,
    /** Takeoff to landing, in decimal hours. Excludes taxi. */
    val airTime: Double = 0.0,
    val dayLandings: Int = 0,
    val nightLandings: Int = 0,
    val instrumentApproaches: Int = 0,
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
    val lastModified: Long = System.currentTimeMillis(),
    /** Aircraft profile this flight was logged against, if chosen from the fleet list. */
    val aircraftId: Long? = null,
    /** Intermediate waypoints or the training area, e.g. "Ngong Hills - Athi River". */
    val routeVia: String? = null,
    val cruiseAltitudeFt: Int? = null,
    val cruiseSpeedKt: Int? = null,
    val maxAltitudeFt: Int? = null,
    val maxGroundSpeedKt: Int? = null,
    /** Ground distance actually flown, from the GPS track. */
    val distanceNm: Double? = null,
    val hobbsStart: Double? = null,
    val hobbsEnd: Double? = null,
    /** True when a GPS track was recorded for this flight. */
    val hasTrack: Boolean = false
) {
    /** Time on the ground under own power: block time not spent airborne. */
    val groundTime: Double
        get() = (blockTime - airTime).coerceAtLeast(0.0)

    val totalLandings: Int
        get() = dayLandings + nightLandings

    /** Takeoff to landing, in whole minutes — what a student asks: "how long was I flying?". */
    val flightMinutes: Int
        get() = toMinutes(airTime)

    /** Taxi, run-up and holding: block time that was not airborne, in whole minutes. */
    val groundMinutes: Int
        get() = toMinutes(groundTime)

    /** Hobbs meter difference when both readings were entered. */
    val hobbsTime: Double?
        get() = if (hobbsStart != null && hobbsEnd != null && hobbsEnd >= hobbsStart)
            Math.round((hobbsEnd - hobbsStart) * 10.0) / 10.0 else null

    /** "HKNW - Ngong Hills - HKNW" style route line for lists and the PDF. */
    val routeSummary: String
        get() = listOfNotNull(
            departureAerodrome.ifBlank { null },
            routeVia?.ifBlank { null },
            arrivalAerodrome.ifBlank { null }
        ).joinToString(" - ")

    private fun toMinutes(decimalHours: Double): Int =
        if (decimalHours <= 0.0) 0 else Math.round(decimalHours * 60).toInt()
}

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
    val createdAt: Long = System.currentTimeMillis(),
    /** Typical cruise speed, used to pre-fill the entry form. */
    val cruiseSpeedKt: Int? = null,
    /** Typical training-area altitude, used to pre-fill the entry form. */
    val cruiseAltitudeFt: Int? = null
) {
    /** "Cessna 172N · 5Y-ABC" — one line for chips and dropdowns. */
    val displayName: String
        get() {
            val name = listOfNotNull(manufacturer?.ifBlank { null }, model.ifBlank { null })
                .joinToString(" ")
                .ifBlank { type }
            return if (registration.isBlank()) name else "$name · $registration"
        }

    companion object {
        /**
         * The trainer most PPL students fly. Seeded as the default profile so a
         * new pilot's first entry needs only a registration.
         */
        fun cessna172N(userId: String, registration: String = ""): Aircraft = Aircraft(
            type = "C172",
            model = "172N",
            registration = registration,
            manufacturer = "Cessna",
            category = AircraftCategory.AIRPLANE_SINGLE_ENGINE_LAND,
            engineType = EngineType.PISTON,
            userId = userId,
            cruiseSpeedKt = 105,
            cruiseAltitudeFt = 3500
        )
    }
}

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
    val blockTime: Double = 0.0,
    val airTime: Double = 0.0,
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