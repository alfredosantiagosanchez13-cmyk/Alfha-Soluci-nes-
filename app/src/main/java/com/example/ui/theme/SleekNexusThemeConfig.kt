package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Paletas de color de acento 'Sleek Nexus' para personalización futurista.
 */
enum class NexusAccentPalette(
    val id: String,
    val title: String,
    val description: String,
    val primary: Color,
    val primaryDark: Color,
    val primaryContainer: Color,
    val highlight: Color,
    val secondary: Color,
    val glow: Color,
    val surfaceHighlight: Color
) {
    HYPER_VIOLET(
        id = "hyper_violet",
        title = "Hyper Violet",
        description = "Núcleo neural clásico con resonancia electromagnética profunda",
        primary = Color(0xFFD0BCFF),
        primaryDark = Color(0xFF381E72),
        primaryContainer = Color(0xFF4F378B),
        highlight = Color(0xFFEADDFF),
        secondary = Color(0xFF80D8FF),
        glow = Color(0xFFB388FF),
        surfaceHighlight = Color(0xFF2D1B4E)
    ),
    CYBER_CYAN(
        id = "cyber_cyan",
        title = "Cyber Cyan",
        description = "Frecuencia superconductora inspirada en redes cuánticas",
        primary = Color(0xFF00E5FF),
        primaryDark = Color(0xFF004D40),
        primaryContainer = Color(0xFF006064),
        highlight = Color(0xFF84FFFF),
        secondary = Color(0xFF69F0AE),
        glow = Color(0xFF18FFFF),
        surfaceHighlight = Color(0xFF0F3642)
    ),
    EMERALD_QUANTUM(
        id = "emerald_quantum",
        title = "Emerald Quantum",
        description = "Biósfera digital con biosíntesis y armonía ecológica",
        primary = Color(0xFF10B981),
        primaryDark = Color(0xFF064E3B),
        primaryContainer = Color(0xFF047857),
        highlight = Color(0xFFA7F3D0),
        secondary = Color(0xFF38BDF8),
        glow = Color(0xFF34D399),
        surfaceHighlight = Color(0xFF063828)
    ),
    SOLAR_AMBER(
        id = "solar_amber",
        title = "Solar Plasma",
        description = "Reactor de plasma iónico con calidez termonuclear",
        primary = Color(0xFFFFB300),
        primaryDark = Color(0xFF78350F),
        primaryContainer = Color(0xFFB45309),
        highlight = Color(0xFFFDE68A),
        secondary = Color(0xFFFF8A80),
        glow = Color(0xFFFFD54F),
        surfaceHighlight = Color(0xFF452205)
    ),
    CRIMSON_REDLINE(
        id = "crimson_redline",
        title = "Crimson Redline",
        description = "Protocolo táctico de máxima prioridad y potencia de combate",
        primary = Color(0xFFFF5252),
        primaryDark = Color(0xFF881337),
        primaryContainer = Color(0xFFBE123C),
        highlight = Color(0xFFFECDD3),
        secondary = Color(0xFFFFD54F),
        glow = Color(0xFFFF8A80),
        surfaceHighlight = Color(0xFF450A1A)
    ),
    NEON_SAKURA(
        id = "neon_sakura",
        title = "Neon Sakura",
        description = "Estética synthwave Neo-Tokyo con destellos ultravioleta",
        primary = Color(0xFFF472B6),
        primaryDark = Color(0xFF831843),
        primaryContainer = Color(0xFFBE185D),
        highlight = Color(0xFFFBCFE8),
        secondary = Color(0xFF80D8FF),
        glow = Color(0xFFFF80AB),
        surfaceHighlight = Color(0xFF4A0E2E)
    );

    companion object {
        fun fromId(id: String): NexusAccentPalette =
            values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: HYPER_VIOLET
    }
}

/**
 * Estilos tipográficos futuristas seleccionables por el usuario.
 */
