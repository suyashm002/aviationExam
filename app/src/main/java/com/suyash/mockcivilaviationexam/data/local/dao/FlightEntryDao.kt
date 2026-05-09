package com.suyash.mockcivilaviationexam.data.local.dao

import androidx.room.*
import com.suyash.mockcivilaviationexam.data.local.entities.FlightEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightEntryDao {
    @Query("SELECT * FROM flight_entries WHERE userId = :userId ORDER BY date DESC, createdAt DESC")
    fun getAllFlights(userId: String): Flow<List<FlightEntryEntity>>

    @Query("SELECT * FROM flight_entries WHERE userId = :userId ORDER BY date DESC, createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getFlightsPaginated(userId: String, limit: Int, offset: Int): List<FlightEntryEntity>

    @Query("SELECT * FROM flight_entries WHERE id = :id")
    suspend fun getFlightById(id: Long): FlightEntryEntity?

    @Query("SELECT * FROM flight_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getFlightsByDateRange(userId: String, startDate: String, endDate: String): List<FlightEntryEntity>

    @Query("SELECT * FROM flight_entries WHERE userId = :userId AND aircraftRegistration = :registration ORDER BY date DESC")
    suspend fun getFlightsByAircraft(userId: String, registration: String): List<FlightEntryEntity>

    @Query("SELECT * FROM flight_entries WHERE userId = :userId AND isSimulator = :isSimulator ORDER BY date DESC")
    suspend fun getFlightsByType(userId: String, isSimulator: Boolean): List<FlightEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlight(flight: FlightEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlights(flights: List<FlightEntryEntity>)

    @Update
    suspend fun updateFlight(flight: FlightEntryEntity)

    @Delete
    suspend fun deleteFlight(flight: FlightEntryEntity)

    @Query("DELETE FROM flight_entries WHERE id = :id")
    suspend fun deleteFlightById(id: Long)

    @Query("SELECT COUNT(*) FROM flight_entries WHERE userId = :userId")
    suspend fun getFlightCount(userId: String): Int

    @Query("SELECT SUM(totalFlightTime) FROM flight_entries WHERE userId = :userId")
    suspend fun getTotalFlightTime(userId: String): Double?

    @Query("SELECT SUM(picTime) FROM flight_entries WHERE userId = :userId")
    suspend fun getTotalPicTime(userId: String): Double?

    @Query("SELECT SUM(crossCountryTime) FROM flight_entries WHERE userId = :userId")
    suspend fun getTotalCrossCountryTime(userId: String): Double?

    @Query("SELECT SUM(nightTime) FROM flight_entries WHERE userId = :userId")
    suspend fun getTotalNightTime(userId: String): Double?

    @Query("SELECT SUM(ifrTime) FROM flight_entries WHERE userId = :userId")
    suspend fun getTotalIfrTime(userId: String): Double?

    @Query("SELECT SUM(dualTime) FROM flight_entries WHERE userId = :userId")
    suspend fun getTotalDualTime(userId: String): Double?

    @Query("SELECT SUM(instructorTime) FROM flight_entries WHERE userId = :userId")
    suspend fun getTotalInstructorTime(userId: String): Double?

    @Query("SELECT SUM(simulatorTime) FROM flight_entries WHERE userId = :userId")
    suspend fun getTotalSimulatorTime(userId: String): Double?

    @Query("""
        SELECT 
            COALESCE(SUM(totalFlightTime), 0.0) as totalTime,
            COALESCE(SUM(picTime), 0.0) as picTime,
            COALESCE(SUM(dualTime), 0.0) as dualTime,
            COALESCE(SUM(coPilotTime), 0.0) as coPilotTime,
            COALESCE(SUM(instructorTime), 0.0) as instructorTime,
            COALESCE(SUM(crossCountryTime), 0.0) as crossCountryTime,
            COALESCE(SUM(nightTime), 0.0) as nightTime,
            COALESCE(SUM(ifrTime), 0.0) as ifrTime,
            COALESCE(SUM(CASE WHEN ifrTime = 0 THEN totalFlightTime ELSE 0 END), 0.0) as vfrTime,
            COALESCE(SUM(simulatorTime), 0.0) as simulatorTime
        FROM flight_entries 
        WHERE userId = :userId
    """)
    suspend fun getLogbookSummary(userId: String): LogbookSummaryQuery

    @Query("SELECT DISTINCT aircraftRegistration FROM flight_entries WHERE userId = :userId ORDER BY aircraftRegistration")
    suspend fun getDistinctAircraftRegistrations(userId: String): List<String>

    @Query("SELECT DISTINCT aircraftType FROM flight_entries WHERE userId = :userId ORDER BY aircraftType")
    suspend fun getDistinctAircraftTypes(userId: String): List<String>

    @Query("""
        SELECT * FROM flight_entries 
        WHERE userId = :userId 
        AND (
            departureAerodrome LIKE '%' || :searchQuery || '%' OR
            arrivalAerodrome LIKE '%' || :searchQuery || '%' OR
            aircraftRegistration LIKE '%' || :searchQuery || '%' OR
            aircraftType LIKE '%' || :searchQuery || '%' OR
            remarks LIKE '%' || :searchQuery || '%'
        )
        ORDER BY date DESC, createdAt DESC
    """)
    suspend fun searchFlights(userId: String, searchQuery: String): List<FlightEntryEntity>

    @Query("SELECT * FROM flight_entries WHERE userId = :userId AND isEndorsed = 0")
    suspend fun getUnendorsedFlights(userId: String): List<FlightEntryEntity>
}

data class LogbookSummaryQuery(
    val totalTime: Double,
    val picTime: Double,
    val dualTime: Double,
    val coPilotTime: Double,
    val instructorTime: Double,
    val crossCountryTime: Double,
    val nightTime: Double,
    val ifrTime: Double,
    val vfrTime: Double,
    val simulatorTime: Double
)