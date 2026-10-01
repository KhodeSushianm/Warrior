package com.warrior.tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * `exercises` — sec.10.8. Built-ins use stable id `builtin:<key>`; custom ones a UUID.
 * Soft delete via is_archived (sec.11.3): an Exercise referenced by history is never hard-deleted.
 */
@Entity(
    tableName = "exercises",
    indices = [
        Index(value = ["key"], unique = true),
        Index(value = ["search_text"]),
        Index(value = ["is_archived"]),
        Index(value = ["is_builtin"]),
        Index(value = ["load_type"]),
    ],
)
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val key: String? = null,
    @ColumnInfo(name = "is_builtin") val isBuiltin: Boolean,
    @ColumnInfo(name = "name_en") val nameEn: String? = null,
    @ColumnInfo(name = "name_fa") val nameFa: String? = null,
    @ColumnInfo(name = "custom_name") val customName: String? = null,
    @ColumnInfo(name = "search_text") val searchText: String,
    @ColumnInfo(name = "load_type") val loadType: String,     // LoadType.name
    @ColumnInfo(name = "measure_type") val measureType: String, // MeasureType.name
    val equipment: String = "NONE",                            // Equipment.name
    val difficulty: String,                                    // ExerciseDifficulty.name
    @ColumnInfo(name = "instructions_en") val instructionsEn: String? = null,
    @ColumnInfo(name = "instructions_fa") val instructionsFa: String? = null,
    @ColumnInfo(name = "is_archived", defaultValue = "0") val isArchived: Boolean = false,
    @ColumnInfo(name = "seed_version") val seedVersion: Int? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
