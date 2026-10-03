package com.warrior.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.warrior.tracker.data.local.entity.SetEntity
import kotlinx.coroutines.flow.Flow

/**
 * Main methods per ARCHITECTURE.md sec.10.16.
 *
 * sec.7 preamble: **only Completed, non-WARMUP sets of COMPLETED workouts are counted.** Every
 * query feeding a metric therefore says so explicitly in its name, and the unfiltered variants
 * exist only for detail screens that must show what the user actually typed.
 */
@Dao
interface SetDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSet(set: SetEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSets(sets: List<SetEntity>)

    @Update
    suspend fun updateSet(set: SetEntity)

    @Delete
    suspend fun deleteSet(set: SetEntity)

    @Query("SELECT * FROM sets WHERE activity_id = :activityId ORDER BY set_number ASC")
    fun getSetsForActivity(activityId: String): Flow<List<SetEntity>>

    @Query("SELECT * FROM sets WHERE activity_id = :activityId ORDER BY set_number ASC")
    suspend fun getSetsForActivityOnce(activityId: String): List<SetEntity>

    /**
     * Metric source for one exercise: COMPLETED workouts only, completed sets only, WARMUP
     * excluded (sec.7 preamble, sec.7.3). Draft/in-progress workouts never leak into PRs,
     * tonnage or history.
     */
    @Query(
        """
        SELECT s.* FROM sets s
        JOIN activities a ON a.id = s.activity_id
        JOIN workouts w ON w.id = a.workout_id
        WHERE a.exercise_id = :exerciseId
          AND w.status = 'COMPLETED'
          AND s.is_completed = 1
          AND s.set_type != 'WARMUP'
        ORDER BY w.started_at DESC, a.order_index ASC, s.set_number ASC
        """
    )
    suspend fun getCountedSetsForExercise(exerciseId: String): List<SetEntity>

    /** Same filter, as a live stream for Progress screens (sec.7.6). */
    @Query(
        """
        SELECT s.* FROM sets s
        JOIN activities a ON a.id = s.activity_id
        JOIN workouts w ON w.id = a.workout_id
        WHERE a.exercise_id = :exerciseId
          AND w.status = 'COMPLETED'
          AND s.is_completed = 1
          AND s.set_type != 'WARMUP'
        ORDER BY w.started_at DESC, a.order_index ASC, s.set_number ASC
        """
    )
    fun observeCountedSetsForExercise(exerciseId: String): Flow<List<SetEntity>>

    /**
     * Every set of an exercise inside COMPLETED workouts, including WARMUP and incomplete rows —
     * for the History/Detail view (sec.3), which must show what was actually recorded.
     */
    @Query(
        """
        SELECT s.* FROM sets s
        JOIN activities a ON a.id = s.activity_id
        JOIN workouts w ON w.id = a.workout_id
        WHERE a.exercise_id = :exerciseId AND w.status = 'COMPLETED'
        ORDER BY w.started_at DESC, a.order_index ASC, s.set_number ASC
        """
    )
    suspend fun getAllSetsForExercise(exerciseId: String): List<SetEntity>

    /**
     * Pre-fill (sec.14): the completed sets of the single most recent COMPLETED session of this
     * exercise, excluding the activity being edited. Scoped with a correlated subquery so the
     * result is one session, not the whole history.
     */
    @Query(
        """
        SELECT s.* FROM sets s
        JOIN activities a ON a.id = s.activity_id
        JOIN workouts w ON w.id = a.workout_id
        WHERE a.exercise_id = :exerciseId
          AND w.status = 'COMPLETED'
          AND s.is_completed = 1
          AND a.id != :excludeActivityId
          AND w.id = (
              SELECT w2.id FROM activities a2
              JOIN workouts w2 ON w2.id = a2.workout_id
              WHERE a2.exercise_id = :exerciseId
                AND w2.status = 'COMPLETED'
                AND a2.id != :excludeActivityId
              ORDER BY w2.started_at DESC LIMIT 1
          )
        ORDER BY s.set_number ASC
        """
    )
    suspend fun getLastSessionSets(exerciseId: String, excludeActivityId: String): List<SetEntity>

    /** Completed, non-WARMUP sets for stats rebuild of one exercise/day pair (sec.7.6 cache). */
    @Query(
        """
        SELECT s.* FROM sets s
        JOIN activities a ON a.id = s.activity_id
        JOIN workouts w ON w.id = a.workout_id
        WHERE a.exercise_id = :exerciseId AND w.local_date = :localDate
          AND w.status = 'COMPLETED' AND s.is_completed = 1 AND s.set_type != 'WARMUP'
        """
    )
    suspend fun getCompletedSetsForStats(exerciseId: String, localDate: String): List<SetEntity>

    /**
     * Every exercise touched by a workout — the invalidation scope after edit/delete (sec.11.4).
     *
     * Read from `activities`, NOT joined through `sets`: if the last set of an exercise is
     * deleted, that exercise still has a stale `exercise_daily_stats` row that must be cleared.
     * The previous sets-join silently dropped exactly those exercises and leaked cache rows.
     */
    @Query(
        """
        SELECT DISTINCT a.exercise_id FROM activities a
        WHERE a.workout_id = :workoutId AND a.exercise_id IS NOT NULL
        """
    )
    suspend fun getExerciseIdsTouchedByWorkout(workoutId: String): List<String>

    @Query("SELECT COUNT(*) FROM sets WHERE is_completed = 1")
    fun observeTotalCompletedSets(): Flow<Int>
}
