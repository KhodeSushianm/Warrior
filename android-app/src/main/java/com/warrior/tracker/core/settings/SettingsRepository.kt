package com.warrior.tracker.core.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.warrior.tracker.core.common.AppLanguage
import com.warrior.tracker.core.common.CalendarSystem
import com.warrior.tracker.core.common.DigitSystem
import com.warrior.tracker.core.common.ThemeMode
import com.warrior.tracker.core.common.derivedWeekStart
import com.warrior.tracker.core.common.enumValueOrDefault
import com.warrior.tracker.core.common.isRtl
import com.warrior.tracker.core.common.resolvesToJalali
import com.warrior.tracker.core.common.resolvesToPersian
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Persisted keys — DataStore is the ONLY settings store (sec.5, sec.10.14). */
internal val THEME_KEY = stringPreferencesKey("theme_mode")         // ThemeMode.name
internal val CALENDAR_KEY = stringPreferencesKey("calendar_system") // CalendarSystem.name
internal val DIGITS_KEY = stringPreferencesKey("digit_system")      // DigitSystem.name
internal val LANGUAGE_KEY = stringPreferencesKey("app_language")    // "" (system) | "en" | "fa"
internal val WEEK_START_KEY = stringPreferencesKey("week_start")    // "" (auto) | DayOfWeek.name

/**
 * Settings after `AUTO` has been resolved against the effective language (sec.13).
 * This is the single value UI and calculators consume, so nobody has to re-derive the rules.
 */
data class ResolvedSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val languageIsOverridden: Boolean = false,
    val calendarSystem: CalendarSystem = CalendarSystem.AUTO,
    val digitSystem: DigitSystem = DigitSystem.AUTO,
    val weekStart: DayOfWeek = DayOfWeek.MONDAY,
) {
    /** True when dates should be rendered in the Jalali calendar (sec.13). */
    val useJalali: Boolean get() = calendarSystem.resolvesToJalali(language)

    /** True when numbers should be rendered with Persian digits (sec.13). */
    val usePersianDigits: Boolean get() = digitSystem.resolvesToPersian(language)

    /** True when the layout direction is right-to-left (sec.13). */
    val isRtl: Boolean get() = language.isRtl()
}

