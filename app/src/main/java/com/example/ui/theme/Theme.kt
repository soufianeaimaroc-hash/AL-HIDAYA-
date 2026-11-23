package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.core.AppStrings
import com.example.core.LocalAppStrings

private val LightColors = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = PearlSurface,
    primaryContainer = EmeraldSoft,
    onPrimaryContainer = EmeraldDeep,
    secondary = GoldRoyal,
    onSecondary = EmeraldDeep,
    secondaryContainer = GoldSoft,
    onSecondaryContainer = GoldAntique,
    tertiary = EmeraldVibrant,
    onTertiary = PearlSurface,
    background = IvoryBackground,
    onBackground = CharcoalInk,
    surface = PearlSurface,
    onSurface = CharcoalInk,
    surfaceVariant = SandSurfaceVariant,
    onSurfaceVariant = MutedMoss,
    outline = SubtleGoldBorder,
    error = ErrorCrimson,
    onError = PearlSurface
)

private val DarkColors = darkColorScheme(
    primary = EmeraldVibrant,
    onPrimary = PearlSurface,
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = EmeraldSoft,
    secondary = GoldChampagne,
    onSecondary = EmeraldDeep,
    secondaryContainer = GoldDarkContainer,
    onSecondaryContainer = GoldChampagne,
    tertiary = GoldRoyal,
    onTertiary = EmeraldDeep,
    background = ObsidianBackground,
    onBackground = MistInk,
    surface = ObsidianSurface,
    onSurface = MistInk,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = MutedSage,
    outline = GoldAntique,
    error = ErrorCrimson,
    onError = PearlSurface
)

private val SoufianShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun AlHidayaTheme(
    themeMode: String = "system",
    language: String = "ar",
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val isArabic = language == "ar"
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    val strings = AppStrings.forLanguage(language)

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalAppStrings provides strings
    ) {
        MaterialTheme(
            colorScheme = if (isDark) DarkColors else LightColors,
            typography = buildSoufianTypography(isArabic),
            shapes = SoufianShapes,
            content = content
        )
    }
}
