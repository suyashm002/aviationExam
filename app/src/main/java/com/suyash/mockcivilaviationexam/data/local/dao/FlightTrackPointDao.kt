package com.suyash.mockcivilaviationexam.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.suyash.mockcivilaviationexam.data.local.entities.FlightTrackPointEntity

@Dao
interface FlightTrackPointDao {
    @Insert
    suspend fun insertAll(points: List<FlightTrackPointEntity>)

    @Insert
    suspend fun insert(point: FlightTrackPointEntity): Long

    @Query("SELECT * FROM flight_track_points WHERE flightId = :flightId ORDER BY timestamp")
    suspend fun getForFlight(flightId: Long): List<FlightTrackPointEntity>

    @Query("SELECT * FROM flight_track_points WHERE sessionId = :sessionId ORDER BY timestamp")
    suspend fun getForSession(sessionId: String): List<FlightTrackPointEntity>

    @Query("SELECT COUNT(*) FROM flight_track_points WHERE sessionId = :sessionId")
    suspend fun countForSession(sessionId: String): Int

    @Query("UPDATE flight_track_points SET flightId = :flightId WHERE sessionId = :sessionId")
    suspend fun attachSessionToFlight(sessionId: String, flightId: Long): Int

    @Query("DELETE FROM flight_track_points WHERE flightId = :flightId")
    suspend fun deleteForFlight(flightId: Long)

    @Query("DELETE FROM flight_track_points WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: String)

    /** Points from abandoned sessions that were never attached to a flight. */
    @Query("DELETE FROM flight_track_points WHERE flightId IS NULL AND timestamp < :olderThan")
    suspend fun deleteOrphansOlderThan(olderThan: Long): Int
}
