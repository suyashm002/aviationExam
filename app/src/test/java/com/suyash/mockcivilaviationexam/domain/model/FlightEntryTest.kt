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
    fun `a flight back to the same aerodrome is described as local`() {
        val e = entry(1.0, 0.8)
        assertEquals(true, e.isLocal)
        assertEquals("Local · HKNW", e.routeSummary)
        assertEquals("Local · HKNW · Ngong Hills area", e.copy(routeVia = "Ngong Hills area").routeSummary)
    }

    @Test
    fun `a cross-country lists departure, via and arrival`() {
        val e = entry(1.0, 0.8).copy(arrivalAerodrome = "HKKR")
        assertEquals(false, e.isLocal)
        assertEquals("HKNW - HKKR", e.routeSummary)
        assertEquals("HKNW - Naivasha - HKKR", e.copy(routeVia = "Naivasha").routeSummary)
    }

    @Test
    fun `dual or solo is read back from the role columns`() {
        assertEquals(FlightRole.DUAL, entry(1.0, 0.8).copy(dualTime = 1.0).role)
        assertEquals(FlightRole.SOLO, entry(1.0, 0.8).copy(picTime = 1.0).role)
        assertEquals(FlightRole.OTHER, entry(1.0, 0.8).copy(picTime = 0.5, dualTime = 0.5).role)
    }

    @Test
    fun `hobbs time is the meter difference to a tenth`() {
        assertEquals(1.4, entry(1.0, 0.8).copy(hobbsStart = 2345.6, hobbsEnd = 2347.0).hobbsTime!!, 0.0001)
        assertNull(entry(1.0, 0.8).copy(hobbsStart = 10.0).hobbsTime)
    }
}
