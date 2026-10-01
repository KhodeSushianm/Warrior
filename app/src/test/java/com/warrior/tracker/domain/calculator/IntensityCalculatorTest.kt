package com.warrior.tracker.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** ARCHITECTURE.md sec.7.4 — duration-weighted average intensity. */
class IntensityCalculatorTest {

    private fun round(dur: Int?, intensity: Int?) =
        IntensityCalculator.RoundLike(dur, intensity)

    @Test
    fun `null when no round carries intensity`() {
        assertNull(IntensityCalculator.weightedAverageIntensity(listOf(round(180, null))))
        assertNull(IntensityCalculator.weightedAverageIntensity(emptyList()))
    }

    @Test
    fun `weighted by duration`() {
        // (8*180 + 4*60) / 240 = 7.0
        val rounds = listOf(round(180, 8), round(60, 4))
        assertEquals(7.0, IntensityCalculator.weightedAverageIntensity(rounds)!!, 1e-9)
    }

    @Test
    fun `rounds without intensity excluded from numerator and denominator`() {
        val rounds = listOf(round(180, null), round(120, 10))
        assertEquals(10.0, IntensityCalculator.weightedAverageIntensity(rounds)!!, 1e-9)
    }

    @Test
    fun `zero duration rated round keeps weight of one`() {
        val rounds = listOf(round(0, 10), round(180, 8))
        val expected = (10 * 1 + 8 * 180) / 181.0
        assertEquals(expected, IntensityCalculator.weightedAverageIntensity(rounds)!!, 1e-9)
    }

    @Test
    fun `semantic bands match spec`() {
        assertEquals(IntensityCalculator.Band.LIGHT, IntensityCalculator.intensityBand(2))
        assertEquals(IntensityCalculator.Band.MODERATE, IntensityCalculator.intensityBand(5))
        assertEquals(IntensityCalculator.Band.HARD, IntensityCalculator.intensityBand(7))
        assertEquals(IntensityCalculator.Band.MAXIMAL, IntensityCalculator.intensityBand(10))
    }
}
