package com.warrior.tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * `rounds` — sec.10.7. Rounds of a BOXING Activity.
 * planned_* vs actual durations/rests are BOTH kept (sec.8.2 decision 6).
 */
@Entity(
    tableName = "rounds",
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activity_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["activity_id", "round_number"], unique = true),
    ],
)
data class RoundEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "activity_id") val activityId: String,
    @ColumnInfo(name = "round_number") val roundNumber: Int,
    @ColumnInfo(name = "planned_duration_sec") val plannedDurationSec: Int? = 180,
    @ColumnInfo(name = "duration_sec") val durationSec: Int? = null,
    @ColumnInfo(name = "planned_rest_sec") val plannedRestSec: Int? = 60,
    @ColumnInfo(name = "rest_sec") val restSec: Int? = null,
    val intensity: Int? = null, // 1..10
    val note: String? = null,
)
