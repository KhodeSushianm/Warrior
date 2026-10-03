package com.warrior.tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** `body_weights` — sec.10.11. Always kg; conversion happens at display time only. */
@Entity(
    tableName = "body_weights",
    indices = [Index(value = ["local_date"])],
)
data class BodyWeightEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "measured_at") val measuredAt: Long,
    @ColumnInfo(name = "local_date") val localDate: String,
    @ColumnInfo(name = "weight_kg") val weightKg: Double, // valid range 20..350 (sec.15)
    val note: String? = null,
)
