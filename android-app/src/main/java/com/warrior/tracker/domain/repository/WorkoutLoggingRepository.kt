package com.warrior.tracker.domain.repository

import com.warrior.tracker.core.common.WarriorResult
import com.warrior.tracker.domain.model.StrengthExercise
import com.warrior.tracker.domain.model.WorkoutDraft
import com.warrior.tracker.domain.model.WorkoutSummary
import kotlinx.coroutines.flow.Flow

/**
 * Logging boundary used by the existing Workouts presentation flow.
 *
 * The first supported catalog is bodyweight Strength; this API remains load-type agnostic.
 * The implementation owns Room transactions and entity mapping; ViewModels never coordinate DAOs.
 */
interface WorkoutLoggingRepository {
    fun observeDraft(): Flow<WorkoutDraft?>
    fun observeExercises(): Flow<List<StrengthExercise>>
    fun observeRecentWorkouts(limit: Int = 20): Flow<List<WorkoutSummary>>

    suspend fun initialize(): WarriorResult<Unit>
    suspend fun startOrResumeWorkout(): WarriorResult<String>
    suspend fun addExercise(exerciseId: String): WarriorResult<Unit>
    suspend fun addCompletedSet(activityId: String, value: Int): WarriorResult<Unit>
    suspend fun removeSet(setId: String): WarriorResult<Unit>
    suspend fun removeActivity(activityId: String): WarriorResult<Unit>
    suspend fun finishWorkout(): WarriorResult<Unit>
}
