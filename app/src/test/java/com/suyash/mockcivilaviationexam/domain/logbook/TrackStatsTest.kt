package com.suyash.mockcivilaviationexam.domain.logbook

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackStatsTest {

    private fun p(t: Long, lat: Double, lon: Double, alt: Double = 5000.0, gs: Double = 100.0) =
        TrackPoint(t, lat, lon, alt, gs)

    @Test
    fun `one degree of latitude is sixty nautical miles`() {
        val d = TrackStats.distanceNm(p(0, -1.0, 36.0), p(1, 0.0, 36.0))
        assertEquals(60.0, d, 0.1)
    }

    @Test
    fun `distance, peak altitude and peak speed accumulate`() {
        val s = TrackStats()
        s.add(p(0, -1.0, 36.0, alt = 5200.0, gs = 90.0))
        s.add(p(1, -0.5, 36.0, alt = 6500.0, gs = 110.0))
        s.add(p(2, 0.0, 36.0, alt = 6100.0, gs = 95.0))
        assertEquals(60.0, s.distanceNm, 0.1)
        assertEquals(6500.0, s.maxAltitudeFt, 0.0)
        assertEquals(110.0, s.maxGroundSpeedKt, 0.0)
        assertEquals(3, s.pointCount)
    }

    @Test
    fun `GPS wander while parked does not add distance`() {
        val s = TrackStats()
        // ~5 m jitter at 0 kt, repeated
        s.add(p(0, -1.3000, 36.8000, gs = 0.0))
        s.add(p(1, -1.3000, 36.80005, gs = 0.0))
        s.add(p(2, -1.30004, 36.8000, gs = 1.0))
        assertEquals(0.0, s.distanceNm, 0.0001)
    }
}
