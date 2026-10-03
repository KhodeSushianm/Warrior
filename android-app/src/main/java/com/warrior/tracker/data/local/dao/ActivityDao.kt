package com.warrior.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.warrior.tracker.data.local.entity.ActivityEntity

/** Main methods per ARCHITECTURE.md sec.10.16. */
@Dao
interface ActivityDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertActivity(activity: ActivityEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertActivities(activities: List<ActivityEntity>)

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Delete
    suspend fun deleteActivity(activity: ActivityEntity)

    @Query("SELECT * FROM activities WHERE workout_id = :workoutId ORDER BY order_index ASC")
    suspend fun getActivitiesForWorkout(workoutId: String): List<ActivityEntity>

    @Query("SELECT * FROM activities WHERE workout_id = :workoutId ORDER BY order_index ASC")
    fun observeActivitiesForWorkout(workoutId: String): kotlinx.coroutines.flow.Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityById(id: String): ActivityEntity?

    /** Reorder inside one transaction; unique index (workout_id, order_index) forces the swap pattern. */
    @Transaction
    suspend fun reorderActivities(orderedIds: List<String>) {
        orderedIds.forEachIndexed { index, id ->
            setOrderIndexTemp(id, -(index + 1)) // negative temp values avoid unique collisions
        }
        orderedIds.forEachIndexed { index, id ->
            setOrderIndexFinal(id, index)
        }
    }

    @Query("UPDATE activities SET order_index = :temp WHERE id = :id")
    suspend fun setOrderIndexTemp(id: String, temp: Int)

    @Query("UPDATE activities SET order_index = :finalIndex WHERE id = :id")
    suspend fun setOrderIndexFinal(id: String, finalIndex: Int)

    /** Pre-fill source (sec.14): the most recent activity using this exercise. */
    @Query(
        """
        SELECT a.* FROM activities a
        JOIN workouts w ON w.id = a.workout_id
        WHERE a.exercise_id = :exerciseId AND w.status = 'COMPLETED'
        ORDER BY w.started_at DESC LIMIT 1
        """
    )
    suspend fun getLastActivityForExercise(exerciseId: String): ActivityEntity?

    /** All activities of an exercise across completed workouts (for stats rebuild scoping). */
    @Query("SELECT * FROM activities WHERE exercise_id = :exerciseId")
    suspend fun getAllActivitiesForExercise(exerciseId: String): List<ActivityEntity>
}
