package com.warrior.tracker.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.warrior.tracker.feature.history.HistoryPlaceholderScreen
import com.warrior.tracker.feature.home.HomeScreen
import com.warrior.tracker.feature.progress.ProgressPlaceholderScreen
import com.warrior.tracker.feature.settings.SettingsScreen
import com.warrior.tracker.feature.workout.WorkoutsPlaceholderScreen

/**
 * App shell — Bottom Navigation with 4 tabs (sec.3). Settings is a route opened from Home's
 * toolbar icon, not a tab. All layouts use Start/End so RTL mirrors correctly (sec.13).
 */
@Composable
fun WarriorAppRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val onSettingsRoute = currentDestination?.route == Screen.Settings.route

    Scaffold(
        bottomBar = {
            if (!onSettingsRoute) {
                NavigationBar {
                    BottomTab.entries.forEach { tab ->
                        val selected = currentDestination
                            ?.hierarchy
                            ?.any { it.route == tab.screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                androidx.compose.material3.Icon(
                                    imageVector = tab.icon,
                                    contentDescription = stringResource(tab.labelRes),
                                )
                            },
                            label = { Text(stringResource(tab.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Home.route) {
                HomeScreen(onOpenSettings = { navController.navigate(Screen.Settings.route) })
            }
            composable(Screen.Workouts.route) { WorkoutsPlaceholderScreen() }
            composable(Screen.History.route) { HistoryPlaceholderScreen() }
            composable(Screen.Progress.route) { ProgressPlaceholderScreen() }
            composable(Screen.Settings.route) { SettingsScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
