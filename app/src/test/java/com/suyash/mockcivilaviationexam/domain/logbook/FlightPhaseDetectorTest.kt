package com.suyash.mockcivilaviationexam.domain.logbook

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlightPhaseDetectorTest {

    private fun detector() = FlightPhaseDetector(
        takeoffSpeedKt = 40.0,
        landingSpeedKt = 30.0,
        takeoffConfirmMs = 3_000,
        landingConfirmMs = 20_000
    )

    @Test
    fun `a takeoff is confirmed after the speed is held, and stamped at the first fast sample`() {
        val d = detector()
        assertNull(d.onSample(0, 10.0))
        assertNull(d.onSample(1_000, 45.0))      // first fast sample
        assertNull(d.onSample(2_000, 50.0))
        val event = d.onSample(4_000, 60.0)      // 3 s held
        assertEquals(FlightPhaseDetector.Event.Takeoff(1_000), event)
        assertTrue(d.airborne)
    }

    @Test
    fun `a single GPS spike on the ramp does not start the flight`() {
        val d = detector()
        assertNull(d.onSample(0, 80.0))
        assertNull(d.onSample(1_000, 5.0))
        assertNull(d.onSample(5_000, 5.0))
        assertFalse(d.airborne)
    }

    @Test
    fun `a landing needs the slow speed sustained, and is stamped when it first went slow`() {
        val d = detector()
        d.onSample(0, 50.0); d.onSample(3_000, 50.0)
        assertTrue(d.airborne)
        assertNull(d.onSample(60_000, 25.0))     // touched down, rolling out
        assertNull(d.onSample(70_000, 10.0))
        val event = d.onSample(80_000, 8.0)      // 20 s below threshold
        assertEquals(FlightPhaseDetector.Event.Landing(60_000), event)
        assertFalse(d.airborne)
    }

    @Test
    fun `slowing briefly in the air is not a landing`() {
        val d = detector()
        d.onSample(0, 50.0); d.onSample(3_000, 50.0)
        assertNull(d.onSample(60_000, 28.0))     // strong headwind on final, say
        assertNull(d.onSample(65_000, 70.0))     // go-around
        assertNull(d.onSample(90_000, 70.0))
        assertTrue(d.airborne)
    }

    @Test
    fun `a manual override resets the timers`() {
        val d = detector()
        d.onSample(0, 45.0)
        d.forceAirborne(true)
        assertTrue(d.airborne)
        assertNull(d.onSample(1_000, 60.0))     // no second Takeoff event
        d.forceAirborne(false)
        assertFalse(d.airborne)
    }
}
