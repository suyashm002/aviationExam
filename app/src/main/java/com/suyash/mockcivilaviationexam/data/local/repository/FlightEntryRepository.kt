package com.suyash.mockcivilaviationexam.data.local.repository

import com.suyash.mockcivilaviationexam.domain.logbook.LogbookUser
import com.suyash.mockcivilaviationexam.data.local.dao.FlightEntryDao
import com.suyash.mockcivilaviationexam.data.local.entities.toDomainModel
import com.suyash.mockcivilaviationexam.data.local.entities.toEntity
import com.suyash.mockcivilaviationexam.domain.model.FlightEntry
import com.suyash.mockcivilaviationexam.domain.model.LogbookSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
class FlightEntryRepository(
    private val flightEntryDao: FlightEntryDao
) {
    fun getAllFlights(userId: String): Flow<List<FlightEntry>> =
        flightEntryDao.getAllFlights(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }

    suspend fun getFlightsPaginated(userId: String, limit: Int, offset: Int): List<FlightEntry> =
        flightEntryDao.getFlightsPaginated(userId, limit, offset).map { it.toDomainModel() }

    suspend fun claimLegacyEntries(newUserId: String): Int =
        flightEntryDao.reassignUserId(LogbookUser.LEGACY_USER_ID, newUserId)

    suspend fun getFlightById(id: Long): FlightEntry? =
        flightEntryDao.getFlightById(id)?.toDomainModel()

    suspend fun getFlightsByDateRange(userId: String, startDate: String, endDate: String): List<FlightEntry> =
        flightEntryDao.getFlightsByDateRange(userId, startDate, endDate).map { it.toDomainModel() }

    suspend fun getFlightsByAircraft(userId: String, registration: String): List<FlightEntry> =
        flightEntryDao.getFlightsByAircraft(userId, registration).map { it.toDomainModel() }

    suspend fun getFlightsByType(userId: String, isSimulator: Boolean): List<FlightEntry> =
        flightEntryDao.getFlightsByType(userId, isSimulator).map { it.toDomainModel() }

    suspend fun insertFlight(flight: FlightEntry): Long =
        flightEntryDao.insertFlight(flight.toEntity())

    suspend fun insertFlights(flights: List<FlightEntry>) =
        flightEntryDao.insertFlights(flights.map { it.toEntity() })

    suspend fun updateFlight(flight: FlightEntry) =
        flightEntryDao.updateFlight(flight.copy(lastModified = System.currentTimeMillis()).toEntity())

    suspend fun deleteFlight(flight: FlightEntry) =
        flightEntryDao.deleteFlight(flight.toEntity())

    suspend fun deleteFlightById(id: Long) =
        flightEntryDao.deleteFlightById(id)

    suspend fun getFlightCount(userId: String): Int =
        flightEntryDao.getFlightCount(userId)

    suspend fun getTotalFlightTime(userId: String): Double =
        flightEntryDao.getTotalFlightTime(userId) ?: 0.0

    suspend fun getTotalPicTime(userId: String): Double =
        flightEntryDao.getTotalPicTime(userId) ?: 0.0

    suspend fun getTotalCrossCountryTime(userId: String): Double =
        flightEntryDao.getTotalCrossCountryTime(userId) ?: 0.0

    suspend fun getTotalNightTime(userId: String): Double =
        flightEntryDao.getTotalNightTime(userId) ?: 0.0

    suspend fun getTotalIfrTime(userId: String): Double =
        flightEntryDao.getTotalIfrTime(userId) ?: 0.0

    suspend fun getTotalDualTime(userId: String): Double =
        flightEntryDao.getTotalDualTime(userId) ?: 0.0

    suspend fun getTotalInstructorTime(userId: String): Double =
        flightEntryDao.getTotalInstructorTime(userId) ?: 0.0

    suspend fun getTotalSimulatorTime(userId: String): Double =
        flightEntryDao.getTotalSimulatorTime(userId) ?: 0.0

    suspend fun getLogbookSummary(userId: String): LogbookSummary {
        val summary = flightEntryDao.getLogbookSummary(userId)
        return LogbookSummary(
            totalTime = summary.totalTime,
            picTime = summary.picTime,
            dualTime = summary.dualTime,
            coPilotTime = summary.coPilotTime,
            instructorTime = summary.instructorTime,
            crossCountryTime = summary.crossCountryTime,
            nightTime = summary.nightTime,
            ifrTime = summary.ifrTime,
            vfrTime = summary.vfrTime,
            simulatorTime = summary.simulatorTime,
            blockTime = summary.blockTime,
            airTime = summary.airTime,
            dayLandings = summary.dayLandings,
            nightLandings = summary.nightLandings,
            totalLandings = summary.dayLandings + summary.nightLandings
        )
    }

    /**
     * Landings in the last 90 days, in real aircraft. This is the recency rule
     * a pilot must satisfy before carrying passengers, so it is worth surfacing
     * rather than making the pilot count rows by hand.
     */
    suspend fun getRecentLandings(userId: String): Pair<Int, Int> {
        val since = java.time.LocalDate.now().minusDays(90)
            .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        return flightEntryDao.getLandingsSince(userId, since) to
            flightEntryDao.getNightLandingsSince(userId, since)
    }

    suspend fun getDistinctAircraftRegistrations(userId: String): List<String> =
        flightEntryDao.getDistinctAircraftRegistrations(userId)

    suspend fun getDistinctAircraftTypes(userId: String): List<String> =
        flightEntryDao.getDistinctAircraftTypes(userId)

    suspend fun searchFlights(userId: String, searchQuery: String): List<FlightEntry> =
        flightEntryDao.searchFlights(userId, searchQuery).map { it.toDomainModel() }

    suspend fun getUnendorsedFlights(userId: String): List<FlightEntry> =
        flightEntryDao.getUnendorsedFlights(userId).map { it.toDomainModel() }
}