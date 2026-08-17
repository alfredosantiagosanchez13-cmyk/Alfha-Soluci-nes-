package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * =========================================================================
 * SISTEMA VISUAL PROPIETARIO: MEDUSA ALPHA (MEDUSA FRAME & DELIMITACIONES)
 * Estética futurista, tecnológica, institucional, táctica y de alta precisión.
 * =========================================================================
 */

// Paleta Institucional Medusa Alpha
object MedusaAlphaPalette {
    val DeepObsidian = Color(0xFF070913)
    val DarkGraphite = Color(0xFF0C101E)
    val PanelTitanium = Color(0xFF10172B)
    val PanelSurface = Color(0xFF141E38)

    val ElectricBlue = Color(0xFF00E5FF)
    val TechBlue = Color(0xFF2979FF)
    val InstitutionalGold = Color(0xFFFFD54F)
    val NobleGold = Color(0xFFFFC107)

    val StatusOperating = Color(0xFF00E676)
    val StatusAttention = Color(0xFFFFB300)
    val StatusCritical = Color(0xFFFF3D00)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFCFD8DC)
    val TextMuted = Color(0xFF90A4AE)
    val TechGridLine = Color(0x2400E5FF)
}

/**
 * Genera una forma geométrica con esquinas tácticas (cortes biselados sutiles)
 * para paneles de control de seguridad.
 */
fun medusaChamferedShape(chamferSize: Float = 14f): GenericShape {
    return GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val c = chamferSize.coerceAtMost(w / 4f).coerceAtMost(h / 4f)

        reset()
        // Top-left chamfer
        moveTo(c, 0f)
        lineTo(w - c, 0f)
        // Top-right chamfer
        lineTo(w, c)
        lineTo(w, h - c)
        // Bottom-right chamfer
        lineTo(w - c, h)
        lineTo(c, h)
        // Bottom-left chamfer
        lineTo(0f, h - c)
        lineTo(0f, c)
        close()
    }
}

/**
 * 1. MEDUSA FRAME - Modificador de Delimitación Táctica
 * Dibuja un marco oscuro de alta precisión con esquinas geométricas, microcircuitos
 * sutiles, puntos luminosos en esquinas y respuesta interactiva al toque.
 */
