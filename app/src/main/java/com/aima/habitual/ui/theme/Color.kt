package com.aima.habitual.ui.theme

import androidx.compose.ui.graphics.Color

// --- LIGHT MODE COLORS ---
val LightBg = Color(0xFFF8F9F7)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceSubtle = Color(0xFFE7F3EE)

val LightTextPrimary = Color(0xFF0F1110)
val LightTextSecondary = Color(0xFF111313).copy(alpha = 0.60f)
val LightTextMuted = Color(0xFF111313).copy(alpha = 0.45f)

val LightAccentPrimary = Color(0xFF1B4D3E)
val LightAccentHover = Color(0xFF246454)
val LightAccentSoft = Color(0xFFCDE8DC)

val LightBorderSubtle = Color.Black.copy(alpha = 0.06f)
val LightBorderStrong = Color.Black.copy(alpha = 0.12f)

// --- DARK MODE COLORS ---
val DarkBg = Color(0xFF0F1110)
val DarkSurface = Color(0xFF161917)
val DarkSurfaceSubtle = Color(0xFF1E2220)

val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color.White.copy(alpha = 0.75f)
val DarkTextMuted = Color.White.copy(alpha = 0.50f)

val DarkAccentPrimary = Color(0xFFB7D9C9)
val DarkAccentHover = Color(0xFFCDE8DC)
val DarkAccentSoft = Color(0xFFB7D9C9).copy(alpha = 0.15f)

val DarkBorderSubtle = Color.White.copy(alpha = 0.06f)
val DarkBorderStrong = Color.White.copy(alpha = 0.12f)

// Semantic Colors (Shared)
val SuccessGreen = Color(0xFF3A8F6E)
val WarningGold = Color(0xFFD4A84F)
val DangerRed = Color(0xFF8A2D2D)