package com.warrior.tracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.warrior.tracker.core.navigation.WarriorAppRoot
import com.warrior.tracker.core.settings.SettingsRepository
import com.warrior.tracker.core.ui.theme.WarriorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Single Activity host (sec.4.3).
 *
 * Extends `AppCompatActivity`, not `ComponentActivity`: `AppCompatDelegate.setApplicationLocales`
 * — the per-app-language mechanism sec.13 mandates — is only honoured by AppCompat activities on
 * API 32 and below. With a plain ComponentActivity the language switch silently did nothing there.
 * The window theme is therefore a `Theme.AppCompat.DayNight` descendant.
 *
 * RTL is driven by `supportsRtl` + the applied locale; theme mode comes from DataStore (sec.13).
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject lateinit var settings: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // After super.onCreate so the window/decor is attached before insets are requested.
        enableEdgeToEdge()

        restoreStoredLanguage()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            WarriorTheme(themeMode = themeMode) {
                WarriorAppRoot()
            }
        }
    }

    /**
     * On API 32 and below the AppCompat locale override is not automatically re-applied to a cold
     * process, so read it back from DataStore and set it before the first frame. Guarded by
     * `repeatOnLifecycle` so it cannot outlive the Activity.
     */
    private fun restoreStoredLanguage() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                runCatching { settings.restoreLanguageIfNeeded() }
            }
        }
    }
}
