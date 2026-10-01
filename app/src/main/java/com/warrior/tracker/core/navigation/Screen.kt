package com.warrior.tracker.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.ui.graphics.vector.ImageVector
import com.warrior.tracker.R

/** Bottom navigation — 4 tabs (sec.3): Home · Workouts · History · Progress. Settings opens from Home. */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Workouts : Screen("workouts")
    data object History : Screen("history")
    data object Progress : Screen("progress")
    data object Settings : Screen("settings")
}

enum class BottomTab(
    val screen: Screen,
    val labelRes: Int,
    val icon: ImageVector,
) {
    HOME(Screen.Home, R.string.nav_home, Icons.Filled.Home),
    WORKOUTS(Screen.Workouts, R.string.nav_workouts, Icons.Filled.FitnessCenter),
    HISTORY(Screen.History, R.string.nav_history, Icons.Filled.History),
    PROGRESS(Screen.Progress, R.string.nav_progress, Icons.Filled.Insights),
}
