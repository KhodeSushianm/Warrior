package com.warrior.tracker.domain.calculator

import com.warrior.tracker.core.common.PRType

/**
 * e1RM & Personal Records — ARCHITECTURE.md sec.7.3.
 * Epley: e1RM = w × (1 + reps / 30); reps = 1 → exactly w. Only meaningful for reps 1..12 (BEST_E1RM).
 */
object Epley {
    fun e1rm(weightKg: Double, reps: Int): Double {
        require(reps >= 1) { "reps must be >= 1" }
        // ARCHITECTURE.md sec.7.3: for reps = 1 the estimate equals the lifted weight.
        if (reps == 1) return weightKg
        return weightKg * (1.0 + reps / 30.0)
    }
}

/**
 * PR detection over historical bests. Strict improvement only — ties are NOT new PRs (sec.7.3).
 */
object PrCalculator {

    /** A candidate set beats the current record iff its value is strictly greater. */
    fun isNewRecord(candidate: Double, currentBest: Double?): Boolean =
        currentBest == null || candidate > currentBest

    /** Value used by each PR type given a set; null when the type doesn't apply to this set. */
    fun prValue(
        prType: PRType,
        loadTypeApplies: Boolean, // caller resolves sec.7.3 applicability matrix per exercise
        measureTypeIsDuration: Boolean,
        externalLoadKg: Double,
        reps: Int?,
        durationSec: Int?,
    ): Double? = when (prType) {
        PRType.MAX_WEIGHT ->
            if (loadTypeApplies && reps != null && reps >= 1) externalLoadKg else null

        PRType.REPS_AT_WEIGHT ->
            if (!measureTypeIsDuration && reps != null) reps.toDouble() else null

        PRType.BEST_E1RM ->
            if (!measureTypeIsDuration && reps != null && reps in 1..12) {
                if (reps == 1) externalLoadKg else Epley.e1rm(externalLoadKg, reps)
            } else null

        PRType.BEST_SET_VOLUME ->
            if (loadTypeApplies && reps != null) externalLoadKg * reps else null

        PRType.MAX_DURATION ->
            if (measureTypeIsDuration) (durationSec?.toDouble()) else null
    }
}
