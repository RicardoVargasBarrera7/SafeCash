package com.project.safecash.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * SafeCash Professional Color Palette
 * Designed for high readability and a modern "Fintech" look.
 */

// Primary Brand Colors
val PrimaryDark = Color(0xFF0F172A)      // Slate 900 (Deep Navy)
val PrimaryBlue = Color(0xFF1E293B)      // Slate 800 (Professional Blue)
val AccentBlue = Color(0xFF3B82F6)       // Blue 500 (Action Blue)
val SecondaryBlue = Color(0xFF64748B)    // Slate 500 (Subtle Text/Icons)

// Semantic Colors
val SuccessGreen = Color(0xFF10B981)     // Emerald 500
val ErrorRed = Color(0xFFEF4444)         // Red 500
val WarningOrange = Color(0xFFF59E0B)    // Amber 500

// Background and Surface
val BackgroundLight = Color(0xFFF8FAFC)  // Slate 50 (Off-white)
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceCard = Color(0xFFFFFFFF)
val BorderLight = Color(0xFFE2E8F0)      // Slate 200

// Text Colors
val TextPrimary = Color(0xFF0F172A)      // Slate 900
val TextSecondary = Color(0xFF475569)    // Slate 600
val TextTertiary = Color(0xFF94A3B8)     // Slate 400

// Gradients
val GradientPrimary = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
val GradientAccent = listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
val GradientStatusActive = listOf(Color(0xFF10B981), Color(0xFF059669))

// Legacy compatibility (renaming to avoid breaking all files at once if possible)
val BackgroundGray = BackgroundLight
val TextDark = TextPrimary
val TextLight = TextSecondary
val AccentGreen = SuccessGreen
val GradientStart = Color(0xFF1E293B)
val GradientEnd = Color(0xFF0F172A)
val CardGradientStart = Color(0xFF3B82F6)
val CardGradientEnd = Color(0xFF2563EB)
