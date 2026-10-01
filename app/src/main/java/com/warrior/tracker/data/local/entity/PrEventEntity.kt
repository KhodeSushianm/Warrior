package com.warrior.tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * `pr_events` (Cache) — sec.10.13. One row per personal-record event;
 * is_current marks the reigning record per (exercise_id, pr_type).
 */
@Entity(
    tableName = "pr_events",
    indices = [
        Index(value = ["exercise_id", "pr_type", "is_current"]),
    ],
)
data class PrEventEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "exercise_id") val exerciseId: String,
    @ColumnInfo(name = "pr_type") val prType: String, // PRType.name
    val value: Double,
    val reps: Int? = null, // for REPS_AT_WEIGHT / BEST_E1RM
    @ColumnInfo(name = "set_id") val setId: String? = null, // may be deleted later
    @ColumnInfo(name = "workout_id") val workoutId: String? = null,
    @ColumnInfo(name = "achieved_at") val achievedAt: Long,
    @ColumnInfo(name = "is_current") val isCurrent: Boolean,
)
