package com.warrior.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.warrior.tracker.data.local.entity.SetEntity
import kotlinx.coroutines.flow.Flow

/** Main methods per ARCHITECTURE.md sec.10.16. */
@Dao
interface SetDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSet(set: SetEntity)

    @Update
    suspend fun updateSet(set: SetEntity)

    @Delete
    suspend fun deleteSet(set: SetEntity)

    @Query("SELECT * FROM sets WHERE activity_id = :activityId ORDER BY set_number ASC")
    fun getSetsForActivity(activityId: String): Flow<List<SetEntity>>

    @Query("SELECT * FROM sets WHERE activity_id = :activityId ORDER BY set_number ASC")
    suspend fun getSetsForActivityOnce(activityId: String): List<SetEntity>

    /** All sets of an exercise (joined through activities), newest workout first. */
    @Query(
        """
        SELECT s.* FROM sets s
        JOIN activities a ON a.id = s.activity_id
        WHERE a.exercise_id = :exerciseId
        ORDER BY s.completed_at DESC
        """
    )
    suspend fun getSetsForExercise(exerciseId: String): List<SetEntity>

    /** Pre-fill (sec.14): completed sets of the last COMPLETED session of this exercise. */
    @Query(
        """
        SELECT s.* FROM sets s
        JOIN activities a ON a.id = s.activity_id
        JOIN workouts w ON w.id = a.workout_id
        WHERE a.exercise_id = :exerciseId AND w.status = 'COMPLETED' AND s.is_completed = 1
          AND a.id != :excludeActivityId
        ORDER BY w.started_at DESC, s.set_number ASC
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

    /** Same, but every exercise on a date (workout edited/deleted → scope of invalidation, sec.11.4). */
    @Query(
        """
        SELECT DISTINCT a.exercise_id FROM sets s
        JOIN activities a ON a.id = s.activity_id
        WHERE a.workout_id = :workoutId AND a.exercise_id IS NOT NULL
        """
    )
    suspend fun getExerciseIdsTouchedByWorkout(workoutId: String): List<String>

    @Query("SELECT COUNT(*) FROM sets WHERE is_completed = 1")
    fun observeTotalCompletedSets(): Flow<Int>
}
