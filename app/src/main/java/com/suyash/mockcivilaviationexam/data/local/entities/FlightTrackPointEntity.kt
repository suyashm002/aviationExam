package com.suyash.mockcivilaviationexam.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One GPS fix from the in-flight recorder.
 *
 * Points are written under a [sessionId] while the flight is in progress, because
 * the logbook row does not exist yet. When the pilot saves the entry the session's
 * points are re-homed to that [flightId] in one UPDATE.
 */
@Entity(
    tableName = "flight_track_points",
    indices = [Index("flightId"), Index("sessionId")]
)
data class FlightTrackPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val flightId: Long? = null,
    /** Epoch millis of the fix. */
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    /** GPS altitude above mean sea level, in feet. */
    val altitudeFt: Double,
    val groundSpeedKt: Double,
    /** Track over the ground in degrees true, when the receiver reports it. */
    val bearingDeg: Float? = null
)
