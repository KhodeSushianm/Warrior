package com.warrior.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.warrior.tracker.data.local.entity.ExerciseEntity
import com.warrior.tracker.data.local.entity.ExerciseMuscleEntity
import kotlinx.coroutines.flow.Flow

/** Main methods per ARCHITECTURE.md sec.10.16; muscles are written together with the exercise (sec.10.9). */
@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercise(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun upsertExercises(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun upsertMuscles(muscles: List<ExerciseMuscleEntity>)

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Query("SELECT * FROM exercises ORDER BY search_text COLLATE NOCASE ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: String): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE `key` = :key LIMIT 1")
    suspend fun getExerciseByKey(key: String): ExerciseEntity?

    @Query(
        """
        SELECT * FROM exercises
        WHERE search_text LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY search_text COLLATE NOCASE ASC
        """
    )
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>

    @Query(
        """
        SELECT e.* FROM exercises e
        JOIN exercise_muscles m ON m.exercise_id = e.id
        WHERE m.muscle = :muscle AND m.role = 'PRIMARY' AND e.is_archived = 0
        ORDER BY e.search_text COLLATE NOCASE ASC
        """
    )
    fun getExercisesByMuscle(muscle: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE equipment = :equipment AND is_archived = 0")
    fun getExercisesByEquipment(equipment: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE load_type = 'BODYWEIGHT' AND is_archived = 0")
    fun getBodyweightExercises(): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCustomExercise(exercise: ExerciseEntity)

    @Query("UPDATE exercises SET is_archived = 1, updated_at = :now WHERE id = :id")
    suspend fun archiveExercise(id: String, now: Long)

    @Query("UPDATE exercises SET is_archived = 0, updated_at = :now WHERE id = :id")
    suspend fun restoreExercise(id: String, now: Long)

    @Transaction
    suspend fun upsertFromSeed(exercise: ExerciseEntity, muscles: List<ExerciseMuscleEntity>) {
        upsertExercise(exercise)
        upsertMuscles(muscles)
    }

    /** True if any completed workout references the exercise — gate for archiving (sec.11.3). */
    @Query("SELECT EXISTS(SELECT 1 FROM activities WHERE exercise_id = :exerciseId)")
    suspend fun isInUse(exerciseId: String): Boolean

    @Query("SELECT COUNT(*) FROM exercises")
    fun observeExerciseCount(): Flow<Int>
}
