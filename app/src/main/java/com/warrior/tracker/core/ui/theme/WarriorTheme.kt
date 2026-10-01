package com.warrior.tracker.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.warrior.tracker.core.common.ThemeMode

/**
 * Dark-first Material 3 palette (sec.14.2): soft neutral blue as primary, rounded cards,
 * high-contrast dark surfaces. Colors are literals here so both light/dark stay in sync
 * with docs; XML colors.xml only carries the pre-Compose window background.
 */
private val WarriorBlue = Color(0xFF5B7FA6)
private val WarriorBlueLight = Color(0xFF3F6188)
private val SurfaceDark = Color(0xFF0F1115)
private val CardDark = Color(0xFF171A20)
private val OnDark = Color(0xFFE6E9EF)
private val SurfaceLight = Color(0xFFF7F8FA)
private val CardLight = Color(0xFFFFFFFF)
private val OnLight = Color(0xFF14171C)

private val DarkColors = darkColorScheme(
    primary = WarriorBlue,
    onPrimary = Color(0xFF0A1220),
    secondary = Color(0xFF8FA6BD),
    background = SurfaceDark,
    onBackground = OnDark,
    surface = CardDark,
    onSurface = OnDark,
    surfaceVariant = Color(0xFF22262E),
    onSurfaceVariant = Color(0xFFBFC7D1),
    error = Color(0xFFCF6679),
    onError = Color(0xFF1D0E10),
)

private val LightColors = lightColorScheme(
    primary = WarriorBlueLight,
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF5C7CA0),
    background = SurfaceLight,
    onBackground = OnLight,
    surface = CardLight,
    onSurface = OnLight,
    surfaceVariant = Color(0xFFE8EBF0),
    onSurfaceVariant = Color(0xFF44505E),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

/** Key numbers rendered large (sec.14.2) — metric tiles use [MetricTypography]. */
val WarriorTypography = Typography()

@Composable
fun WarriorTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = WarriorTypography,
        content = content,
    )
}
