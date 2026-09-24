package com.suyash.mockcivilaviationexam.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class FlightEntryTest {

    private fun entry(block: Double, air: Double) = FlightEntry(
        date = LocalDate.of(2026, 9, 24),
        departureAerodrome = "HKNW",
        arrivalAerodrome = "HKNW",
        aircraftType = "C172",
        aircraftModel = "172N",
        aircraftRegistration = "5Y-TST",
        totalFlightTime = block,
        blockTime = block,
        airTime = air,
        userId = "u"
    )

    @Test
    fun `flight and ground minutes split the block time`() {
        val e = entry(block = 1.5, air = 1.2)
        assertEquals(72, e.flightMinutes)
        assertEquals(18, e.groundMinutes)
    }

    @Test
    fun `route summary includes the via leg only when present`() {
        val e = entry(1.0, 0.8)
        assertEquals("HKNW - HKNW", e.routeSummary)
        assertEquals("HKNW - Ngong Hills - HKNW", e.copy(routeVia = "Ngong Hills").routeSummary)
    }

    @Test
    fun `hobbs time is the meter difference to a tenth`() {
        assertEquals(1.4, entry(1.0, 0.8).copy(hobbsStart = 2345.6, hobbsEnd = 2347.0).hobbsTime!!, 0.0001)
        assertNull(entry(1.0, 0.8).copy(hobbsStart = 10.0).hobbsTime)
    }
}
