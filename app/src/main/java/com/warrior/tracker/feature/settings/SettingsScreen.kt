package com.warrior.tracker.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.warrior.tracker.core.common.ThemeMode

/**
 * Settings (sec.13): theme, language (fa/en), calendar system and digit system.
 * All writes go through [SettingsRepository] → DataStore; nothing leaves the device.
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = state.themeMode == mode,
                            onClick = { viewModel.setTheme(mode) },
                            label = {
                                Text(
                                    stringResource(
                                        when (mode) {
                                            ThemeMode.SYSTEM -> R.string.settings_theme_system
                                            ThemeMode.LIGHT -> R.string.settings_theme_light
                                            ThemeMode.DARK -> R.string.settings_theme_dark
                                        }
                                    )
                                )
                            },
                        )
                    }
                }
            }

            SettingSection(title = stringResource(R.string.settings_language)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LanguageChip(entry = null, labelRes = R.string.settings_language_system,
                        selected = state.language == null, onSelect = viewModel::setLanguage)
                    LanguageChip(entry = "en", labelRes = R.string.settings_language_en,
                        selected = state.language == "en", onSelect = viewModel::setLanguage)
                    LanguageChip(entry = "fa", labelRes = R.string.settings_language_fa,
                        selected = state.language == "fa", onSelect = viewModel::setLanguage)
                }
            }

            SettingSection(title = stringResource(R.string.settings_calendar)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalDigitChips(
                        options = listOf("AUTO" to R.string.settings_auto, "JALALI" to R.string.settings_jalali, "GREGORIAN" to R.string.settings_gregorian),
                        selected = state.calendarSystem,
                        onSelect = viewModel::setCalendarSystem,
                    )
                }
            }

            SettingSection(title = stringResource(R.string.settings_digits)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalDigitChips(
                        options = listOf("AUTO" to R.string.settings_auto, "LATIN" to R.string.settings_digits_latin, "PERSIAN" to R.string.settings_digits_persian),
                        selected = state.digitSystem,
                        onSelect = viewModel::setDigitSystem,
                    )
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
private fun LanguageChip(
    entry: String?,
    labelRes: Int,
    selected: Boolean,
    onSelect: (String?) -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = { onSelect(entry) },
        label = { Text(stringResource(labelRes)) },
    )
}

@Composable
private fun CalDigitChips(
    options: List<Pair<String, Int>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    options.forEach { (value, labelRes) ->
        FilterChip(
            selected = selected == value,
            onClick = { onSelect(value) },
            label = { Text(stringResource(labelRes)) },
            modifier = Modifier.padding(end = 8.dp),
        )
    }
}
