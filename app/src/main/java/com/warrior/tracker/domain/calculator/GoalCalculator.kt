package com.warrior.tracker.domain.calculator

import com.warrior.tracker.core.common.GoalPeriod
import com.warrior.tracker.core.common.GoalType
import com.warrior.tracker.core.common.GoalUnit
import com.warrior.tracker.core.common.derivedUnit
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Goal progress — ARCHITECTURE.md sec.7.5.
 * `current` is never stored; direction is inferred from start vs target;
 * periodic goals count only inside the current period window.
 */
object GoalCalculator {

    /**
     * progress = clamp((current − start) / (target − start), 0, 1). start defaults to 0 when absent.
     *
     * A degenerate goal (`start == target`, zero span) cannot be interpolated; it reports 1.0 only
     * when [isAchieved] agrees, so the progress bar and the ACHIEVED badge never contradict
     * each other. Non-finite input yields 0.0 rather than NaN (which would poison the UI).
     */
    fun progress(current: Double, start: Double?, target: Double): Double {
        if (!current.isFinite()) return 0.0
        val s = start ?: 0.0
        val span = target - s
        if (span == 0.0) return if (isAchieved(current, start, target)) 1.0 else 0.0
        return ((current - s) / span).coerceIn(0.0, 1.0)
    }

    /** True when the goal value has been reached/exceeded in its direction (sec.7.5). */
    fun isAchieved(current: Double, start: Double?, target: Double): Boolean {
        val s = start ?: 0.0
        return if (target >= s) current >= target else current <= target
    }

    fun unitOf(type: GoalType): GoalUnit = type.derivedUnit()

    /**
     * Current-period window start..end inclusive (ISO dates) for periodic goals (sec.7.5).
     * Week start comes from user settings (default Saturday for fa, Monday for en — sec.13).
     * MONTHLY uses calendar months; deadline (if any) caps the end date.
     */
    fun periodWindow(
        today: LocalDate,
        period: GoalPeriod,
        weekStart: DayOfWeek,
        deadline: LocalDate? = null,
    ): Pair<LocalDate, LocalDate> {
        val start = when (period) {
            GoalPeriod.NONE -> LocalDate.of(1970, 1, 1)
            GoalPeriod.WEEKLY -> {
                val diff = (today.dayOfWeek.value - weekStart.value + 7) % 7
                today.minusDays(diff.toLong())
            }
            GoalPeriod.MONTHLY -> today.withDayOfMonth(1)
        }
        val naturalEnd = when (period) {
            GoalPeriod.NONE -> today
            GoalPeriod.WEEKLY -> start.plusDays(6)
            GoalPeriod.MONTHLY -> today.withDayOfMonth(today.lengthOfMonth())
        }
        // A deadline that already passed clamps the window; never invert it (end < start), which
        // would make every BETWEEN query return nothing instead of an empty-but-valid range.
        val end = minOf(naturalEnd, deadline ?: naturalEnd).coerceAtLeast(start)
        return start to end
    }
}
