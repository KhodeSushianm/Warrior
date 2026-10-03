package com.warrior.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warrior.tracker.core.common.ThemeMode
import com.warrior.tracker.core.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Hosts the only app-wide UI state in Phase 0: theme mode from DataStore (sec.13). */
@HiltViewModel
class MainViewModel @Inject constructor(
    settings: SettingsRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settings.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)
}
