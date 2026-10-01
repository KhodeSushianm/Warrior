package com.warrior.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.warrior.tracker.core.common.Equipment
import com.warrior.tracker.core.common.Muscle
import com.warrior.tracker.data.local.entity.ExerciseEntity
import com.warrior.tracker.data.local.entity.ExerciseMuscleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Main methods per ARCHITECTURE.md sec.10.16; muscles are written together with the exercise
 * (sec.10.9). Enum columns are bound through `DatabaseConverters`, so parameters are typed.
 *
 * Picker-facing queries exclude archived rows (sec.11.3: an archived Exercise disappears from
 * selection but its history stays intact); `getAllExercises`/`getById`/`getByKey` do not, because
 * History and Backup need to resolve archived Exercises too.
 */
@Dao
interface ExerciseDao {

    /** Insert-or-replace a single exercise (used for user edits and single-item seeding). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercise(exercise: ExerciseEntity)

    /**
     * Insert-or-replace many exercises. REPLACE (not IGNORE) so a re-seed with a higher
     * `seed_version` actually updates built-ins — sec.9.3. User data is protected by the
     * built-in id scheme (`builtin:<key>`), never by an IGNORE that silently drops updates.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercises(exercises: List<ExerciseEntity>)

    /**
     * Insert-or-replace muscle links. REPLACE so re-seeding can correct a muscle mapping;
     * the composite PK is `(exercise_id, muscle)` so re-running is idempotent.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMuscles(muscles: List<ExerciseMuscleEntity>)

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Query("SELECT * FROM exercises ORDER BY search_text COLLATE NOCASE ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    /** Active (non-archived) exercises for the in-workout Picker (sec.10.15 `is_archived`). */
    @Query("SELECT * FROM exercises WHERE is_archived = 0 ORDER BY search_text COLLATE NOCASE ASC")
    fun getActiveExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: String): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE `key` = :key LIMIT 1")
    suspend fun getExerciseByKey(key: String): ExerciseEntity?

    /**
     * Substring search over the pre-computed `search_text` column.
     *
     * - The parameter must be built by [containsQuery], which escapes LIKE metacharacters to
     *   pair with `ESCAPE '\'` (so a query containing `%` or `_` matches literally instead of
     *   acting as a wildcard) and adds the surrounding `%`.
     * - `COLLATE NOCASE` is applied to the *column* (its only effective position), not to the
     *   pattern.
     * - Archived rows are excluded to stay consistent with the other Picker queries.
     */
    @Query(
        """
        SELECT * FROM exercises
        WHERE (search_text COLLATE NOCASE) LIKE :escapedQuery ESCAPE '\'
          AND is_archived = 0
        ORDER BY search_text COLLATE NOCASE ASC
        """
    )
    fun searchExercises(escapedQuery: String): Flow<List<ExerciseEntity>>

    @Query(
        """
        SELECT e.* FROM exercises e
        JOIN exercise_muscles m ON m.exercise_id = e.id
        WHERE m.muscle = :muscle AND m.role = 'PRIMARY' AND e.is_archived = 0
        ORDER BY e.search_text COLLATE NOCASE ASC
        """
    )
    fun getExercisesByMuscle(muscle: Muscle): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE equipment = :equipment AND is_archived = 0")
    fun getExercisesByEquipment(equipment: Equipment): Flow<List<ExerciseEntity>>

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

    @Transaction
    suspend fun upsertSeedBatch(
        exercises: List<ExerciseEntity>,
        muscles: List<ExerciseMuscleEntity>,
    ) {
        upsertExercises(exercises)
        upsertMuscles(muscles)
    }

    /**
     * True when any activity references this exercise — the gate for hard-deletion (sec.11.3:
     * "an Exercise referenced by history is never hard-deleted").
     *
     * Deliberately NOT filtered by workout status: a reference from an in-progress draft also
     * blocks deletion, otherwise finishing the draft would violate the RESTRICT foreign key on
     * `activities.exercise_id`. Use [isReferencedByCompletedWorkout] for the archive prompt copy.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM activities WHERE exercise_id = :exerciseId)")
    suspend fun isInUse(exerciseId: String): Boolean

    /** True when a COMPLETED workout references this exercise (drives the sec.11.3 UI wording). */
    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM activities a
            JOIN workouts w ON w.id = a.workout_id
            WHERE a.exercise_id = :exerciseId AND w.status = 'COMPLETED'
        )
        """
    )
    suspend fun isReferencedByCompletedWorkout(exerciseId: String): Boolean

    @Query("SELECT COUNT(*) FROM exercises")
    fun observeExerciseCount(): Flow<Int>

    @Query("SELECT MAX(seed_version) FROM exercises WHERE is_builtin = 1")
    suspend fun getInstalledSeedVersion(): Int?

    companion object {
        /**
         * Build the argument for [searchExercises]: a `%`-wrapped substring pattern with SQLite
         * LIKE metacharacters escaped, to be used with `ESCAPE '\'`.
         */
        fun containsQuery(raw: String): String = buildString(raw.length + 8) {
            append('%')
            for (ch in raw) {
                if (ch == '\\' || ch == '%' || ch == '_') append('\\')
                append(ch)
            }
            append('%')
        }
    }
}