@Composable
fun Modifier.medusaFrame(
    accentColor: Color = MedusaAlphaPalette.ElectricBlue,
    isHighlighted: Boolean = false,
    isSelected: Boolean = false,
    chamferRadius: Dp = 8.dp,
    showCornerNodes: Boolean = true,
    showCircuitTraces: Boolean = true
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "medusaPulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val cornerPx = chamferRadius.value * 2.5f

    return this
        .clip(RoundedCornerShape(chamferRadius))
        .background(
            Brush.verticalGradient(
                listOf(
                    MedusaAlphaPalette.PanelTitanium.copy(alpha = if (isSelected) 0.98f else 0.92f),
                    MedusaAlphaPalette.DarkGraphite.copy(alpha = 0.95f)
                )
            )
        )
        .drawWithContent {
            drawContent()

            val w = size.width
            val h = size.height
            val activeAlpha = when {
                isSelected -> 0.95f
                isHighlighted -> pulseGlow
                else -> 0.40f
            }
            val mainAccent = accentColor.copy(alpha = activeAlpha)
            val goldTrim = MedusaAlphaPalette.InstitutionalGold.copy(alpha = activeAlpha * 0.7f)

            // Marco exterior fino y elegante
            drawRoundRect(
                brush = Brush.linearGradient(
                    listOf(
                        mainAccent,
                        Color(0x33FFFFFF),
                        goldTrim,
                        mainAccent.copy(alpha = activeAlpha * 0.3f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                ),
                topLeft = Offset(0.5f, 0.5f),
                size = Size(w - 1f, h - 1f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerPx, cornerPx),
                style = Stroke(width = if (isSelected) 1.6f else 1.1f)
            )

            // Micro-detalles en esquinas: Brackets tácticos
            val bracketLen = 14f
            val strokeB = 2.0f

            // Top-Left bracket
            drawLine(
                color = mainAccent,
                start = Offset(4f, 4f),
                end = Offset(4f + bracketLen, 4f),
                strokeWidth = strokeB,
                cap = StrokeCap.Square
            )
            drawLine(
                color = mainAccent,
                start = Offset(4f, 4f),
                end = Offset(4f, 4f + bracketLen),
                strokeWidth = strokeB,
                cap = StrokeCap.Square
            )

            // Bottom-Right bracket
            drawLine(
                color = goldTrim,
                start = Offset(w - 4f, h - 4f),
                end = Offset(w - 4f - bracketLen, h - 4f),
                strokeWidth = strokeB,
                cap = StrokeCap.Square
            )
            drawLine(
                color = goldTrim,
                start = Offset(w - 4f, h - 4f),
                end = Offset(w - 4f, h - 4f - bracketLen),
                strokeWidth = strokeB,
                cap = StrokeCap.Square
            )

            // Microcircuitos decorativos muy sutiles en los bordes
            if (showCircuitTraces) {
                val traceColor = mainAccent.copy(alpha = 0.22f)
                // Línea de circuito horizontal superior con nodo
                drawLine(
                    color = traceColor,
                    start = Offset(w * 0.35f, 3f),
                    end = Offset(w * 0.65f, 3f),
                    strokeWidth = 1f
                )
                drawCircle(
                    color = mainAccent.copy(alpha = 0.5f),
                    radius = 1.5f,
                    center = Offset(w * 0.65f, 3f)
                )

                // Trazo inferior sutil
                drawLine(
                    color = goldTrim.copy(alpha = 0.20f),
                    start = Offset(w * 0.15f, h - 3f),
                    end = Offset(w * 0.45f, h - 3f),
                    strokeWidth = 1f
                )
            }

            // Pequeños puntos luminosos en esquinas estratégicas
            if (showCornerNodes) {
                // Top Right luminous node
                drawCircle(
                    color = mainAccent,
                    radius = 2.2f,
                    center = Offset(w - 10f, 10f)
                )
                drawCircle(
                    color = mainAccent.copy(alpha = 0.3f),
                    radius = 4.5f,
                    center = Offset(w - 10f, 10f)
                )

                // Bottom Left luminous node
                drawCircle(
                    color = goldTrim,
                    radius = 1.8f,
                    center = Offset(10f, h - 10f)
                )
            }

            // Resplandor tenue en la parte superior (efecto cristal oscuro)
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.18f),
                        mainAccent.copy(alpha = 0.35f),
                        Color.Transparent
                    )
                ),
                start = Offset(15f, 1.5f),
                end = Offset(w - 15f, 1.5f),
                strokeWidth = 1.5f
            )
        }
}

/**
 * 2. TARJETA DE MÓDULO INTERACTIVA MEDUSA FRAME
 * Con elevación táctica, respuesta háptica visual, micro-circuitos y estado operativo.
 */
