package com.warrior.tracker.data.repository

import androidx.room.withTransaction
import com.warrior.tracker.core.common.ActivityType
import com.warrior.tracker.core.common.LoadType
import com.warrior.tracker.core.common.MeasureType
import com.warrior.tracker.core.common.SetType
import com.warrior.tracker.core.common.WarriorError
import com.warrior.tracker.core.common.WarriorResult
import com.warrior.tracker.core.common.WorkoutStatus
import com.warrior.tracker.core.time.Clock
import com.warrior.tracker.core.validation.InputValidator
import com.warrior.tracker.data.local.dao.ActivityDao
import com.warrior.tracker.data.local.dao.BodyWeightDao
import com.warrior.tracker.data.local.dao.ExerciseDao
import com.warrior.tracker.data.local.dao.SetDao
import com.warrior.tracker.data.local.dao.StatsDao
import com.warrior.tracker.data.local.dao.WorkoutDao
import com.warrior.tracker.data.local.database.AppDatabase
import com.warrior.tracker.data.local.entity.ActivityEntity
import com.warrior.tracker.data.local.entity.ExerciseDailyStatsEntity
import com.warrior.tracker.data.local.entity.SetEntity
import com.warrior.tracker.data.local.entity.WorkoutEntity
import com.warrior.tracker.data.local.model.WorkoutWithDetails
import com.warrior.tracker.data.seed.BodyweightExerciseSeeder
import com.warrior.tracker.domain.model.StrengthActivity
import com.warrior.tracker.domain.model.StrengthExercise
import com.warrior.tracker.domain.model.LoggedSet
import com.warrior.tracker.domain.model.WorkoutDraft
import com.warrior.tracker.domain.model.WorkoutSummary
import com.warrior.tracker.domain.repository.WorkoutLoggingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Room-backed implementation of logging for the existing Workouts flow. */
@Singleton
class DefaultWorkoutLoggingRepository @Inject constructor(
    private val database: AppDatabase,
    private val workoutDao: WorkoutDao,
    private val activityDao: ActivityDao,
    private val setDao: SetDao,
    private val exerciseDao: ExerciseDao,
    private val bodyWeightDao: BodyWeightDao,
    private val statsDao: StatsDao,
    private val seeder: BodyweightExerciseSeeder,
    private val clock: Clock,
) : WorkoutLoggingRepository {

    override fun observeDraft(): Flow<WorkoutDraft?> =
        workoutDao.observeInProgressWorkoutDetails().map { details -> details?.toDomain() }

    override fun observeExercises(): Flow<List<StrengthExercise>> =
        exerciseDao.getBodyweightExercises().map { exercises ->
            exercises.mapNotNull { exercise ->
                val nameEn = exercise.nameEn ?: exercise.customName ?: return@mapNotNull null
                val nameFa = exercise.nameFa ?: exercise.customName ?: nameEn
                StrengthExercise(
                    id = exercise.id,
                    nameEn = nameEn,
                    nameFa = nameFa,
                    loadType = exercise.loadType,
                    measureType = exercise.measureType,
                    equipment = exercise.equipment,
                    difficulty = exercise.difficulty,
                )
            }
        }

    override fun observeRecentWorkouts(limit: Int): Flow<List<WorkoutSummary>> =
        workoutDao.observeRecentStrengthWorkouts(limit.coerceIn(1, 100)).map { rows ->
            rows.map { row ->
                WorkoutSummary(
                    id = row.id,
                    startedAt = row.startedAt,
                    localDate = row.localDate,
                    activeDurationSec = row.activeDurationSec ?: 0L,
                    exerciseCount = row.exerciseCount,
                    setCount = row.setCount,
                    totalReps = row.totalReps,
                    totalDurationSec = row.totalDurationSec,
                )
            }
        }

    override suspend fun initialize(): WarriorResult<Unit> = guarded {
        seeder.seedIfNeeded()
    }

    override suspend fun startOrResumeWorkout(): WarriorResult<String> = guarded {
        seeder.seedIfNeeded()
        database.withTransaction {
            workoutDao.getInProgressWorkoutOnce()?.id ?: run {
                val now = clock.nowMillis()
                val workout = WorkoutEntity(
                    id = UUID.randomUUID().toString(),
                    startedAt = now,
                    localDate = clock.localDateToday(clock.zoneId()).toString(),
                    timezoneId = clock.zoneId().id,
                    status = WorkoutStatus.IN_PROGRESS,
                    createdAt = now,
                    updatedAt = now,
                )
                workoutDao.insertWorkout(workout)
                workout.id
            }
        }
    }

    override suspend fun addExercise(exerciseId: String): WarriorResult<Unit> = guarded {
        seeder.seedIfNeeded()
        database.withTransaction {
            val workout = activeWorkout()
            val exercise = exerciseDao.getExerciseById(exerciseId)
                ?: fail(WarriorError.NotFound("exercise", exerciseId))
            if (exercise.isArchived || exercise.loadType != LoadType.BODYWEIGHT) {
                fail(WarriorError.Validation("exercise", WarriorError.Reason.INCONSISTENT_STATE))
            }
            val activities = activityDao.getActivitiesForWorkout(workout.id)
            activityDao.insertActivity(
                ActivityEntity(
                    id = UUID.randomUUID().toString(),
                    workoutId = workout.id,
                    orderIndex = (activities.maxOfOrNull { it.orderIndex } ?: -1) + 1,
                    type = ActivityType.STRENGTH,
                    exerciseId = exercise.id,
                )
            )
        }
    }

    override suspend fun addCompletedSet(activityId: String, value: Int): WarriorResult<Unit> = guarded {
        database.withTransaction {
            val activity = activityDao.getActivityById(activityId)
                ?: fail(WarriorError.NotFound("activity", activityId))
            requireActiveWorkout(activity.workoutId)
            if (activity.type != ActivityType.STRENGTH) {
                fail(WarriorError.Validation("activity", WarriorError.Reason.INCONSISTENT_STATE))
            }
            val exerciseId = activity.exerciseId
                ?: fail(WarriorError.Validation("exercise", WarriorError.Reason.REQUIRED))
            val exercise = exerciseDao.getExerciseById(exerciseId)
                ?: fail(WarriorError.NotFound("exercise", exerciseId))
            if (exercise.loadType != LoadType.BODYWEIGHT) {
                fail(WarriorError.Validation("exercise", WarriorError.Reason.INCONSISTENT_STATE))
            }

            when (exercise.measureType) {
                MeasureType.REPS -> requireValid(InputValidator.validateReps(value))
                MeasureType.DURATION -> requireValid(InputValidator.validateHoldDurationSec(value))
            }

            val existing = setDao.getSetsForActivityOnce(activity.id)
            val now = clock.nowMillis()
            setDao.insertSet(
                SetEntity(
                    id = UUID.randomUUID().toString(),
                    activityId = activity.id,
                    setNumber = (existing.maxOfOrNull { it.setNumber } ?: 0) + 1,
                    setType = SetType.NORMAL,
                    reps = value.takeIf { exercise.measureType == MeasureType.REPS },
                    durationSec = value.takeIf { exercise.measureType == MeasureType.DURATION },
                    externalLoadKg = 0.0,
                    isCompleted = true,
                    completedAt = now,
                )
            )
        }
    }

    override suspend fun removeSet(setId: String): WarriorResult<Unit> = guarded {
        database.withTransaction {
            val set = setDao.getSetById(setId) ?: fail(WarriorError.NotFound("set", setId))
            val activity = activityDao.getActivityById(set.activityId)
                ?: fail(WarriorError.NotFound("activity", set.activityId))
            requireActiveWorkout(activity.workoutId)
            setDao.deleteSet(set)

            // Preserve the documented contiguous numbering invariant after a middle-row deletion.
            setDao.getSetsForActivityOnce(activity.id)
                .sortedBy { it.setNumber }
                .forEachIndexed { index, remaining ->
                    val expected = index + 1
                    if (remaining.setNumber != expected) setDao.updateSet(remaining.copy(setNumber = expected))
                }
        }
    }

    override suspend fun removeActivity(activityId: String): WarriorResult<Unit> = guarded {
        database.withTransaction {
            val activity = activityDao.getActivityById(activityId)
                ?: fail(WarriorError.NotFound("activity", activityId))
            requireActiveWorkout(activity.workoutId)
            activityDao.deleteActivity(activity)
            val remainingIds = activityDao.getActivitiesForWorkout(activity.workoutId)
                .sortedBy { it.orderIndex }
                .map { it.id }
            activityDao.reorderActivities(remainingIds)
        }
    }

    override suspend fun finishWorkout(): WarriorResult<Unit> = guarded {
        database.withTransaction {
            val workout = activeWorkout()
            val activities = activityDao.getActivitiesForWorkout(workout.id)
            val completedSetCount = activities.sumOf { activity ->
                setDao.getSetsForActivityOnce(activity.id).count { it.isCompleted }
            }
            if (completedSetCount == 0) {
                fail(WarriorError.Validation("workout", WarriorError.Reason.INCONSISTENT_STATE))
            }

            val now = clock.nowMillis()
            val bodyWeightSnapshot = bodyWeightDao.getWeightAt(workout.localDate)?.weightKg
            workoutDao.updateWorkout(
                workout.copy(
                    endedAt = now,
                    status = WorkoutStatus.COMPLETED,
                    activeDurationSec = ((now - workout.startedAt).coerceAtLeast(0L) / 1_000L),
                    bodyWeightKgSnapshot = bodyWeightSnapshot,
                    updatedAt = now,
                )
            )

            // The raw sets remain the source of truth; refresh only affected daily cache rows.
            activities.mapNotNull { it.exerciseId }.distinct().forEach { exerciseId ->
                refreshDailyStats(exerciseId, workout.localDate, now)
            }
        }
    }

    private suspend fun refreshDailyStats(exerciseId: String, localDate: String, now: Long) {
        val sets = setDao.getCompletedSetsForStats(exerciseId, localDate)
        if (sets.isEmpty()) {
            statsDao.deleteStats(exerciseId, localDate)
            return
        }
        val reps = sets.mapNotNull { it.reps }
        val durations = sets.mapNotNull { it.durationSec }
        statsDao.upsertDailyStats(
            ExerciseDailyStatsEntity(
                exerciseId = exerciseId,
                localDate = localDate,
                maxLoad = null,
                maxReps = reps.maxOrNull(),
                bestE1Rm = null,
                tonnage = 0.0,
                totalReps = reps.sum().takeIf { reps.isNotEmpty() },
                totalDuration = durations.sum().takeIf { durations.isNotEmpty() },
                setCount = sets.size,
                updatedAt = now,
            )
        )
    }

    private suspend fun activeWorkout(): WorkoutEntity =
        workoutDao.getInProgressWorkoutOnce()
            ?: fail(WarriorError.NotFound("workout", "active"))

    private suspend fun requireActiveWorkout(workoutId: String): WorkoutEntity {
        val workout = workoutDao.getWorkoutById(workoutId)
            ?: fail(WarriorError.NotFound("workout", workoutId))
        if (workout.status != WorkoutStatus.IN_PROGRESS) {
            fail(WarriorError.Validation("workout", WarriorError.Reason.INCONSISTENT_STATE))
        }
        return workout
    }

    private fun requireValid(result: WarriorResult<Unit>) {
        if (result is WarriorResult.Failure) fail(result.error)
    }

    private suspend fun <T> guarded(block: suspend () -> T): WarriorResult<T> = try {
        WarriorResult.Success(block())
    } catch (failure: RepositoryFailure) {
        WarriorResult.Failure(failure.error)
    } catch (throwable: Throwable) {
        WarriorResult.Failure(WarriorError.Persistence(throwable))
    }

    private fun fail(error: WarriorError): Nothing = throw RepositoryFailure(error)

    private class RepositoryFailure(val error: WarriorError) : RuntimeException()

    private fun WorkoutWithDetails.toDomain(): WorkoutDraft =
        WorkoutDraft(
            id = workout.id,
            startedAt = workout.startedAt,
            localDate = workout.localDate,
            activities = activities
                .sortedBy { it.activity.orderIndex }
                .mapNotNull { item ->
                    val exercise = item.exercise ?: return@mapNotNull null
                    if (item.activity.type != ActivityType.STRENGTH || exercise.loadType != LoadType.BODYWEIGHT) {
                        return@mapNotNull null
                    }
                    StrengthActivity(
                        id = item.activity.id,
                        exerciseId = exercise.id,
                        nameEn = exercise.nameEn ?: exercise.customName ?: exercise.id,
                        nameFa = exercise.nameFa ?: exercise.customName ?: exercise.nameEn ?: exercise.id,
                        loadType = exercise.loadType,
                        measureType = exercise.measureType,
                        sets = item.sets.sortedBy { it.setNumber }.map { set ->
                            LoggedSet(
                                id = set.id,
                                setNumber = set.setNumber,
                                reps = set.reps,
                                durationSec = set.durationSec,
                                completedAt = set.completedAt,
                            )
                        },
                    )
                },
        )
}
