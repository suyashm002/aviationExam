package com.suyash.mockcivilaviationexam.domain.logbook

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** One GPS fix. Altitude in feet, groundspeed in knots, timestamp in epoch millis. */
data class TrackPoint(
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val altitudeFt: Double,
    val groundSpeedKt: Double,
    val bearingDeg: Float? = null
)

/**
 * Running statistics over a GPS track: distance flown, peak altitude and peak
 * groundspeed. Pure Kotlin so it is unit-testable and reusable by the detail
 * screen, which rebuilds the same numbers from stored points.
 */
class TrackStats {
    var distanceNm: Double = 0.0
        private set
    var maxAltitudeFt: Double = 0.0
        private set
    var maxGroundSpeedKt: Double = 0.0
        private set
    var pointCount: Int = 0
        private set

    private var last: TrackPoint? = null

    fun add(point: TrackPoint) {
        pointCount++
        if (point.altitudeFt > maxAltitudeFt) maxAltitudeFt = point.altitudeFt
        if (point.groundSpeedKt > maxGroundSpeedKt) maxGroundSpeedKt = point.groundSpeedKt

        val previous = last
        if (previous != null) {
            val leg = distanceNm(previous, point)
            // A parked aircraft still "moves" a few metres per fix as the GPS
            // solution wanders. Ignore legs that a taxiing aircraft could not
            // plausibly have covered, so distance does not creep on the ramp.
            if (point.groundSpeedKt >= MIN_MOVING_SPEED_KT || leg >= MIN_LEG_NM) {
                distanceNm += leg
            }
        }
        last = point
    }

    fun addAll(points: Iterable<TrackPoint>) = points.forEach(::add)

    companion object {
        private const val EARTH_RADIUS_NM = 3440.065
        private const val MIN_MOVING_SPEED_KT = 3.0
        private const val MIN_LEG_NM = 0.05

        /** Great-circle distance between two fixes in nautical miles. */
        fun distanceNm(a: TrackPoint, b: TrackPoint): Double {
            val lat1 = Math.toRadians(a.latitude)
            val lat2 = Math.toRadians(b.latitude)
            val dLat = lat2 - lat1
            val dLon = Math.toRadians(b.longitude - a.longitude)
            val h = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
            return 2 * EARTH_RADIUS_NM * atan2(sqrt(h), sqrt(1 - h))
        }
    }
}
