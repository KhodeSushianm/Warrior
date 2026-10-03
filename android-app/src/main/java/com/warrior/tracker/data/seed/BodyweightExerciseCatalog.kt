package com.warrior.tracker.data.seed

import com.warrior.tracker.core.common.Equipment
import com.warrior.tracker.core.common.ExerciseDifficulty
import com.warrior.tracker.core.common.LoadType
import com.warrior.tracker.core.common.MeasureType
import com.warrior.tracker.core.common.Muscle
import com.warrior.tracker.core.common.MuscleRole
import com.warrior.tracker.core.time.Clock
import com.warrior.tracker.data.local.dao.ExerciseDao
import com.warrior.tracker.data.local.entity.ExerciseEntity
import com.warrior.tracker.data.local.entity.ExerciseMuscleEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val BODYWEIGHT_SEED_VERSION = 1

/** Curated Phase-1 catalogue: balanced push, pull, legs and core coverage. */
internal data class BodyweightExerciseSeed(
    val key: String,
    val nameEn: String,
    val nameFa: String,
    val measureType: MeasureType,
    val equipment: Equipment = Equipment.NONE,
    val difficulty: ExerciseDifficulty = ExerciseDifficulty.BEGINNER,
    val primaryMuscle: Muscle,
    val secondaryMuscles: List<Muscle> = emptyList(),
)

internal val BODYWEIGHT_EXERCISES = listOf(
    BodyweightExerciseSeed("push_up", "Push-Up", "شنا سوئدی", MeasureType.REPS, primaryMuscle = Muscle.CHEST, secondaryMuscles = listOf(Muscle.TRICEPS, Muscle.SHOULDERS)),
    BodyweightExerciseSeed("wide_push_up", "Wide Push-Up", "شنا دست باز", MeasureType.REPS, difficulty = ExerciseDifficulty.INTERMEDIATE, primaryMuscle = Muscle.CHEST, secondaryMuscles = listOf(Muscle.SHOULDERS)),
    BodyweightExerciseSeed("diamond_push_up", "Diamond Push-Up", "شنا الماسی", MeasureType.REPS, difficulty = ExerciseDifficulty.INTERMEDIATE, primaryMuscle = Muscle.TRICEPS, secondaryMuscles = listOf(Muscle.CHEST)),
    BodyweightExerciseSeed("decline_push_up", "Decline Push-Up", "شنا پا بالا", MeasureType.REPS, equipment = Equipment.BENCH, difficulty = ExerciseDifficulty.INTERMEDIATE, primaryMuscle = Muscle.CHEST, secondaryMuscles = listOf(Muscle.SHOULDERS, Muscle.TRICEPS)),
    BodyweightExerciseSeed("pike_push_up", "Pike Push-Up", "شنا سرشانه پایک", MeasureType.REPS, difficulty = ExerciseDifficulty.INTERMEDIATE, primaryMuscle = Muscle.SHOULDERS, secondaryMuscles = listOf(Muscle.TRICEPS)),
    BodyweightExerciseSeed("dip", "Dip", "دیپ", MeasureType.REPS, equipment = Equipment.PARALLEL_BARS, difficulty = ExerciseDifficulty.INTERMEDIATE, primaryMuscle = Muscle.TRICEPS, secondaryMuscles = listOf(Muscle.CHEST, Muscle.SHOULDERS)),
    BodyweightExerciseSeed("pull_up", "Pull-Up", "بارفیکس دست باز", MeasureType.REPS, equipment = Equipment.PULL_UP_BAR, difficulty = ExerciseDifficulty.INTERMEDIATE, primaryMuscle = Muscle.BACK, secondaryMuscles = listOf(Muscle.BICEPS, Muscle.FOREARMS)),
    BodyweightExerciseSeed("chin_up", "Chin-Up", "بارفیکس دست جمع", MeasureType.REPS, equipment = Equipment.PULL_UP_BAR, difficulty = ExerciseDifficulty.INTERMEDIATE, primaryMuscle = Muscle.BICEPS, secondaryMuscles = listOf(Muscle.BACK, Muscle.FOREARMS)),
    BodyweightExerciseSeed("inverted_row", "Inverted Row", "پارویی معکوس", MeasureType.REPS, equipment = Equipment.PULL_UP_BAR, difficulty = ExerciseDifficulty.BEGINNER, primaryMuscle = Muscle.BACK, secondaryMuscles = listOf(Muscle.BICEPS)),
    BodyweightExerciseSeed("bodyweight_squat", "Bodyweight Squat", "اسکوات وزن بدن", MeasureType.REPS, primaryMuscle = Muscle.QUADRICEPS, secondaryMuscles = listOf(Muscle.GLUTES, Muscle.HAMSTRINGS)),
    BodyweightExerciseSeed("reverse_lunge", "Reverse Lunge", "لانج معکوس", MeasureType.REPS, primaryMuscle = Muscle.QUADRICEPS, secondaryMuscles = listOf(Muscle.GLUTES, Muscle.HAMSTRINGS)),
    BodyweightExerciseSeed("bulgarian_split_squat", "Bulgarian Split Squat", "اسکوات بلغاری", MeasureType.REPS, equipment = Equipment.BENCH, difficulty = ExerciseDifficulty.INTERMEDIATE, primaryMuscle = Muscle.QUADRICEPS, secondaryMuscles = listOf(Muscle.GLUTES, Muscle.HAMSTRINGS)),
    BodyweightExerciseSeed("single_leg_calf_raise", "Single-Leg Calf Raise", "ساق پا تک‌پا", MeasureType.REPS, difficulty = ExerciseDifficulty.INTERMEDIATE, primaryMuscle = Muscle.CALVES),
    BodyweightExerciseSeed("glute_bridge", "Glute Bridge", "پل باسن", MeasureType.REPS, primaryMuscle = Muscle.GLUTES, secondaryMuscles = listOf(Muscle.HAMSTRINGS, Muscle.LOWER_BACK)),
    BodyweightExerciseSeed("plank", "Plank", "پلانک", MeasureType.DURATION, primaryMuscle = Muscle.ABS, secondaryMuscles = listOf(Muscle.LOWER_BACK, Muscle.SHOULDERS)),
    BodyweightExerciseSeed("side_plank", "Side Plank", "پلانک بغل", MeasureType.DURATION, difficulty = ExerciseDifficulty.INTERMEDIATE, primaryMuscle = Muscle.OBLIQUES, secondaryMuscles = listOf(Muscle.ABS, Muscle.SHOULDERS)),
    BodyweightExerciseSeed("hollow_hold", "Hollow Body Hold", "هالو هولد", MeasureType.DURATION, difficulty = ExerciseDifficulty.INTERMEDIATE, primaryMuscle = Muscle.ABS, secondaryMuscles = listOf(Muscle.HIPS)),
    BodyweightExerciseSeed("wall_sit", "Wall Sit", "وال سیت", MeasureType.DURATION, primaryMuscle = Muscle.QUADRICEPS, secondaryMuscles = listOf(Muscle.GLUTES, Muscle.CALVES)),
    BodyweightExerciseSeed("handstand_hold", "Handstand Hold", "هندستند هولد", MeasureType.DURATION, difficulty = ExerciseDifficulty.ADVANCED, primaryMuscle = Muscle.SHOULDERS, secondaryMuscles = listOf(Muscle.TRICEPS, Muscle.ABS)),
)

