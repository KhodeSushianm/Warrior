package com.warrior.tracker.core.common

/**
 * Final enums of the domain model (ARCHITECTURE.md sec.6.7).
 * Persisted as TEXT via Room TypeConverters; never free-form strings in code.
 */

enum class WorkoutStatus { IN_PROGRESS, COMPLETED }

enum class ActivityType { STRENGTH, BOXING }

enum class BoxingType { SHADOW_BOXING, HEAVY_BAG, PADS, SPARRING, JUMP_ROPE, CUSTOM }

enum class SetType { NORMAL, WARMUP, DROP, FAILURE }

enum class LoadType { WEIGHTED, BODYWEIGHT, BODYWEIGHT_PLUS, ASSISTED }

enum class MeasureType { REPS, DURATION }

enum class PRType { MAX_WEIGHT, REPS_AT_WEIGHT, BEST_E1RM, BEST_SET_VOLUME, MAX_DURATION }

enum class ExerciseDifficulty { BEGINNER, INTERMEDIATE, ADVANCED }

enum class Equipment {
    NONE, PULL_UP_BAR, PARALLEL_BARS, BENCH, BARBELL, DUMBBELL,
    CABLE, MACHINE, KETTLEBELL, RESISTANCE_BAND, OTHER
}

enum class Muscle {
    CHEST, BACK, SHOULDERS, BICEPS, TRICEPS, FOREARMS,
    ABS, OBLIQUES, LOWER_BACK, GLUTES, QUADRICEPS, HAMSTRINGS,
    CALVES, HIPS, NECK, FULL_BODY
}

enum class MuscleRole { PRIMARY, SECONDARY }

enum class GoalType {
    STRENGTH_WEIGHT, STRENGTH_REPS, SESSIONS, ROUNDS, TRAINING_TIME, BODY_WEIGHT
}

enum class GoalPeriod { NONE, WEEKLY, MONTHLY }

enum class GoalStatus { ACTIVE, ACHIEVED, ARCHIVED }

/** UI theme preference stored in DataStore (sec.13, sec.14.2 dark-first default = SYSTEM). */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Derived display unit for a Goal — computed from GoalType (sec.6.7). */
enum class GoalUnit { KG, REPS, ROUNDS, SESSIONS, MINUTES }

fun GoalType.derivedUnit(): GoalUnit = when (this) {
    GoalType.STRENGTH_WEIGHT -> GoalUnit.KG
    GoalType.STRENGTH_REPS -> GoalUnit.REPS
    GoalType.SESSIONS -> GoalUnit.SESSIONS
    GoalType.ROUNDS -> GoalUnit.ROUNDS
    GoalType.TRAINING_TIME -> GoalUnit.MINUTES
    GoalType.BODY_WEIGHT -> GoalUnit.KG
}
