package com.warrior.tracker.core.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.core.os.LocaleListCompat
import com.warrior.tracker.core.common.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Persisted keys — DataStore is the ONLY settings store (sec.5, sec.10.14). */
internal val THEME_KEY = stringPreferencesKey("theme_mode")         // SYSTEM|LIGHT|DARK
internal val CALENDAR_KEY = stringPreferencesKey("calendar_system") // AUTO|JALALI|GREGORIAN
internal val DIGITS_KEY = stringPreferencesKey("digit_system")      // AUTO|LATIN|PERSIAN
internal val LANGUAGE_KEY = stringPreferencesKey("app_language")    // ""|en|fa

/**
 * App preferences (DataStore Preferences). Defaults follow sec.13:
 * theme System, calendar Auto (fa→Jalali), digits Auto-by-language.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore get() = context.warriorPreferences

    val themeMode: Flow<ThemeMode> = dataStore.data
        .map { prefs -> prefs[THEME_KEY]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM }

    val calendarSystem: Flow<String> = dataStore.data
        .map { it[CALENDAR_KEY] ?: "AUTO" }

    val digitSystem: Flow<String> = dataStore.data
        .map { it[DIGITS_KEY] ?: "AUTO" }

    /** Raw stored override; null means "follow system". */
    val languageOverride: Flow<String?> = dataStore.data
        .map { prefs -> prefs[LANGUAGE_KEY]?.takeIf { it.isNotEmpty() } }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_KEY] = mode.name }
    }

    suspend fun setCalendarSystem(value: String) {
        dataStore.edit { it[CALENDAR_KEY] = value }
    }

    suspend fun setDigitSystem(value: String) {
        dataStore.edit { it[DIGITS_KEY] = value }
    }

    /** Applies via AppCompat per-app languages (sec.13) and persists the choice. */
    suspend fun setLanguage(tag: String?) {
        val locales = tag?.let { LocaleListCompat.create(Locale.forLanguageTag(it)) }
            ?: LocaleListCompat.getEmptyLocaleList()
        AppCompatDelegate.setApplicationLocales(locales)
        dataStore.edit { it[LANGUAGE_KEY] = tag.orEmpty() }
    }

    /** Effective UI language tag ("fa"/"en") — override first, then system locale. */
    suspend fun effectiveLanguage(): String {
        val override = languageOverride.first()
        return override ?: Locale.getDefault().language.take(2)
    }
}

private val Context.warriorPreferences by preferencesDataStore(name = "warrior_settings")
