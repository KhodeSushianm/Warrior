package com.warrior.tracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warrior.tracker.core.common.AppLanguage
import com.warrior.tracker.core.common.CalendarSystem
import com.warrior.tracker.core.common.DigitSystem
import com.warrior.tracker.core.common.ThemeMode
import com.warrior.tracker.core.settings.ResolvedSettings
import com.warrior.tracker.core.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Settings screen state (sec.13). Every option is a typed enum — the previous `String`-based
 * "AUTO"/"JALALI" values could silently mismatch between the writer and the reader.
 */
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** null = follow the system language. */
    val language: AppLanguage? = null,
    val calendarSystem: CalendarSystem = CalendarSystem.AUTO,
    val digitSystem: DigitSystem = DigitSystem.AUTO,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {

    val state: StateFlow<SettingsUiState> = settings.resolved
        .map { r: ResolvedSettings ->
            SettingsUiState(
                themeMode = r.themeMode,
                // Report the *override*, not the resolved value, so "System default" stays selected
                // when the user has not chosen a language explicitly.
                language = if (r.languageIsOverridden) r.language else null,
                calendarSystem = r.calendarSystem,
                digitSystem = r.digitSystem,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { settings.setThemeMode(mode) }
    fun setLanguage(language: AppLanguage?) = viewModelScope.launch { settings.setLanguage(language?.tag) }
    fun setCalendarSystem(value: CalendarSystem) = viewModelScope.launch { settings.setCalendarSystem(value) }
    fun setDigitSystem(value: DigitSystem) = viewModelScope.launch { settings.setDigitSystem(value) }
}
