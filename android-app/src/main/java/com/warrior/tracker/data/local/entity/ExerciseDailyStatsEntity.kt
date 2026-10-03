package com.warrior.tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * `exercise_daily_stats` (Cache) — sec.10.12. Derived from `sets`, rebuildable anytime;
 * NOT a source of truth (sec.10.1 decision 3). Composite PK (exercise_id, local_date).
 *
 * No explicit `@Index` is declared: SQLite already creates a unique index backing the composite
 * PRIMARY KEY, and a second index on the same column pair would double the write cost of the
 * hottest cache table in the app while never being chosen by the query planner.
 */
@Entity(
    tableName = "exercise_daily_stats",
    primaryKeys = ["exercise_id", "local_date"],
)
data class ExerciseDailyStatsEntity(
    @ColumnInfo(name = "exercise_id") val exerciseId: String,
    @ColumnInfo(name = "local_date") val localDate: String,
    @ColumnInfo(name = "max_load") val maxLoad: Double? = null,
    @ColumnInfo(name = "max_reps") val maxReps: Int? = null,
    @ColumnInfo(name = "best_e1rm") val bestE1Rm: Double? = null,
    val tonnage: Double? = null,        // SUM(external_load × reps)
    @ColumnInfo(name = "total_reps") val totalReps: Int? = null,
    @ColumnInfo(name = "total_duration") val totalDuration: Int? = null,
    @ColumnInfo(name = "set_count") val setCount: Int? = null, // completed, non-WARMUP
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
