package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Extensiones y utilidades para diseño 'Sleek Nexus' con estilo Glassmorphism oscuro y acentos neón.
 */

// Tonos base para superficies de cristal translúcido oscuro
val GlassObsidianDark = Color(0xEB0A0D18)
val GlassSurfaceDeep = Color(0xD910162B)
val GlassSurfaceMedium = Color(0xB8161E3B)
val GlassSurfaceLight = Color(0x80212C54)
val GlassSpecularWhite = Color(0x33FFFFFF)

// Paleta Neón de alto contraste
val NeonCyan = Color(0xFF00E5FF)
val NeonViolet = Color(0xFFB388FF)
val NeonGold = Color(0xFFFFD54F)
val NeonEmerald = Color(0xFF00E676)
val NeonCrimson = Color(0xFFFF5252)
val NeonBlue = Color(0xFF2979FF)

/**
 * Modificador composable que aplica una superficie Glassmorphism oscura con borde neón brillante y reflejo especular.
 */
@Composable
fun Modifier.sleekGlassmorphism(
    shape: Shape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.2.dp,
    neonAccent: Color = NeonCyan,
    backgroundAlpha: Float = 0.85f,
    includeSpecularShine: Boolean = true
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "glassShinePulse")
    val shinePulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glassPulseAlpha"
    )

    val glassBrush = Brush.verticalGradient(
        colors = listOf(
            GlassSurfaceDeep.copy(alpha = backgroundAlpha),
            GlassObsidianDark.copy(alpha = backgroundAlpha * 0.95f)
        )
    )

    val neonBorderBrush = Brush.linearGradient(
        colors = listOf(
            neonAccent.copy(alpha = shinePulse),
            GlassSpecularWhite.copy(alpha = 0.25f),
            neonAccent.copy(alpha = shinePulse * 0.4f),
            Color(0x00FFFFFF)
        ),
        start = Offset(0f, 0f),
        end = Offset(400f, 600f)
    )

    return this
        .clip(shape)
        .background(glassBrush)
        .border(borderWidth, neonBorderBrush, shape)
        .then(
            if (includeSpecularShine) {
                Modifier.drawBehind {
                    // Reflejo de luz especular horizontal en el borde superior del cristal
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.25f),
                                neonAccent.copy(alpha = 0.5f),
                                Color.White.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(0f, 1f),
                        end = Offset(size.width, 1f),
                        strokeWidth = 2f
                    )
                }
            } else Modifier
        )
}

/**
 * Modificador composable para tarjetas compactas o mini-píldoras con halo neón interactivo.
 */
@Composable
fun Modifier.sleekGlassPill(
    neonColor: Color = NeonCyan,
    shape: Shape = RoundedCornerShape(16.dp),
    borderAlpha: Float = 0.4f
): Modifier {
    return this
        .clip(shape)
        .background(
            Brush.linearGradient(
                listOf(
                    Color(0xFF131A33).copy(alpha = 0.9f),
                    Color(0xFF0C1021).copy(alpha = 0.9f)
                )
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(
                    neonColor.copy(alpha = borderAlpha + 0.2f),
                    Color(0x22FFFFFF),
                    neonColor.copy(alpha = borderAlpha * 0.5f)
                )
            ),
            shape = shape
        )
}
