package com.warrior.tracker.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

/**
 * sec.6.7 / sec.13 — enum derivation rules, and the defensive parsing that keeps a corrupt
 * DataStore value from taking the whole settings Flow (and therefore the UI) down with it.
 */
class EnumsTest {

    @Test
    fun `unknown persisted values fall back to the documented default instead of throwing`() {
        // Phase 0 called ThemeMode.valueOf(...) inside Flow.map; one corrupt byte in DataStore then
        // errored the stream permanently, until the user cleared app data.
        assertEquals(ThemeMode.SYSTEM, enumValueOrDefault("NOPE", ThemeMode.SYSTEM))
        assertEquals(ThemeMode.DARK, enumValueOrDefault(null, ThemeMode.DARK))
        assertEquals(ThemeMode.SYSTEM, enumValueOrDefault("light", ThemeMode.SYSTEM)) // case-sensitive
        assertEquals(ThemeMode.LIGHT, enumValueOrDefault("LIGHT", ThemeMode.SYSTEM))
        assertEquals(CalendarSystem.AUTO, enumValueOrDefault("SOLAR", CalendarSystem.AUTO))
        assertEquals(DigitSystem.PERSIAN, enumValueOrDefault("", DigitSystem.PERSIAN))
    }

    @Test
    fun `enumValueOrNull returns null for unknown names`() {
        assertNull(enumValueOrNull<WorkoutStatus>(null))
        assertNull(enumValueOrNull<WorkoutStatus>("FINISHED"))
        assertEquals(WorkoutStatus.COMPLETED, enumValueOrNull<WorkoutStatus>("COMPLETED"))
    }

