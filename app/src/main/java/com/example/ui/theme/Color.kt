package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Vibrant, Punchy Primary & Accent Colors
val MilkNavy = Color(0xFF0A2540)
val MilkBlue = Color(0xFF0066FF)
val MilkSky = Color(0xFF00B4D8)
val MilkGreen = Color(0xFF00C853)
val MilkAmber = Color(0xFFFF9100)
val MilkRed = Color(0xFFF43F5E)
val MilkPurple = Color(0xFF7C3AED)
val MilkTeal = Color(0xFF00BFA5)

val MilkBgLight = Color(0xFFF6F8FC)
val MilkSurfaceLight = Color(0xFFFFFFFF)
val MilkBorderLight = Color(0xFFE2E8F0)
val MilkTextPrimaryLight = Color(0xFF0F172A)
val MilkTextSecondaryLight = Color(0xFF475569)

val MilkNavyDark = Color(0xFF071426)
val MilkBlueDark = Color(0xFF38BDF8)
val MilkSurfaceDark = Color(0xFF0F1A2B)
val MilkBgDark = Color(0xFF080E18)
val MilkBorderDark = Color(0xFF1E293B)
val MilkTextPrimaryDark = Color(0xFFF8FAFC)
val MilkTextSecondaryDark = Color(0xFF94A3B8)

/**
 * Rich, High-Energy Gradients for Visual Impact and Sharp Modern Polish
 */
object AppGradients {
    // Top Bar Header Gradient: Deep Royal Navy to Electric Blue to Cyan
    val TopBar = Brush.horizontalGradient(
        colors = listOf(Color(0xFF0A192F), Color(0xFF0052D4), Color(0xFF0091FF))
    )

    // Hero Card: Royal Blue to Electric Sapphire to Bright Sky
    val RoyalHero = Brush.linearGradient(
        colors = listOf(Color(0xFF0A1A3A), Color(0xFF0044CC), Color(0xFF0099FF))
    )

    // Cobalt Blue Metric Card (Total Milk)
    val BlueCobalt = Brush.linearGradient(
        colors = listOf(Color(0xFF0D47A1), Color(0xFF0066FF), Color(0xFF40C4FF))
    )

    // Lush Emerald Green (Total Payment / Profit / Quality)
    val EmeraldVibrant = Brush.linearGradient(
        colors = listOf(Color(0xFF00695C), Color(0xFF00C853), Color(0xFF69F0AE))
    )

    // Warm Butterfat Amber / Gold (Average Fat %)
    val AmberGold = Brush.linearGradient(
        colors = listOf(Color(0xFFE65100), Color(0xFFFF9100), Color(0xFFFFD54F))
    )

    // Electric Purple / AI Intelligence (TS Milk / AI Advisor)
    val PurpleAi = Brush.linearGradient(
        colors = listOf(Color(0xFF4A148C), Color(0xFF7C3AED), Color(0xFFC084FC))
    )

    // Sunset Coral Red (Farmers / Alerts)
    val SunsetCoral = Brush.linearGradient(
        colors = listOf(Color(0xFF9F1239), Color(0xFFE11D48), Color(0xFFFB7185))
    )

    // Deep Ocean Teal (Total Entries)
    val OceanTeal = Brush.linearGradient(
        colors = listOf(Color(0xFF006064), Color(0xFF00838F), Color(0xFF00E5FF))
    )

    // Vibrant Mint / Green (Average LR)
    val MintFresh = Brush.linearGradient(
        colors = listOf(Color(0xFF00796B), Color(0xFF00897B), Color(0xFF4DB6AC))
    )

    // Radiant Tangerine (Average TS)
    val OrangeFlame = Brush.linearGradient(
        colors = listOf(Color(0xFFBF360C), Color(0xFFF4511E), Color(0xFFFF8A65))
    )

    // Dark Card Surface Gradient with subtle glow
    val DarkSurface = Brush.linearGradient(
        colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
    )

    // Soft Elegant Screen Background Gradient
    val ScreenBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFFF8FAFC), Color(0xFFEDF2F7))
    )
}

