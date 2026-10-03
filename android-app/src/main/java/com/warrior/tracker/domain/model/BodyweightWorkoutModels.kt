package com.warrior.tracker.domain.model

import com.warrior.tracker.core.common.Equipment
import com.warrior.tracker.core.common.ExerciseDifficulty
import com.warrior.tracker.core.common.MeasureType

/** A selectable strength exercise whose resistance is the athlete's own body weight. */
data class BodyweightExercise(
    val id: String,
    val nameEn: String,
    val nameFa: String,
    val measureType: MeasureType,
    val equipment: Equipment,
    val difficulty: ExerciseDifficulty,
)

/** A completed set inside an in-progress bodyweight workout. */
data class BodyweightSet(
    val id: String,
    val setNumber: Int,
    val reps: Int?,
    val durationSec: Int?,
    val completedAt: Long?,
)

/** One exercise/activity and all sets currently logged for it. */
data class BodyweightActivity(
    val id: String,
    val exerciseId: String,
    val nameEn: String,
    val nameFa: String,
    val measureType: MeasureType,
    val sets: List<BodyweightSet>,
)

/** The single resumable workout draft supported by the application policy. */
data class BodyweightWorkoutDraft(
    val id: String,
    val startedAt: Long,
    val localDate: String,
    val activities: List<BodyweightActivity>,
) {
    val completedSetCount: Int get() = activities.sumOf { activity -> activity.sets.count { it.completedAt != null } }
}

/** Compact history row for a completed bodyweight workout. */
data class BodyweightWorkoutSummary(
    val id: String,
    val startedAt: Long,
    val localDate: String,
    val activeDurationSec: Long,
    val exerciseCount: Int,
    val setCount: Int,
    val totalReps: Int,
    val totalDurationSec: Int,
)