@Composable
fun MedusaModuleCard(
    title: String,
    category: String,
    icon: String,
    statusText: String,
    isOperating: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 1.dp,
        animationSpec = tween(150),
        label = "cardElevation"
    )

    val frameAccent by animateColorAsState(
        targetValue = if (isPressed) MedusaAlphaPalette.InstitutionalGold else accentColor,
        animationSpec = tween(150),
        label = "frameColor"
    )

    Box(
        modifier = modifier
            .shadow(elevation, RoundedCornerShape(14.dp), ambientColor = accentColor.copy(alpha = 0.3f))
            .medusaFrame(
                accentColor = frameAccent,
                isSelected = isPressed,
                chamferRadius = 14.dp
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Fila Superior: Icono y Diagnóstico de Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono dentro de micro-contenedor tecnológico
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MedusaAlphaPalette.DarkGraphite)
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    accentColor.copy(alpha = 0.6f),
                                    MedusaAlphaPalette.InstitutionalGold.copy(alpha = 0.3f)
                                )
                            ),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 20.sp)
                }

                // Indicador de Estado Tecnológico
                MedusaStatusIndicator(
                    status = if (isOperating) MedusaOperationalStatus.OPERANDO else MedusaOperationalStatus.ATENCION,
                    customLabel = statusText
                )
            }

            // Título del Módulo
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MedusaAlphaPalette.TextPrimary,
                fontSize = 14.sp,
                maxLines = 1
            )

            // Categoría con código de seguridad
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Text(
                    text = category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MedusaAlphaPalette.TextMuted,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * 3. SEPARADORES DE SECCIÓN FUTURISTAS
 * Integra el título con una línea tecnológica horizontal con nodos geométricos
 * y segmentos de circuito elegantes (no texto ASCII).
 */
@Composable
fun MedusaSectionHeader(
    title: String,
    iconGlyph: String = "◈",
    accentColor: Color = MedusaAlphaPalette.InstitutionalGold,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glifo geométrico
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(accentColor.copy(alpha = 0.15f))
                .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iconGlyph,
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Título en mayúsculas con espaciado técnico
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = accentColor,
            letterSpacing = 1.6.sp,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Trazo gráfico de circuito horizontal con Canvas
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
        ) {
            val h = size.height / 2f
            val w = size.width

            // Línea de circuito principal con degradado
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(
                        accentColor.copy(alpha = 0.8f),
                        MedusaAlphaPalette.ElectricBlue.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, h),
                end = Offset(w, h),
                strokeWidth = 1.2f
            )

            // Nodo de circuito intermedio 1 (Punto con halo)
            val node1X = (w * 0.25f).coerceAtMost(40f)
            drawCircle(
                color = accentColor,
                radius = 2.5f,
                center = Offset(node1X, h)
            )
            drawCircle(
                color = accentColor.copy(alpha = 0.35f),
                radius = 5f,
                center = Offset(node1X, h)
            )

            // Pequeño escalón geométrico de circuito
            if (w > 80f) {
                val stepX = node1X + 16f
                val path = Path().apply {
                    moveTo(stepX, h)
                    lineTo(stepX + 8f, h - 3f)
                    lineTo(stepX + 24f, h - 3f)
                    lineTo(stepX + 30f, h)
                }
                drawPath(
                    path = path,
                    color = MedusaAlphaPalette.ElectricBlue.copy(alpha = 0.5f),
                    style = Stroke(width = 1f)
                )

                // Micro nodo final del escalón
                drawCircle(
                    color = MedusaAlphaPalette.ElectricBlue,
                    radius = 1.8f,
                    center = Offset(stepX + 30f, h)
                )
            }
        }
    }
}

/**
 * 5. ESTADOS DE LOS MÓDULOS (DIAGNÓSTICO TECNOLÓGICO)
 */
enum class MedusaOperationalStatus {
    OPERANDO,
    ATENCION,
    CRITICO
}

@Composable
fun MedusaStatusIndicator(
    status: MedusaOperationalStatus,
    modifier: Modifier = Modifier,
    customLabel: String? = null
) {
    val (baseColor, defaultLabel) = when (status) {
        MedusaOperationalStatus.OPERANDO -> Pair(MedusaAlphaPalette.StatusOperating, "OPERANDO")
        MedusaOperationalStatus.ATENCION -> Pair(MedusaAlphaPalette.StatusAttention, "ATENCIÓN")
        MedusaOperationalStatus.CRITICO -> Pair(MedusaAlphaPalette.StatusCritical, "CRÍTICO")
    }

    val displayLabel = customLabel ?: defaultLabel

    val infiniteTransition = rememberInfiniteTransition(label = "statusPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MedusaAlphaPalette.DarkGraphite)
            .border(
                width = 0.8.dp,
                color = baseColor.copy(alpha = 0.45f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Punto diagnóstico con doble halo concéntrico
            Box(
                modifier = Modifier
                    .size(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(baseColor.copy(alpha = pulseAlpha * 0.4f))
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(baseColor)
                )
            }

            Text(
                text = displayLabel.uppercase(),
                color = baseColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
        }
    }
}