/** Idempotently installs the built-in bodyweight catalogue before the logger references it. */
@Singleton
class BodyweightExerciseSeeder @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val clock: Clock,
) {
    private val mutex = Mutex()

    suspend fun seedIfNeeded() = mutex.withLock {
        if ((exerciseDao.getInstalledSeedVersion() ?: 0) >= BODYWEIGHT_SEED_VERSION) return@withLock

        val now = clock.nowMillis()
        val exercises = BODYWEIGHT_EXERCISES.map { seed ->
            ExerciseEntity(
                id = seed.id,
                key = seed.key,
                isBuiltin = true,
                nameEn = seed.nameEn,
                nameFa = seed.nameFa,
                searchText = normalizeSearchText("${seed.nameEn} ${seed.nameFa}"),
                loadType = LoadType.BODYWEIGHT,
                measureType = seed.measureType,
                equipment = seed.equipment,
                difficulty = seed.difficulty,
                seedVersion = BODYWEIGHT_SEED_VERSION,
                createdAt = now,
                updatedAt = now,
            )
        }
        val muscles = BODYWEIGHT_EXERCISES.flatMap { seed ->
            listOf(
                ExerciseMuscleEntity(seed.id, seed.primaryMuscle, MuscleRole.PRIMARY),
            ) + seed.secondaryMuscles.distinct().map { muscle ->
                ExerciseMuscleEntity(seed.id, muscle, MuscleRole.SECONDARY)
            }
        }
        exerciseDao.upsertSeedBatch(exercises, muscles)
    }

    private val BodyweightExerciseSeed.id: String get() = "builtin:$key"

    private fun normalizeSearchText(value: String): String = value
        .lowercase(Locale.ROOT)
        .replace('ي', 'ی')
        .replace('ك', 'ک')
        .replace('\u200c', ' ')
        .replace(Regex("\\s+"), " ")
        .trim()
}
