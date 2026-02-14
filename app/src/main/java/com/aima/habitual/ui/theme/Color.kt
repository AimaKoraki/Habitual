package com.aima.habitual.ui.theme

import androidx.compose.ui.graphics.Color

// --- LIGHT MODE COLORS ---
val LightBg = Color(0xFFF8F9F7)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceSubtle = Color(0xFFF1F4F2)

// Light surface containers (M3 tonal elevation)
val LightSurfaceTint = Color(0xFF18463A)
val LightSurfaceContainer = Color(0xFFF1F4F2)
val LightSurfaceContainerLow = Color(0xFFF7F9F7)
val LightSurfaceContainerHigh = Color(0xFFE9EEEA)

val LightTextPrimary = Color(0xFF0F1110)
val LightTextSecondary = Color(0xFF0F1110).copy(alpha = 0.65f)
val LightTextMuted = Color(0xFF111313).copy(alpha = 0.45f)

val LightAccentPrimary = Color(0xFF18463A)
val LightAccentSoft = Color(0xFFCDE8DC)

val LightSecondaryContainer = Color(0xFFE3F0EA)   // Warm sage container
val LightOnSecondaryContainer = Color(0xFF1B4D3E)  // Dark green text on sage

val LightBorderSubtle = Color.Black.copy(alpha = 0.06f)
val LightBorderStrong = Color.Black.copy(alpha = 0.12f)

// --- DARK MODE COLORS ---
val DarkBg = Color(0xFF0F1110)
val DarkSurface = Color(0xFF171A18)
val DarkSurfaceSubtle = Color(0xFF1F2421)

// Dark surface containers (M3 tonal elevation)
val DarkSurfaceTint = Color(0xFFB7D9C9)
val DarkSurfaceContainer = Color(0xFF1E2320)
val DarkSurfaceContainerLow = Color(0xFF181C19)
val DarkSurfaceContainerHigh = Color(0xFF262C29)

val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color.White.copy(alpha = 0.75f)
val DarkTextMuted = Color.White.copy(alpha = 0.50f)

val DarkAccentPrimary = Color(0xFFB0C9BB) // Premium Sage (less minty)
val DarkAccentSoft = Color(0xFFB7D9C9).copy(alpha = 0.15f)

val DarkSecondaryContainer = Color(0xFF1E2E27)      // Deep green container
val DarkOnSecondaryContainer = Color(0xFFB7D9C9)    // Light sage text

val DarkBorderSubtle = Color.White.copy(alpha = 0.06f)
val DarkBorderStrong = Color.White.copy(alpha = 0.12f)

// Semantic Colors (Shared)
val SuccessGreen = Color(0xFF3A8F6E)
val WarningGold = Color(0xFFD4A84F)
val DangerRed = Color(0xFF8A2D2D)