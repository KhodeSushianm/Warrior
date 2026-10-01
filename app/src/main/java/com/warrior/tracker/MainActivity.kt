package com.warrior.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warrior.tracker.core.navigation.WarriorAppRoot
import com.warrior.tracker.core.ui.theme.WarriorTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single Activity host (sec.4.3). Per-app language is applied through AppCompat locales;
 * theme mode comes from DataStore (sec.13). RTL is driven by supportsRtl + locale (sec.3 shell).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            WarriorTheme(themeMode = themeMode) {
                WarriorAppRoot()
            }
        }
    }
}