    @Test
    fun `language tags resolve to supported app languages`() {
        assertEquals(AppLanguage.PERSIAN, AppLanguage.fromTag("fa"))
        assertEquals(AppLanguage.PERSIAN, AppLanguage.fromTag("fa-IR"))
        assertEquals(AppLanguage.PERSIAN, AppLanguage.fromTag("FA-ir"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("en"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("en-US"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("en-GB"))
        // Unsupported languages fall back rather than silently picking a text direction.
        assertNull(AppLanguage.fromTag("de"))
        assertNull(AppLanguage.fromTag(null))
        assertNull(AppLanguage.fromTag(""))
    }

    @Test
    fun `week start follows the language by default`() {
        // sec.13: Saturday for Persian, Monday for English.
        assertEquals(DayOfWeek.SATURDAY, AppLanguage.PERSIAN.derivedWeekStart())
        assertEquals(DayOfWeek.MONDAY, AppLanguage.ENGLISH.derivedWeekStart())
    }

    @Test
    fun `auto calendar and digits resolve from the language`() {
        assertTrue(CalendarSystem.AUTO.resolvesToJalali(AppLanguage.PERSIAN))
        assertFalse(CalendarSystem.AUTO.resolvesToJalali(AppLanguage.ENGLISH))
        assertTrue(CalendarSystem.JALALI.resolvesToJalali(AppLanguage.ENGLISH))
        assertFalse(CalendarSystem.GREGORIAN.resolvesToJalali(AppLanguage.PERSIAN))

        assertTrue(DigitSystem.AUTO.resolvesToPersian(AppLanguage.PERSIAN))
        assertFalse(DigitSystem.AUTO.resolvesToPersian(AppLanguage.ENGLISH))
        assertTrue(DigitSystem.PERSIAN.resolvesToPersian(AppLanguage.ENGLISH))
        assertFalse(DigitSystem.LATIN.resolvesToPersian(AppLanguage.PERSIAN))
    }

    @Test
    fun `rtl follows the language`() {
        assertTrue(AppLanguage.PERSIAN.isRtl())
        assertFalse(AppLanguage.ENGLISH.isRtl())
    }

    @Test
    fun `every goal type derives exactly one unit`() {
        // Exhaustive by construction: a new GoalType without a mapping fails to compile.
        val units = GoalType.entries.associateWith { it.derivedUnit() }
        assertEquals(GoalUnit.KG, units[GoalType.STRENGTH_WEIGHT])
        assertEquals(GoalUnit.KG, units[GoalType.BODY_WEIGHT])
        assertEquals(GoalUnit.REPS, units[GoalType.STRENGTH_REPS])
        assertEquals(GoalUnit.SESSIONS, units[GoalType.SESSIONS])
        assertEquals(GoalUnit.ROUNDS, units[GoalType.ROUNDS])
        assertEquals(GoalUnit.MINUTES, units[GoalType.TRAINING_TIME])
        assertEquals(GoalType.entries.size, units.size)
    }

    /**
     * sec.10.16: enum columns persist `name`, never `ordinal`. Renaming or reordering an entry is
     * therefore a data migration rather than a refactor — this test makes that explicit so the
     * compiler cannot let it slip through.
     */
    @Test
    fun `enum names are the persisted representation and must stay stable`() {
        assertEquals(listOf("IN_PROGRESS", "COMPLETED"), WorkoutStatus.entries.map { it.name })
        assertEquals(listOf("STRENGTH", "BOXING"), ActivityType.entries.map { it.name })
        assertEquals(listOf("NORMAL", "WARMUP", "DROP", "FAILURE"), SetType.entries.map { it.name })
        assertEquals(
            listOf("WEIGHTED", "BODYWEIGHT", "BODYWEIGHT_PLUS", "ASSISTED"),
            LoadType.entries.map { it.name },
        )
        assertEquals(listOf("REPS", "DURATION"), MeasureType.entries.map { it.name })
        assertEquals(
            listOf("MAX_WEIGHT", "REPS_AT_WEIGHT", "BEST_E1RM", "BEST_SET_VOLUME", "MAX_DURATION"),
            PRType.entries.map { it.name },
        )
        assertEquals(listOf("NONE", "WEEKLY", "MONTHLY"), GoalPeriod.entries.map { it.name })
        assertEquals(listOf("ACTIVE", "ACHIEVED", "ARCHIVED"), GoalStatus.entries.map { it.name })
        assertEquals(listOf("AUTO", "JALALI", "GREGORIAN"), CalendarSystem.entries.map { it.name })
        assertEquals(listOf("AUTO", "LATIN", "PERSIAN"), DigitSystem.entries.map { it.name })
        assertEquals(listOf("ENGLISH", "PERSIAN"), AppLanguage.entries.map { it.name })
        assertEquals(listOf("en", "fa"), AppLanguage.entries.map { it.tag })
        assertEquals(
            listOf("SHADOW_BOXING", "HEAVY_BAG", "PADS", "SPARRING", "JUMP_ROPE", "CUSTOM"),
            BoxingType.entries.map { it.name },
        )
        assertEquals(listOf("PRIMARY", "SECONDARY"), MuscleRole.entries.map { it.name })
        assertEquals(
            listOf("BEGINNER", "INTERMEDIATE", "ADVANCED"),
            ExerciseDifficulty.entries.map { it.name },
        )
        assertEquals(
            listOf(
                "CHEST", "BACK", "SHOULDERS", "BICEPS", "TRICEPS", "FOREARMS",
                "ABS", "OBLIQUES", "LOWER_BACK", "GLUTES", "QUADRICEPS", "HAMSTRINGS",
                "CALVES", "HIPS", "NECK", "FULL_BODY",
            ),
            Muscle.entries.map { it.name },
        )
        assertEquals(
            listOf(
                "NONE", "PULL_UP_BAR", "PARALLEL_BARS", "BENCH", "BARBELL", "DUMBBELL",
                "CABLE", "MACHINE", "KETTLEBELL", "RESISTANCE_BAND", "OTHER",
            ),
            Equipment.entries.map { it.name },
        )
    }
}
