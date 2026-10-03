package com.warrior.tracker.core.common

import java.time.DayOfWeek

/**
 * Final enums of the domain model (ARCHITECTURE.md sec.6.7).
 *
 * Persisted as TEXT columns holding `Enum.name`, converted by `DatabaseConverters`, and exposed to
 * Kotlin as the enum type — never as free-form strings (sec.10.16). Settings enums live in
 * DataStore and follow the same rule via [enumValueOrDefault].
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

/**
 * Which calendar the UI renders (sec.13). Storage is ALWAYS Gregorian ISO `local_date`;
 * this only affects presentation. AUTO resolves from the effective language (fa → JALALI).
 */
enum class CalendarSystem { AUTO, JALALI, GREGORIAN }

/** Digit glyphs used when rendering numbers (sec.13). AUTO resolves from the effective language. */
enum class DigitSystem { AUTO, LATIN, PERSIAN }

/** Effective app language (sec.13). Parsed from `Locale` tags, never stored as free text. */
enum class AppLanguage(val tag: String) {
    ENGLISH("en"),
    PERSIAN("fa"),
    ;

    companion object {
        /** Parse a BCP-47 tag such as `"fa"`, `"fa-IR"` or `"en-US"`; null when unrecognised. */
        fun fromTag(tag: String?): AppLanguage? = when (tag?.lowercase()?.substringBefore('-')) {
            "fa" -> PERSIAN
            "en" -> ENGLISH
            else -> null
        }
    }
}

/**
 * Safe enum parsing for values that come from disk (DataStore) or the database.
 * `Enum.valueOf` throws on unknown/corrupt values, which would take the whole settings `Flow`
 * (and therefore the UI) down with it — sec.15 requires a comprehensible failure, not a crash.
 */
inline fun <reified T : Enum<T>> enumValueOrNull(name: String?): T? =
    name?.let { n -> enumValues<T>().firstOrNull { it.name == n } }

/** Safe parsing with a fallback for corrupt/unknown persisted values. */
inline fun <reified T : Enum<T>> enumValueOrDefault(name: String?, default: T): T =
    enumValueOrNull<T>(name) ?: default

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

/**
 * First day of the week per language (sec.13: Saturday for Persian, Monday for English).
 * Feeds `WeeklySummaryCalculator.weekWindow` and `GoalCalculator.periodWindow`.
 */
fun AppLanguage.derivedWeekStart(): DayOfWeek = when (this) {
    AppLanguage.PERSIAN -> DayOfWeek.SATURDAY
    AppLanguage.ENGLISH -> DayOfWeek.MONDAY
}

/** True when this language is rendered right-to-left (sec.13). */
fun AppLanguage.isRtl(): Boolean = this == AppLanguage.PERSIAN

/** Display digits should be Persian for this setting/language pair (sec.13). */
fun DigitSystem.resolvesToPersian(language: AppLanguage): Boolean = when (this) {
    DigitSystem.PERSIAN -> true
    DigitSystem.LATIN -> false
    DigitSystem.AUTO -> language == AppLanguage.PERSIAN
}

/** Jalali rendering is used for this setting/language pair (sec.13). */
fun CalendarSystem.resolvesToJalali(language: AppLanguage): Boolean = when (this) {
    CalendarSystem.JALALI -> true
    CalendarSystem.GREGORIAN -> false
    CalendarSystem.AUTO -> language == AppLanguage.PERSIAN
}
