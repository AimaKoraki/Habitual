package com.aima.habitual.ui.theme

import androidx.compose.ui.graphics.Color

// --- LIGHT MODE COLORS ---
val LightBg = Color(0xFFF8F9F7)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceSubtle = Color(0xFFF3F6F4)

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
val DarkSurface = Color(0xFF151816)
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
val StreakFire = Color(0xFFFF7043) // Deep Orange 400

// Error container colors — earthy rose instead of M3's default pink
val LightErrorContainer   = Color(0xFFF5DDDA)   // Muted warm rose
val LightOnErrorContainer = Color(0xFF5C1A1A)   // Dark rosewood text
val DarkErrorContainer    = Color(0xFF3D2020)    // Deep rosewood surface
val DarkOnErrorContainer  = Color(0xFFF5C6C0)   // Soft peach text

// --- RED THEME (LIGHT MODE) ---
val RedLightBg = Color(0xFFFCF8F8)             // Warm reddish white
val RedLightSurface = Color(0xFFFFFFFF)
val RedLightSurfaceSubtle = Color(0xFFF7F2F2)

val RedLightSurfaceTint = Color(0xFFD32F2F)
val RedLightSurfaceContainer = Color(0xFFF5EDED)
val RedLightSurfaceContainerLow = Color(0xFFFAF4F4)
val RedLightSurfaceContainerHigh = Color(0xFFEBE0E0)

val RedLightTextPrimary = Color(0xFF1F0F0F)
val RedLightTextSecondary = Color(0xFF1F0F0F).copy(alpha = 0.65f)

val RedLightAccentPrimary = Color(0xFFD32F2F)   // Brighter Crimson
val RedLightAccentSoft = Color(0xFFFFCDD2)      // Soft pink/red tint

val RedLightSecondaryContainer = Color(0xFFFFEBEE)   // Warm blush container
val RedLightOnSecondaryContainer = Color(0xFFB71C1C) // Deep red text

val RedLightBorderSubtle = Color.Black.copy(alpha = 0.06f)
val RedLightBorderStrong = Color.Black.copy(alpha = 0.12f)

// --- RED THEME (DARK MODE) ---
val RedDarkBg = Color(0xFF140C0C)              // Deep warm black
val RedDarkSurface = Color(0xFF1C1111)
val RedDarkSurfaceSubtle = Color(0xFF261717)

val RedDarkSurfaceTint = Color(0xFFEF5350)
val RedDarkSurfaceContainer = Color(0xFF241515)
val RedDarkSurfaceContainerLow = Color(0xFF1A0F0F)
val RedDarkSurfaceContainerHigh = Color(0xFF2E1B1B)

val RedDarkTextPrimary = Color(0xFFFFFFFF)
val RedDarkTextSecondary = Color.White.copy(alpha = 0.75f)

val RedDarkAccentPrimary = Color(0xFFEF5350)   // Brighter Coral/Red
val RedDarkAccentSoft = Color(0xFFEF5350).copy(alpha = 0.15f)

val RedDarkSecondaryContainer = Color(0xFF3B1B1B)     // Deep wine container
val RedDarkOnSecondaryContainer = Color(0xFFFFCDD2)   // Soft peach/pink text

val RedDarkBorderSubtle = Color.White.copy(alpha = 0.06f)
val RedDarkBorderStrong = Color.White.copy(alpha = 0.12f)

enum class AppTheme {
    GREEN, RED
}