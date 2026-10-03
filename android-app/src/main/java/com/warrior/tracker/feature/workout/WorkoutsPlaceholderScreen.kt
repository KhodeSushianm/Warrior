package com.warrior.tracker.feature.workout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.warrior.tracker.R

/** Phase 1 delivers the Workout Draft + Strength logging flow (sec.8). Placeholder keeps CI green now. */
@Composable
fun WorkoutsPlaceholderScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = stringResource(R.string.nav_workouts),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(R.string.coming_soon_phase_1),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
