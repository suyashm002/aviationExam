package com.suyash.mockcivilaviationexam.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── Aviation Cockpit-Inspired Color System ──────────────────────────────

// Dark Theme Surfaces (navy-tinted, never pure black)
val CockpitBackground = Color(0xFF0A0E1A)       // Deepest background
val CockpitSurface = Color(0xFF141B2D)           // Default card/surface
val CockpitSurfaceVariant = Color(0xFF1A2236)    // Elevated surface
val CockpitSurfaceHighlight = Color(0xFF1E2A40)  // Active/selected surface
val CockpitSurfaceBright = Color(0xFF243352)     // Bright elevated surface

// Light Theme Surfaces
val SkyBackground = Color(0xFFF0F4FA)            // Light sky background
val SkySurface = Color(0xFFFFFFFF)               // Clean white surface
val SkySurfaceVariant = Color(0xFFE8EDF5)        // Subtle blue-gray

// Primary Accent — Amber/Gold (cockpit instrument inspired)
val AviationGold = Color(0xFFF5A623)             // Primary accent
val AviationGoldDark = Color(0xFFD4901A)         // Pressed/darker gold
val AviationGoldLight = Color(0xFFFFC55C)        // Light gold for containers
val AviationGoldSubtle = Color(0x1AF5A623)       // 10% gold for backgrounds

// Interactive — Electric Blue (avionics display inspired)
val AvionicsBlue = Color(0xFF3A82FF)             // Interactive elements
val AvionicsBlueDark = Color(0xFF2563EB)         // Pressed blue
val AvionicsBlueLight = Color(0xFF93BBFF)        // Light blue for containers
val AvionicsBlueSubtle = Color(0x1A3A82FF)       // 10% blue for backgrounds

// Status Colors — HUD-inspired
val HUDGreen = Color(0xFF22C55E)                 // Success / correct
val HUDGreenSubtle = Color(0x1A22C55E)           // 10% green background
val HUDGreenDark = Color(0xFF16A34A)             // Darker green
val AviationError = Color(0xFFEF4444)            // Error / incorrect
val AviationErrorSubtle = Color(0x1AEF4444)      // 10% red background
val AviationErrorDark = Color(0xFFDC2626)        // Darker red
val AviationWarning = Color(0xFFF59E0B)          // Warnings
val AviationWarningSubtle = Color(0x1AF59E0B)    // 10% amber background

// Text Colors — Dark Theme (white with controlled opacity)
val TextHighEmphasis = Color(0xDEFFFFFF)         // 87% white — primary text
val TextMediumEmphasis = Color(0x99CCDDF0)       // 60% blue-white — secondary
val TextLowEmphasis = Color(0x61AAB8CC)          // 38% — tertiary/disabled

// Text Colors — Light Theme
val TextDark = Color(0xFF0F172A)                 // Primary text on light
val TextDarkSecondary = Color(0xFF475569)        // Secondary on light
val TextDarkTertiary = Color(0xFF64748B)         // Tertiary on light

// Borders & Outlines
val CockpitBorder = Color(0xFF2A3654)            // Dark theme card borders
val SkyBorder = Color(0xFFD1D5DB)                // Light theme card borders

// Google Sign-In
val GoogleBlue = Color(0xFF4285F4)

// Semantic (on-status text)
val OnSuccess = Color.White
val OnError = Color.White
val OnWarning = Color(0xFF78350F)

// Status Containers — Light theme
val SuccessContainerLight = Color(0xFFF0FDF4)
val ErrorContainerLight = Color(0xFFFEF2F2)
val WarningContainerLight = Color(0xFFFFFBEB)
val OnSuccessContainerLight = Color(0xFF16A34A)
val OnErrorContainerLight = Color(0xFFDC2626)

// Status Containers — Dark theme
val SuccessContainerDark = Color(0xFF0A2E1A)
val ErrorContainerDark = Color(0xFF2E0A0A)

// Elevation Levels
object AviationElevation {
    val Level0: Dp = 0.dp
    val Level1: Dp = 1.dp
    val Level2: Dp = 2.dp
    val Level3: Dp = 4.dp
    val Level4: Dp = 8.dp
}

// Spacing Constants (8dp grid system)
object AviationSpacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
}
