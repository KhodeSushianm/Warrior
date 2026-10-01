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
 *
 * The full set of Material 3 surface-container and container roles is declared on purpose.
 * Components used by the app shell read roles that `darkColorScheme()`/`lightColorScheme()`
 * otherwise fill from a generic neutral ramp — `NavigationBar` uses `surfaceContainer`, `Card`
 * uses `surfaceContainerLow`, `FilterChip` uses `surfaceContainerLow`/`secondaryContainer` and
 * `TopAppBar` uses `surface`. Leaving them at their defaults produced bars and chips whose grey
 * did not belong to this palette.
 */

// Brand
private val WarriorBlue = Color(0xFF5B7FA6)
private val WarriorBlueLight = Color(0xFF3F6188)

// Dark neutrals
private val SurfaceDark = Color(0xFF0F1115)
private val CardDark = Color(0xFF171A20)
private val OnDark = Color(0xFFE6E9EF)

// Light neutrals
private val SurfaceLight = Color(0xFFF7F8FA)
private val CardLight = Color(0xFFFFFFFF)
private val OnLight = Color(0xFF14171C)

private val DarkColors = darkColorScheme(
    primary = WarriorBlue,
    onPrimary = Color(0xFF0A1220),
    primaryContainer = Color(0xFF2A3D52),
    onPrimaryContainer = Color(0xFFD6E2F0),
    secondary = Color(0xFF8FA6BD),
    onSecondary = Color(0xFF0F1720),
    secondaryContainer = Color(0xFF2C3947),
    onSecondaryContainer = Color(0xFFDCE6F0),
    tertiary = Color(0xFFA79BC0),
    onTertiary = Color(0xFF17121F),
    background = SurfaceDark,
    onBackground = OnDark,
    surface = CardDark,
    onSurface = OnDark,
    surfaceVariant = Color(0xFF22262E),
    onSurfaceVariant = Color(0xFFBFC7D1),
    // Surface-container ladder, harmonised with SurfaceDark/CardDark.
    surfaceDim = Color(0xFF0F1115),
    surfaceBright = Color(0xFF2A2F38),
    surfaceContainerLowest = Color(0xFF0A0C10),
    surfaceContainerLow = Color(0xFF12151A),
    surfaceContainer = Color(0xFF1A1E25),
    surfaceContainerHigh = Color(0xFF22262E),
    surfaceContainerHighest = Color(0xFF2A2F38),
    outline = Color(0xFF6E7883),
    outlineVariant = Color(0xFF333941),
    inverseSurface = Color(0xFFE6E9EF),
    inverseOnSurface = Color(0xFF14171C),
    inversePrimary = WarriorBlueLight,
    surfaceTint = WarriorBlue,
    error = Color(0xFFCF6679),
    onError = Color(0xFF1D0E10),
    errorContainer = Color(0xFF5C2230),
    onErrorContainer = Color(0xFFFFDADD),
    scrim = Color(0xFF000000),
)

private val LightColors = lightColorScheme(
    primary = WarriorBlueLight,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E2F0),
    onPrimaryContainer = Color(0xFF12212F),
    secondary = Color(0xFF5C7CA0),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDDE7F2),
    onSecondaryContainer = Color(0xFF16212C),
    tertiary = Color(0xFF6B5C8E),
    onTertiary = Color(0xFFFFFFFF),
    background = SurfaceLight,
    onBackground = OnLight,
    surface = CardLight,
    onSurface = OnLight,
    surfaceVariant = Color(0xFFE8EBF0),
    onSurfaceVariant = Color(0xFF44505E),
    // Surface-container ladder, harmonised with SurfaceLight/CardLight.
    surfaceDim = Color(0xFFE4E7EC),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFBFCFD),
    surfaceContainer = Color(0xFFF7F8FA),
    surfaceContainerHigh = Color(0xFFF1F3F6),
    surfaceContainerHighest = Color(0xFFEBEEF2),
    outline = Color(0xFF747F8C),
    outlineVariant = Color(0xFFC6CDD6),
    inverseSurface = Color(0xFF2C3138),
    inverseOnSurface = Color(0xFFEFF2F6),
    inversePrimary = WarriorBlue,
    surfaceTint = WarriorBlueLight,
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    scrim = Color(0xFF000000),
)

/**
 * Default Material 3 type scale. sec.14.2's "large key numbers" are applied per-metric-tile by
 * passing an explicit `style` (e.g. `displaySmall`) at the call site, not through a second
 * Typography instance.
 *
 * TODO(sec.13): bundle Vazirmatn and set it as the Persian `fontFamily`. Not done in Phase 0 —
 * adding a font binary is a separate, licence-reviewed change.
 */
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
