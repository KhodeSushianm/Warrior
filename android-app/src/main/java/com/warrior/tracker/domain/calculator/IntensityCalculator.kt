package com.warrior.tracker.domain.calculator

/**
 * Intensity — ARCHITECTURE.md sec.7.4.
 * Activity intensity = duration-weighted average of round intensities (derived, never stored).
 * Rounds without intensity are excluded from BOTH numerator and denominator.
 */
object IntensityCalculator {

    data class RoundLike(val durationSec: Int?, val intensity: Int?)

    /** Duration-weighted mean intensity, or null when no round carries an intensity. */
    fun weightedAverageIntensity(rounds: List<RoundLike>): Double? {
        var weightedSum = 0.0
        var totalWeight = 0L
        for (r in rounds) {
            val i = r.intensity ?: continue
            val w = (r.durationSec ?: 0).coerceAtLeast(0).toLong()
            // Zero-duration rounds still count with weight 1 so they aren't silently dropped.
            val weight = if (w == 0L) 1L else w
            weightedSum += i * weight
            totalWeight += weight
        }
        if (totalWeight == 0L) return null
        return weightedSum / totalWeight
    }

    /** UI semantic band (sec.7.4): 1–3 سبک، ۴–۶ متوسط، ۷–۸ سخت، ۹–۱۰ حداکثر. */
    fun intensityBand(value: Int): Band = when (value) {
        in 1..3 -> Band.LIGHT
        in 4..6 -> Band.MODERATE
        in 7..8 -> Band.HARD
        in 9..10 -> Band.MAXIMAL
        else -> throw IllegalArgumentException("intensity out of range: $value")
    }

    enum class Band { LIGHT, MODERATE, HARD, MAXIMAL }
}
