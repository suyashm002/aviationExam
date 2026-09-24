package com.suyash.mockcivilaviationexam.data.local.repository

import com.suyash.mockcivilaviationexam.data.local.dao.FlightTrackPointDao
import com.suyash.mockcivilaviationexam.data.local.entities.FlightTrackPointEntity
import com.suyash.mockcivilaviationexam.domain.logbook.TrackPoint

class FlightTrackRepository(private val dao: FlightTrackPointDao) {

    suspend fun append(sessionId: String, point: TrackPoint) {
        dao.insert(point.toEntity(sessionId))
    }

    suspend fun appendAll(sessionId: String, points: List<TrackPoint>) {
        if (points.isEmpty()) return
        dao.insertAll(points.map { it.toEntity(sessionId) })
    }

    suspend fun getTrack(flightId: Long): List<TrackPoint> =
        dao.getForFlight(flightId).map { it.toDomain() }

    suspend fun getSessionTrack(sessionId: String): List<TrackPoint> =
        dao.getForSession(sessionId).map { it.toDomain() }

    suspend fun sessionPointCount(sessionId: String): Int = dao.countForSession(sessionId)

    /** Re-homes a recorder session's points to the saved logbook row. Returns rows moved. */
    suspend fun attachToFlight(sessionId: String, flightId: Long): Int =
        dao.attachSessionToFlight(sessionId, flightId)

    suspend fun deleteForFlight(flightId: Long) = dao.deleteForFlight(flightId)

    suspend fun discardSession(sessionId: String) = dao.deleteForSession(sessionId)

    suspend fun purgeOrphans(olderThanMillis: Long): Int = dao.deleteOrphansOlderThan(olderThanMillis)

    private fun TrackPoint.toEntity(sessionId: String) = FlightTrackPointEntity(
        sessionId = sessionId,
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude,
        altitudeFt = altitudeFt,
        groundSpeedKt = groundSpeedKt,
        bearingDeg = bearingDeg
    )

    private fun FlightTrackPointEntity.toDomain() = TrackPoint(
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude,
        altitudeFt = altitudeFt,
        groundSpeedKt = groundSpeedKt,
        bearingDeg = bearingDeg
    )
}
