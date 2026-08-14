package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalSleekNexusTheme
import com.example.ui.theme.SleekCyanGlow
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.theme.SleekVioletContainer
import com.example.ui.theme.SleekVioletDark
import com.example.ui.theme.SleekVioletHighlight
import com.example.ui.theme.SleekVioletPrimary
import kotlinx.coroutines.delay

/**
 * Indicador de progreso futurista con pulsaciones cuánticas, ondas de energía y
 * oscilador neural para estados de espera de la API Gemini.
 */
@Composable
fun FuturisticPulsingIndicator(
    modifier: Modifier = Modifier,
    statusText: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "FuturisticPulse")

    // Ripple 1 scale & alpha
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseScale1"
    )
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha1"
    )

    // Ripple 2 (staggered)
    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseScale2"
    )
    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha2"
    )

    // Core glow oscillation
    val coreGlow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CoreGlow"
    )

    // Shimmer sweep for energy bar
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerOffset"
    )

    // Rotating phase text
    var phaseIndex by remember { mutableIntStateOf(0) }
    val phases = remember {
        listOf(
            "Sincronizando Núcleo Gemini 2.5 Flash...",
            "Consultando memorias y contexto vecinal...",
            "Procesando razonamiento cognitivo...",
            "Sintetizando respuesta neural..."
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2200)
            phaseIndex = (phaseIndex + 1) % phases.size
        }
    }

    val nexusTheme = LocalSleekNexusTheme.current.accentPalette
    val displayStatus = statusText ?: phases[phaseIndex]

    // Container card with futuristic glowing border
    val cardBorderBrush = Brush.linearGradient(
        colors = listOf(
            nexusTheme.primary.copy(alpha = 0.8f * coreGlow),
            nexusTheme.glow.copy(alpha = 0.6f * coreGlow),
            nexusTheme.primaryDark
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .semantics {
                testTag = "gemini_pulsing_indicator"
                contentDescription = "Generando respuesta de IA Gemini con Núcleo Neural"
            }
            .clip(RoundedCornerShape(18.dp))
            .background(SleekSurfaceVariant.copy(alpha = 0.92f))
            .border(1.dp, cardBorderBrush, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Futuristic Pulsing Node Icon Container
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Ripple 2
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .scale(pulseScale2)
                            .clip(CircleShape)
                            .background(nexusTheme.secondary.copy(alpha = pulseAlpha2 * 0.4f))
                    )

                    // Outer Ripple 1
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .scale(pulseScale1)
                            .clip(CircleShape)
                            .background(nexusTheme.primary.copy(alpha = pulseAlpha1 * 0.6f))
                    )

                    // Core Neural Orb
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .scale(coreGlow)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        nexusTheme.primary,
                                        nexusTheme.primaryContainer,
                                        nexusTheme.primaryDark
                                    )
                                )
                            )
                            .border(1.dp, nexusTheme.glow.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Status Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(nexusTheme.secondary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GEMINI NEURAL CORE",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = nexusTheme.secondary,
                                letterSpacing = 1.sp
                            )
                        }

                        // Mini active badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(nexusTheme.primaryDark.copy(alpha = 0.7f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "2.5 FLASH",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = nexusTheme.highlight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = displayStatus,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SleekTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Waveform / Oscillating Spectrum Bars
                FuturisticWaveformOscillator(
                    infiniteTransition = infiniteTransition,
                    accentColor = nexusTheme.primary,
                    secondaryColor = nexusTheme.secondary,
                    modifier = Modifier.height(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Glowing Scanning Energy Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SleekSurface)
            ) {
                val energyBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        nexusTheme.primary.copy(alpha = 0.3f),
                        nexusTheme.secondary,
                        nexusTheme.highlight,
                        Color.Transparent
                    ),
                    start = Offset(shimmerOffset - 300f, 0f),
                    end = Offset(shimmerOffset, 0f)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(energyBrush)
                )
            }
        }
    }
}

/**
 * Oscilador cuántico de 5 barras con fases desfasadas que simula el flujo sináptico del modelo de IA.
 */
@Composable
private fun FuturisticWaveformOscillator(
    infiniteTransition: androidx.compose.animation.core.InfiniteTransition,
    accentColor: Color = SleekVioletPrimary,
    secondaryColor: Color = SleekCyanGlow,
    modifier: Modifier = Modifier
) {
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar1"
    )

    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, delayMillis = 100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar2"
    )

    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, delayMillis = 180, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar3"
    )

    val bar4 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(620, delayMillis = 80, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar4"
    )

    val bar5 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(430, delayMillis = 240, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar5"
    )

    val bars = listOf(bar1, bar2, bar3, bar4, bar5)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        bars.forEachIndexed { index, scale ->
            val color = when (index % 3) {
                0 -> accentColor
                1 -> secondaryColor
                else -> accentColor.copy(alpha = 0.8f)
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((22 * scale).coerceAtLeast(4f).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color.copy(alpha = (0.6f + scale * 0.4f).coerceIn(0f, 1f)))
            )
        }
    }
}
