package com.warrior.tracker.feature.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warrior.tracker.R
import com.warrior.tracker.core.common.AppLanguage
import com.warrior.tracker.core.common.CalendarSystem
import com.warrior.tracker.core.common.DigitSystem
import com.warrior.tracker.core.common.ThemeMode

/**
 * Settings (sec.13): theme, language (fa/en), calendar system and digit system.
 * All writes go through [SettingsRepository] → DataStore; nothing leaves the device.
 *
 * Chip rows are horizontally scrollable so a row never overflows in English (wider labels) or
 * under a large accessibility font scale (sec.17).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SettingSection(title = stringResource(R.string.settings_theme)) {
                ChipRow {
                    ThemeMode.entries.forEach { mode ->
                        OptionChip(
                            selected = state.themeMode == mode,
                            label = stringResource(
                                when (mode) {
                                    ThemeMode.SYSTEM -> R.string.settings_theme_system
                                    ThemeMode.LIGHT -> R.string.settings_theme_light
                                    ThemeMode.DARK -> R.string.settings_theme_dark
                                }
                            ),
                            onSelect = { viewModel.setTheme(mode) },
                        )
                    }
                }
            }

            SettingSection(title = stringResource(R.string.settings_language)) {
                ChipRow {
                    OptionChip(
                        selected = state.language == null,
                        label = stringResource(R.string.settings_language_system),
                        onSelect = { viewModel.setLanguage(null) },
                    )
                    AppLanguage.entries.forEach { language ->
                        OptionChip(
                            selected = state.language == language,
                            // Language names are endonyms and must not be translated.
                            label = stringResource(
                                when (language) {
                                    AppLanguage.ENGLISH -> R.string.settings_language_en
                                    AppLanguage.PERSIAN -> R.string.settings_language_fa
                                }
                            ),
                            onSelect = { viewModel.setLanguage(language) },
                        )
                    }
                }
            }

            SettingSection(title = stringResource(R.string.settings_calendar)) {
                ChipRow {
                    CalendarSystem.entries.forEach { system ->
                        OptionChip(
                            selected = state.calendarSystem == system,
                            label = stringResource(
                                when (system) {
                                    CalendarSystem.AUTO -> R.string.settings_auto
                                    CalendarSystem.JALALI -> R.string.settings_jalali
                                    CalendarSystem.GREGORIAN -> R.string.settings_gregorian
                                }
                            ),
                            onSelect = { viewModel.setCalendarSystem(system) },
                        )
                    }
                }
            }

            SettingSection(title = stringResource(R.string.settings_digits)) {
                ChipRow {
                    DigitSystem.entries.forEach { system ->
                        OptionChip(
                            selected = state.digitSystem == system,
                            label = stringResource(
                                when (system) {
                                    DigitSystem.AUTO -> R.string.settings_auto
                                    DigitSystem.LATIN -> R.string.settings_digits_latin
                                    DigitSystem.PERSIAN -> R.string.settings_digits_persian
                                }
                            ),
                            onSelect = { viewModel.setDigitSystem(system) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        content()
    }
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) { content() }
}

@Composable
private fun OptionChip(selected: Boolean, label: String, onSelect: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onSelect,
        label = { Text(label) },
    )
}
