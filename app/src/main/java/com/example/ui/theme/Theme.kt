package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = MilkBlueDark,
    onPrimary = MilkNavyDark,
    primaryContainer = MilkNavy,
    onPrimaryContainer = Color.White,
    secondary = MilkSky,
    onSecondary = Color.White,
    tertiary = MilkGreen,
    background = MilkBgDark,
    surface = MilkSurfaceDark,
    onBackground = MilkTextPrimaryDark,
    onSurface = MilkTextPrimaryDark,
    outline = MilkBorderDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MilkBlue,
    onPrimary = Color.White,
    primaryContainer = MilkNavy,
    onPrimaryContainer = Color.White,
    secondary = MilkSky,
    onSecondary = Color.White,
    tertiary = MilkGreen,
    onTertiary = Color.White,
    background = MilkBgLight,
    surface = MilkSurfaceLight,
    onBackground = MilkTextPrimaryLight,
    onSurface = MilkTextPrimaryLight,
    outline = MilkBorderLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
