package com.warrior.tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * `workouts` table — ARCHITECTURE.md sec.10.4.
 * UUID PK for future merge/sync; times are UTC epoch millis; local_date is ISO yyyy-MM-dd.
 */
@Entity(
    tableName = "workouts",
    indices = [
        Index(value = ["local_date", "status"]),
        Index(value = ["status"]),
    ],
)
data class WorkoutEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "ended_at") val endedAt: Long? = null,
    @ColumnInfo(name = "local_date") val localDate: String,
    @ColumnInfo(name = "timezone_id") val timezoneId: String,
    val status: String, // WorkoutStatus.name — converter maps enum <-> TEXT
    @ColumnInfo(name = "active_duration_sec") val activeDurationSec: Long? = null,
    @ColumnInfo(name = "session_rpe") val sessionRpe: Double? = null,
    @ColumnInfo(name = "body_weight_kg_snapshot") val bodyWeightKgSnapshot: Double? = null,
    val notes: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
