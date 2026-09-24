package com.suyash.mockcivilaviationexam.domain.logbook

/**
 * Turns a stream of groundspeed samples into takeoff and landing events, the
 * way ForeFlight and Garmin Pilot auto-log a flight.
 *
 * - **Takeoff**: groundspeed at or above [takeoffSpeedKt] for [takeoffConfirmMs]
 *   (a few seconds), so a GPS spike on the ramp does not start the clock.
 * - **Landing**: while airborne, groundspeed at or below [landingSpeedKt] for
 *   [landingConfirmMs]. That is long enough to roll out and clear the runway but
 *   short enough that a full-stop-and-taxi-back is not counted as air time.
 *
 * A touch-and-go usually never drops below the landing speed, so it is
 * deliberately NOT detected here — the aircraft is still flying, so the air
 * time is right, and the pilot adds the landing with the +1 button. The
 * defaults match ForeFlight's 40 kt start rule.
 */
class FlightPhaseDetector(
    private val takeoffSpeedKt: Double = 40.0,
    private val landingSpeedKt: Double = 30.0,
    private val takeoffConfirmMs: Long = 3_000,
    private val landingConfirmMs: Long = 20_000
) {
    sealed class Event {
        /** [at] is the timestamp of the first fast sample, not when it was confirmed. */
        data class Takeoff(val at: Long) : Event()
        /** [at] is the timestamp of the first slow sample. */
        data class Landing(val at: Long) : Event()
    }

    var airborne: Boolean = false
        private set

    private var fastSince: Long? = null
    private var slowSince: Long? = null

    /** Feed one sample; returns an event when a phase change is confirmed. */
    fun onSample(timestamp: Long, groundSpeedKt: Double): Event? {
        return if (!airborne) {
            if (groundSpeedKt >= takeoffSpeedKt) {
                val since = fastSince ?: timestamp.also { fastSince = it }
                if (timestamp - since >= takeoffConfirmMs) {
                    airborne = true
                    fastSince = null
                    slowSince = null
                    Event.Takeoff(since)
                } else null
            } else {
                fastSince = null
                null
            }
        } else {
            if (groundSpeedKt <= landingSpeedKt) {
                val since = slowSince ?: timestamp.also { slowSince = it }
                if (timestamp - since >= landingConfirmMs) {
                    airborne = false
                    slowSince = null
                    fastSince = null
                    Event.Landing(since)
                } else null
            } else {
                slowSince = null
                null
            }
        }
    }

    /** The pilot pressed the button; trust them over the GPS. */
    fun forceAirborne(value: Boolean) {
        airborne = value
        fastSince = null
        slowSince = null
    }
}
