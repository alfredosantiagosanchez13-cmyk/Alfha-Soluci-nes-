package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun SistemaMedusaTheme(
    darkTheme: Boolean = true, // Default to sleek dark interface
    themeConfig: SleekNexusThemeConfig = SleekNexusThemeConfig(),
    content: @Composable () -> Unit
) {
    val colorScheme = themeConfig.toColorScheme()
    val typography = themeConfig.toTypography()
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SleekBackground.toArgb()
            window.navigationBarColor = SleekBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    CompositionLocalProvider(LocalSleekNexusTheme provides themeConfig) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

