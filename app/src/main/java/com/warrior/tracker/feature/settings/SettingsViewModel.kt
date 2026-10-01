package com.warrior.tracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warrior.tracker.core.common.ThemeMode
import com.warrior.tracker.core.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: String? = null,          // null = follow system
    val calendarSystem: String = "AUTO",   // AUTO|JALALI|GREGORIAN (sec.13)
    val digitSystem: String = "AUTO",      // AUTO|LATIN|PERSIAN (sec.13)
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {

    val state: StateFlow<SettingsUiState> = combine(
        settings.themeMode,
        settings.languageOverride,
        settings.calendarSystem,
        settings.digitSystem,
    ) { theme, lang, cal, digits ->
        SettingsUiState(theme, lang, cal, digits)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { settings.setThemeMode(mode) }
    fun setLanguage(tag: String?) = viewModelScope.launch { settings.setLanguage(tag) }
    fun setCalendarSystem(value: String) = viewModelScope.launch { settings.setCalendarSystem(value) }
    fun setDigitSystem(value: String) = viewModelScope.launch { settings.setDigitSystem(value) }
}
