package com.warrior.tracker.domain.repository

import com.warrior.tracker.core.common.WarriorResult
import com.warrior.tracker.domain.model.BodyweightExercise
import com.warrior.tracker.domain.model.BodyweightWorkoutDraft
import com.warrior.tracker.domain.model.BodyweightWorkoutSummary
import kotlinx.coroutines.flow.Flow

/**
 * Bodyweight logging boundary used by the presentation layer.
 *
 * The implementation owns Room transactions and entity mapping; ViewModels never coordinate DAOs.
 */
interface BodyweightWorkoutRepository {
    fun observeDraft(): Flow<BodyweightWorkoutDraft?>
    fun observeExercises(): Flow<List<BodyweightExercise>>
    fun observeRecentWorkouts(limit: Int = 20): Flow<List<BodyweightWorkoutSummary>>

    suspend fun initialize(): WarriorResult<Unit>
    suspend fun startOrResumeWorkout(): WarriorResult<String>
    suspend fun addExercise(exerciseId: String): WarriorResult<Unit>
    suspend fun addCompletedSet(activityId: String, value: Int): WarriorResult<Unit>
    suspend fun removeSet(setId: String): WarriorResult<Unit>
    suspend fun removeActivity(activityId: String): WarriorResult<Unit>
    suspend fun finishWorkout(): WarriorResult<Unit>
}