/**
 * App preferences (DataStore Preferences). Defaults follow sec.13:
 * theme System, calendar Auto (fa → Jalali), digits Auto-by-language, week start
 * Saturday for Persian / Monday for English.
 *
 * Every persisted value is parsed defensively: `Enum.valueOf` throws on unknown data, and an
 * exception inside a `Flow.map` would permanently break the settings stream (and with it the UI)
 * until the user cleared app data. Corrupt values fall back to the documented default instead.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore get() = context.warriorPreferences

    val themeMode: Flow<ThemeMode> = dataStore.data
        .map { prefs -> enumValueOrDefault(prefs[THEME_KEY], ThemeMode.SYSTEM) }

    val calendarSystem: Flow<CalendarSystem> = dataStore.data
        .map { prefs -> enumValueOrDefault(prefs[CALENDAR_KEY], CalendarSystem.AUTO) }

    val digitSystem: Flow<DigitSystem> = dataStore.data
        .map { prefs -> enumValueOrDefault(prefs[DIGITS_KEY], DigitSystem.AUTO) }

    /** Raw stored override; null means "follow system". */
    val languageOverride: Flow<String?> = dataStore.data
        .map { prefs -> prefs[LANGUAGE_KEY]?.takeIf { it.isNotEmpty() } }

    /** Explicit week-start override; null means "derive from language" (sec.13). */
    val weekStartOverride: Flow<DayOfWeek?> = dataStore.data
        .map { prefs -> prefs[WEEK_START_KEY]?.let { name -> DayOfWeek.values().firstOrNull { it.name == name } } }

    /**
     * Settings with every `AUTO` resolved — the value UI and calculators should read.
     *
     * Built from a single `dataStore.data` subscription rather than combining the five derived
     * flows above, so one preference change costs one read instead of five collectors waking up.
     *
     * `languageFor` also consults `Locale.getDefault()` when no override is stored. That input is
     * not a Flow, but a system-locale change recreates the Activity (and therefore the ViewModels
     * collecting this), so the value is re-derived at exactly the moment it can have changed.
     */
    val resolved: Flow<ResolvedSettings> = dataStore.data.map { prefs ->
        val override = prefs[LANGUAGE_KEY]?.takeIf { it.isNotEmpty() }
        val language = languageFor(override)
        ResolvedSettings(
            themeMode = enumValueOrDefault(prefs[THEME_KEY], ThemeMode.SYSTEM),
            language = language,
            languageIsOverridden = override != null,
            calendarSystem = enumValueOrDefault(prefs[CALENDAR_KEY], CalendarSystem.AUTO),
            digitSystem = enumValueOrDefault(prefs[DIGITS_KEY], DigitSystem.AUTO),
            weekStart = prefs[WEEK_START_KEY]
                ?.let { name -> DayOfWeek.values().firstOrNull { it.name == name } }
                ?: language.derivedWeekStart(),
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_KEY] = mode.name }
    }

    suspend fun setCalendarSystem(value: CalendarSystem) {
        dataStore.edit { it[CALENDAR_KEY] = value.name }
    }

    suspend fun setDigitSystem(value: DigitSystem) {
        dataStore.edit { it[DIGITS_KEY] = value.name }
    }

    suspend fun setWeekStart(day: DayOfWeek?) {
        dataStore.edit { prefs ->
            if (day == null) prefs.remove(WEEK_START_KEY) else prefs[WEEK_START_KEY] = day.name
        }
    }

    /**
     * Applies via AppCompat per-app languages (sec.13) and persists the choice.
     *
     * `AppCompatDelegate.setApplicationLocales` mutates Activity state and must run on the main
     * thread, while `dataStore.edit` suspends on IO — so the two are dispatched explicitly.
     * On API 33+ the platform persists the choice itself; on API 32 and below AppCompat persists
     * it, and [restoreLanguageIfNeeded] re-applies it on cold start.
     */
    suspend fun setLanguage(tag: String?) {
        dataStore.edit { it[LANGUAGE_KEY] = tag.orEmpty() }
        withContext(Dispatchers.Main.immediate) { applyLocales(tag) }
    }

    /**
     * Re-apply the stored language at process start. Needed on API 32 and below, where the
     * AppCompat locale override lives in a SharedPreferences file that is only read by an
     * AppCompat Activity; calling it unconditionally is harmless on API 33+.
     */
    suspend fun restoreLanguageIfNeeded() {
        val tag = languageOverride.first()
        withContext(Dispatchers.Main.immediate) {
            val current = AppCompatDelegate.getApplicationLocales()
            val desired = tag?.let { LocaleListCompat.forLanguageTags(it) }
                ?: LocaleListCompat.getEmptyLocaleList()
            if (!sameLocales(current, desired)) applyLocales(tag)
        }
    }

    private fun applyLocales(tag: String?) {
        val locales = tag?.let { LocaleListCompat.forLanguageTags(it) }
            ?: LocaleListCompat.getEmptyLocaleList()
        AppCompatDelegate.setApplicationLocales(locales)
    }

    private fun sameLocales(a: LocaleListCompat, b: LocaleListCompat): Boolean {
        if (a.size() != b.size()) return false
        for (i in 0 until a.size()) {
            if (a.get(i)?.toLanguageTag() != b.get(i)?.toLanguageTag()) return false
        }
        return true
    }

    /** Effective UI language: stored override first, then the system locale (sec.13). */
    suspend fun effectiveLanguage(): AppLanguage = languageFor(languageOverride.first())

    /** Effective first day of week: explicit override, else derived from language (sec.13). */
    suspend fun effectiveWeekStart(): DayOfWeek {
        val override = weekStartOverride.first()
        return override ?: effectiveLanguage().derivedWeekStart()
    }

    private fun languageFor(override: String?): AppLanguage =
        AppLanguage.fromTag(override)
            ?: AppLanguage.fromTag(Locale.getDefault().toLanguageTag())
            ?: AppLanguage.ENGLISH
}

private val Context.warriorPreferences by preferencesDataStore(name = "warrior_settings")
