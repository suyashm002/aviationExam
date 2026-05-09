package com.suyash.mockcivilaviationexam.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val AviationDarkColorScheme = darkColorScheme(
    primary = AviationGold,
    onPrimary = Color(0xFF1A1200),
    primaryContainer = AviationGoldDark,
    onPrimaryContainer = AviationGoldLight,

    secondary = AvionicsBlue,
    onSecondary = Color.White,
    secondaryContainer = AvionicsBlueDark,
    onSecondaryContainer = AvionicsBlueLight,

    tertiary = HUDGreen,
    onTertiary = Color.White,
    tertiaryContainer = HUDGreenDark,
    onTertiaryContainer = Color(0xFFA7F3D0),

    background = CockpitBackground,
    onBackground = TextHighEmphasis,
    surface = CockpitSurface,
    onSurface = TextHighEmphasis,
    surfaceVariant = CockpitSurfaceVariant,
    onSurfaceVariant = TextMediumEmphasis,
    surfaceContainerHighest = CockpitSurfaceBright,

    error = AviationError,
    onError = Color.White,
    errorContainer = ErrorContainerDark,
    onErrorContainer = AviationError,

    outline = CockpitBorder,
    outlineVariant = Color(0xFF1E2A40),

    inverseSurface = SkySurface,
    inverseOnSurface = TextDark,
    inversePrimary = AviationGoldDark
)

private val AviationLightColorScheme = lightColorScheme(
    primary = AvionicsBlueDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = AvionicsBlueDark,

    secondary = AviationGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF7ED),
    onSecondaryContainer = AviationGoldDark,

    tertiary = HUDGreenDark,
    onTertiary = Color.White,
    tertiaryContainer = SuccessContainerLight,
    onTertiaryContainer = HUDGreenDark,

    background = SkyBackground,
    onBackground = TextDark,
    surface = SkySurface,
    onSurface = TextDark,
    surfaceVariant = SkySurfaceVariant,
    onSurfaceVariant = TextDarkSecondary,

    error = AviationError,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = AviationErrorDark,

    outline = TextDarkTertiary,
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun MockcivilAviationExamTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AviationDarkColorScheme
        else -> AviationLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
