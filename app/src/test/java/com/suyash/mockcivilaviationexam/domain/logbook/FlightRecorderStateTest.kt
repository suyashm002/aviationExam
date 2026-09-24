package com.suyash.mockcivilaviationexam.domain.logbook

import org.junit.Assert.assertEquals
import org.junit.Test

class FlightRecorderStateTest {

    @Test
    fun `block, air and ground minutes come from the four timestamps`() {
        val min = 60_000L
        val s = RecorderState(
            sessionId = "s",
            phase = FlightPhase.COMPLETE,
            offBlockAt = 0,
            takeoffAt = 12 * min,
            landingAt = 72 * min,
            onBlockAt = 80 * min,
            airborneMillisClosed = 60 * min
        )
        val now = 200 * min
        assertEquals(80 * min, s.blockMillis(now))
        assertEquals(60 * min, s.airMillis(now))
        assertEquals(20 * min, s.groundMillis(now))
    }

    @Test
    fun `while airborne the open segment counts up to now`() {
        val min = 60_000L
        val s = RecorderState(
            sessionId = "s",
            phase = FlightPhase.AIRBORNE,
            offBlockAt = 0,
            takeoffAt = 10 * min,
            airborneSince = 10 * min,
            airborneMillisClosed = 0
        )
        assertEquals(25 * min, s.airMillis(35 * min))
        assertEquals(35 * min, s.blockMillis(35 * min))
    }

    @Test
    fun `circuits add up their airborne segments`() {
        val min = 60_000L
        val s = RecorderState(
            sessionId = "s",
            phase = FlightPhase.TAXI_IN,
            offBlockAt = 0,
            airborneMillisClosed = 15 * min + 12 * min,
            landings = 2
        )
        assertEquals(27 * min, s.airMillis(60 * min))
    }
}
