package com.warrior.tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.warrior.tracker.core.common.SetType

/**
 * `sets` — sec.10.6. The core table for Strength tracking.
 * load_type/measure_type are NOT stored here; they are read from the Exercise
 * of the owning Activity (single source of truth).
 */
@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activity_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["activity_id", "set_number"], unique = true),
        Index(value = ["completed_at"]),
    ],
)
data class SetEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "activity_id") val activityId: String,
    @ColumnInfo(name = "set_number") val setNumber: Int,
    @ColumnInfo(name = "set_type") val setType: SetType = SetType.NORMAL,
    val reps: Int? = null,                       // only when measure_type == REPS
    @ColumnInfo(name = "duration_sec") val durationSec: Int? = null, // only when DURATION
    @ColumnInfo(name = "external_load_kg") val externalLoadKg: Double = 0.0,
    val rpe: Double? = null,                     // 1..10 step 0.5
    @ColumnInfo(name = "rest_planned_sec") val restPlannedSec: Int? = null,
    @ColumnInfo(name = "rest_actual_sec") val restActualSec: Int? = null,
    @ColumnInfo(name = "is_completed", defaultValue = "0") val isCompleted: Boolean = false,
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null,
)
