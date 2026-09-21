package com.suyash.mockcivilaviationexam.domain.logbook

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime

class FlightTimeCalculatorTest {

    @Test
    fun `parses times with and without a separator`() {
        assertEquals(LocalTime.of(9, 30), FlightTimeCalculator.parse("0930"))
        assertEquals(LocalTime.of(9, 30), FlightTimeCalculator.parse("09:30"))
        assertEquals(LocalTime.of(9, 30), FlightTimeCalculator.parse("9:30"))
        assertEquals(LocalTime.of(23, 59), FlightTimeCalculator.parse("2359"))
        assertEquals(LocalTime.of(0, 0), FlightTimeCalculator.parse("0000"))
    }

    @Test
    fun `rejects invalid times instead of guessing`() {
        assertNull(FlightTimeCalculator.parse(""))
        assertNull(FlightTimeCalculator.parse(null))
        assertNull(FlightTimeCalculator.parse("09"))
        assertNull(FlightTimeCalculator.parse("2460"))
        assertNull(FlightTimeCalculator.parse("2400"))
        assertNull(FlightTimeCalculator.parse("abcd"))
    }

    @Test
    fun `computes a normal block time`() {
        val block = FlightTimeCalculator.durationHours(
            LocalTime.of(9, 30),
            LocalTime.of(11, 15)
        )
        assertEquals(1.75, block, 0.001)
    }

    @Test
    fun `a flight landing after midnight is not negative`() {
        val block = FlightTimeCalculator.durationHours(
            LocalTime.of(23, 30),
            LocalTime.of(1, 15)
        )
        assertEquals(1.75, block, 0.001)
    }

    @Test
    fun `identical times are a zero duration, not a full day`() {
        val block = FlightTimeCalculator.durationHours(
            LocalTime.of(10, 0),
            LocalTime.of(10, 0)
        )
        assertEquals(0.0, block, 0.001)
    }

    @Test
    fun `a missing time yields zero rather than an exception`() {
        assertEquals(0.0, FlightTimeCalculator.durationHours(null, LocalTime.of(10, 0)), 0.001)
        assertEquals(0.0, FlightTimeCalculator.durationHours(LocalTime.of(10, 0), null), 0.001)
    }

    @Test
    fun `ground time is block minus air and never negative`() {
        assertEquals(0.25, FlightTimeCalculator.groundTime(1.75, 1.5), 0.001)
        // Air time greater than block time is a data-entry error; clamp at zero
        // rather than reporting negative taxi time.
        assertEquals(0.0, FlightTimeCalculator.groundTime(1.0, 1.5), 0.001)
    }

    @Test
    fun `durations render as hours and minutes`() {
        assertEquals("1:45", FlightTimeCalculator.formatHoursMinutes(1.75))
        assertEquals("0:06", FlightTimeCalculator.formatHoursMinutes(0.1))
        assertEquals("0:00", FlightTimeCalculator.formatHoursMinutes(0.0))
        assertEquals("12:25", FlightTimeCalculator.formatHoursMinutes(12.4166666))
    }

    @Test
    fun `derived decimals are rounded, not repeating`() {
        // 09:30 to 10:55 is 85 minutes = 1.41666... hours
        val block = FlightTimeCalculator.durationHours(
            LocalTime.of(9, 30),
            LocalTime.of(10, 55)
        )
        assertEquals(1.42, block, 0.0001)
        assertEquals("1:25", FlightTimeCalculator.formatHoursMinutes(block))
    }
}
