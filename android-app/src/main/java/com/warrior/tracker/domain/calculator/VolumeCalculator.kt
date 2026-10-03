package com.warrior.tracker.domain.calculator

import com.warrior.tracker.core.common.LoadType
import com.warrior.tracker.core.common.MeasureType

/**
 * Volume & Strength metrics — ARCHITECTURE.md sec.7.1.
 * Pure Kotlin; only completed, non-WARMUP sets are counted (caller filters).
 */
object VolumeCalculator {

    /** One set's volume: external_load × reps (sec.7.1). BODYWEIGHT/ASSISTED contribute 0 tonnage. */
    fun setVolume(
        loadType: LoadType,
        measureType: MeasureType,
        externalLoadKg: Double,
        reps: Int?,
        durationSec: Int?,
    ): Double = when {
        measureType == MeasureType.DURATION -> 0.0
        loadType == LoadType.BODYWEIGHT || loadType == LoadType.ASSISTED -> 0.0
        else -> externalLoadKg * (reps ?: 0)
    }

    /** Tonnage of a group of sets: SUM(external_load × reps) for WEIGHTED / BODYWEIGHT_PLUS. */
    fun tonnage(sets: List<SetLike>, loadType: LoadType): Double =
        sets.sumOf { setVolume(loadType, it.measureType, it.externalLoadKg, it.reps, it.durationSec) }

    /** Total reps — primary metric for BODYWEIGHT and ASSISTED (sec.7.1). */
    fun totalReps(sets: List<SetLike>): Int =
        sets.filter { it.measureType == MeasureType.REPS }.sumOf { it.reps ?: 0 }

    /** Total hold time — primary metric for DURATION exercises (sec.7.1). */
    fun totalTimeSec(sets: List<SetLike>): Int =
        sets.filter { it.measureType == MeasureType.DURATION }.sumOf { it.durationSec ?: 0 }

    /**
     * Effective Volume for BODYWEIGHT_PLUS when a body-weight snapshot exists (sec.7.1).
     * Returns null (never 0 or a guess) when body weight is unknown.
     */
    fun effectiveVolume(
        sets: List<SetLike>,
        loadType: LoadType,
        bodyWeightKgSnapshot: Double?,
    ): Double? {
        if (loadType != LoadType.BODYWEIGHT_PLUS) return null
        val bw = bodyWeightKgSnapshot ?: return null
        return sets.sumOf { (bw + it.externalLoadKg) * (it.reps ?: 0) }
    }

    /** Minimal shape the calculator needs — keeps it independent from Room entities. */
    data class SetLike(
        val measureType: MeasureType,
        val externalLoadKg: Double,
        val reps: Int?,
        val durationSec: Int?,
    )
}