enum class NexusFontStyle(
    val id: String,
    val title: String,
    val subtitle: String,
    val primaryFontFamily: FontFamily,
    val displayFontFamily: FontFamily,
    val labelFontFamily: FontFamily,
    val letterSpacingScale: Float
) {
    NEXUS_TECH(
        id = "nexus_tech",
        title = "Nexus Tech",
        subtitle = "Sans-Serif geométrica, moderna y ultra legible",
        primaryFontFamily = FontFamily.SansSerif,
        displayFontFamily = FontFamily.SansSerif,
        labelFontFamily = FontFamily.Monospace,
        letterSpacingScale = 1.0f
    ),
    CYBER_TERMINAL(
        id = "cyber_terminal",
        title = "Cyber Terminal",
        subtitle = "Monospace de consola con estética de matriz y hacking",
        primaryFontFamily = FontFamily.Monospace,
        displayFontFamily = FontFamily.Monospace,
        labelFontFamily = FontFamily.Monospace,
        letterSpacingScale = 1.25f
    ),
    CRYSTAL_SERIF(
        id = "crystal_serif",
        title = "Crystal Serif",
        subtitle = "Serif ejecutiva holográfica con elegancia arquitectónica",
        primaryFontFamily = FontFamily.Serif,
        displayFontFamily = FontFamily.Serif,
        labelFontFamily = FontFamily.Monospace,
        letterSpacingScale = 1.1f
    );

    companion object {
        fun fromId(id: String): NexusFontStyle =
            values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NEXUS_TECH
    }
}

/**
 * Nivel de intensidad de resplandor / brillo 'Glow'.
 */
enum class NexusGlowLevel(
    val id: String,
    val title: String,
    val alphaMultiplier: Float
) {
    SUBTLE("subtle", "Sutil", 0.6f),
    BALANCED("balanced", "Equilibrado", 1.0f),
    OVERDRIVE("overdrive", "Hyper Glow", 1.4f);

    companion object {
        fun fromId(id: String): NexusGlowLevel =
            values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: BALANCED
    }
}

/**
 * Configuración completa del tema Sleek Nexus.
 */
@Immutable
data class SleekNexusThemeConfig(
    val accentPalette: NexusAccentPalette = NexusAccentPalette.HYPER_VIOLET,
    val fontStyle: NexusFontStyle = NexusFontStyle.NEXUS_TECH,
    val glowLevel: NexusGlowLevel = NexusGlowLevel.BALANCED
) {
    fun toColorScheme(): ColorScheme {
        return darkColorScheme(
            primary = accentPalette.primary,
            onPrimary = accentPalette.primaryDark,
            primaryContainer = accentPalette.primaryContainer,
            onPrimaryContainer = accentPalette.highlight,
            secondary = accentPalette.secondary,
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
    }

    fun toTypography(): Typography {
        val dispFamily = fontStyle.displayFontFamily
        val bodyFamily = fontStyle.primaryFontFamily
        val labelFamily = fontStyle.labelFontFamily
        val spacing = fontStyle.letterSpacingScale

        return Typography(
            displayLarge = TextStyle(
                fontFamily = dispFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 38.sp,
                letterSpacing = (-0.5 * spacing).sp,
                color = SleekTextPrimary
            ),
            headlineMedium = TextStyle(
                fontFamily = dispFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = (0.0 * spacing).sp,
                color = SleekTextPrimary
            ),
            titleMedium = TextStyle(
                fontFamily = dispFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                letterSpacing = (0.15 * spacing).sp,
                color = SleekTextPrimary
            ),
            bodyLarge = TextStyle(
                fontFamily = bodyFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                letterSpacing = (0.25 * spacing).sp,
                color = SleekTextPrimary
            ),
            bodyMedium = TextStyle(
                fontFamily = bodyFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = (0.25 * spacing).sp,
                color = SleekTextSecondary
            ),
            labelSmall = TextStyle(
                fontFamily = labelFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = (1.25 * spacing).sp,
                color = accentPalette.primary
            )
        )
    }
}

val LocalSleekNexusTheme = staticCompositionLocalOf {
    SleekNexusThemeConfig()
}
