package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SleekDarkColorScheme = darkColorScheme(
    primary = SleekVioletPrimary,
    onPrimary = SleekVioletDark,
    primaryContainer = SleekVioletContainer,
    onPrimaryContainer = SleekVioletHighlight,
    secondary = SleekCyanGlow,
    onSecondary = SleekBackground,
    background = SleekBackground,
    onBackground = SleekTextPrimary,
    surface = SleekSurface,
    onSurface = SleekTextPrimary,
    surfaceVariant = SleekSurfaceVariant,
    onSurfaceVariant = SleekTextSecondary,
    outline = SleekBorder,
    outlineVariant = SleekBorderSubtle
)

@Composable
fun SistemaMedusaTheme(
    darkTheme: Boolean = true, // Default to sleek dark interface
    content: @Composable () -> Unit
) {
    val colorScheme = SleekDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SleekBackground.toArgb()
            window.navigationBarColor = SleekBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
