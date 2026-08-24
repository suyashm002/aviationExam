package com.suyash.mockcivilaviationexam.domain.usecase

import com.suyash.mockcivilaviationexam.data.local.repository.FlightEntryRepository
import com.suyash.mockcivilaviationexam.data.local.repository.UserProfileRepository
import com.suyash.mockcivilaviationexam.domain.model.FlightEntry
import com.suyash.mockcivilaviationexam.domain.model.LogbookSummary
import kotlinx.coroutines.flow.Flow
class FlightOperationsUseCase(
    private val flightEntryRepository: FlightEntryRepository,
    private val userProfileRepository: UserProfileRepository
) {

    fun getAllFlights(userId: String): Flow<List<FlightEntry>> =
        flightEntryRepository.getAllFlights(userId)

    suspend fun getFlightById(id: Long): FlightEntry? =
        flightEntryRepository.getFlightById(id)

    suspend fun addFlight(flight: FlightEntry): Long {
        val flightId = flightEntryRepository.insertFlight(flight)
        updateUserProfileTotals(flight.userId)
        return flightId
    }

    suspend fun updateFlight(flight: FlightEntry) {
        flightEntryRepository.updateFlight(flight)
        updateUserProfileTotals(flight.userId)
    }

    suspend fun deleteFlight(flight: FlightEntry) {
        flightEntryRepository.deleteFlight(flight)
        updateUserProfileTotals(flight.userId)
    }

    suspend fun getLogbookSummary(userId: String): LogbookSummary =
        flightEntryRepository.getLogbookSummary(userId)

    suspend fun validateFlightEntry(flight: FlightEntry): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        if (flight.departureAerodrome.isBlank()) {
            errors.add(ValidationError.DEPARTURE_REQUIRED)
        }

        if (flight.arrivalAerodrome.isBlank()) {
            errors.add(ValidationError.ARRIVAL_REQUIRED)
        }

        if (flight.aircraftRegistration.isBlank()) {
            errors.add(ValidationError.AIRCRAFT_REGISTRATION_REQUIRED)
        }

        if (flight.totalFlightTime <= 0) {
            errors.add(ValidationError.TOTAL_TIME_REQUIRED)
        }

        val timeSum = flight.dayTime + flight.nightTime
        if (timeSum > flight.totalFlightTime + 0.1) {
            errors.add(ValidationError.DAY_NIGHT_EXCEEDS_TOTAL)
        }

        val roleTimeSum = flight.picTime + flight.dualTime + flight.coPilotTime + flight.instructorTime
        if (roleTimeSum > flight.totalFlightTime + 0.1) {
            errors.add(ValidationError.ROLE_TIME_EXCEEDS_TOTAL)
        }

        val ruleSum = flight.ifrTime + flight.vfrTime
        if (ruleSum > flight.totalFlightTime + 0.1) {
            errors.add(ValidationError.IFR_VFR_EXCEEDS_TOTAL)
        }

        if (flight.crossCountryTime > flight.totalFlightTime + 0.1) {
            errors.add(ValidationError.CROSS_COUNTRY_EXCEEDS_TOTAL)
        }

        if (flight.isSimulator && flight.simulatorTime <= 0) {
            errors.add(ValidationError.SIMULATOR_TIME_REQUIRED)
        }

        if (flight.instructorName != null && flight.instructorLicenseNumber.isNullOrBlank()) {
            errors.add(ValidationError.INSTRUCTOR_LICENSE_REQUIRED)
        }

        return errors
    }

    suspend fun searchFlights(userId: String, query: String): List<FlightEntry> =
        flightEntryRepository.searchFlights(userId, query)

    suspend fun getFlightsByDateRange(userId: String, startDate: String, endDate: String): List<FlightEntry> =
        flightEntryRepository.getFlightsByDateRange(userId, startDate, endDate)

    suspend fun getUnendorsedFlights(userId: String): List<FlightEntry> =
        flightEntryRepository.getUnendorsedFlights(userId)

    private suspend fun updateUserProfileTotals(userId: String) {
        val summary = flightEntryRepository.getLogbookSummary(userId)
        userProfileRepository.updateTotalHours(userId, summary.totalTime)
        userProfileRepository.updatePicHours(userId, summary.picTime)
        userProfileRepository.updateCrossCountryHours(userId, summary.crossCountryTime)
        userProfileRepository.updateNightHours(userId, summary.nightTime)
        userProfileRepository.updateIfrHours(userId, summary.ifrTime)
    }

    suspend fun calculateProgress(userId: String): PilotProgress {
        val summary = getLogbookSummary(userId)
        return PilotProgress(
            totalHours = summary.totalTime,
            picHours = summary.picTime,
            crossCountryHours = summary.crossCountryTime,
            nightHours = summary.nightTime,
            ifrHours = summary.ifrTime,
            commercialRequirement = CalculateCommercialRequirement(summary),
            atplRequirement = CalculateAtplRequirement(summary)
        )
    }

    private fun CalculateCommercialRequirement(summary: LogbookSummary): RequirementProgress {
        val required = CommercialRequirements()
        return RequirementProgress(
            totalRequired = required.totalTime,
            totalActual = summary.totalTime,
            picRequired = required.picTime,
            picActual = summary.picTime,
            crossCountryRequired = required.crossCountry,
            crossCountryActual = summary.crossCountryTime,
            nightRequired = required.night,
            nightActual = summary.nightTime
        )
    }

    private fun CalculateAtplRequirement(summary: LogbookSummary): RequirementProgress {
        val required = AtplRequirements()
        return RequirementProgress(
            totalRequired = required.totalTime,
            totalActual = summary.totalTime,
            picRequired = required.picTime,
            picActual = summary.picTime,
            crossCountryRequired = required.crossCountry,
            crossCountryActual = summary.crossCountryTime,
            nightRequired = required.night,
            nightActual = summary.nightTime
        )
    }
}

enum class ValidationError {
    DEPARTURE_REQUIRED,
    ARRIVAL_REQUIRED,
    AIRCRAFT_REGISTRATION_REQUIRED,
    TOTAL_TIME_REQUIRED,
    DAY_NIGHT_EXCEEDS_TOTAL,
    ROLE_TIME_EXCEEDS_TOTAL,
    IFR_VFR_EXCEEDS_TOTAL,
    CROSS_COUNTRY_EXCEEDS_TOTAL,
    SIMULATOR_TIME_REQUIRED,
    INSTRUCTOR_LICENSE_REQUIRED
}

data class PilotProgress(
    val totalHours: Double,
    val picHours: Double,
    val crossCountryHours: Double,
    val nightHours: Double,
    val ifrHours: Double,
    val commercialRequirement: RequirementProgress,
    val atplRequirement: RequirementProgress
)

data class RequirementProgress(
    val totalRequired: Double,
    val totalActual: Double,
    val picRequired: Double,
    val picActual: Double,
    val crossCountryRequired: Double,
    val crossCountryActual: Double,
    val nightRequired: Double,
    val nightActual: Double
)

data class CommercialRequirements(
    val totalTime: Double = 250.0,
    val picTime: Double = 100.0,
    val crossCountry: Double = 50.0,
    val night: Double = 10.0
)

data class AtplRequirements(
    val totalTime: Double = 1500.0,
    val picTime: Double = 250.0,
    val crossCountry: Double = 500.0,
    val night: Double = 100.0
)