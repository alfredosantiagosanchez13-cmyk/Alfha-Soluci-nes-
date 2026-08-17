package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.NexusAccentPalette
import com.example.ui.theme.NexusFontStyle
import com.example.ui.theme.NexusGlowLevel
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekNexusThemeConfig
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceHeader
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

/**
 * Panel de Configuración Futurista 'Sleek Nexus' para personalizar colores de acento,
 * estilos tipográficos y atmósfera de resplandor.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SleekNexusSettingsDialog(
    currentConfig: SleekNexusThemeConfig,
    currentApiKey: String = "",
    onDismiss: () -> Unit,
    onApplyConfig: (SleekNexusThemeConfig) -> Unit,
    onSaveApiKey: (String) -> Boolean = { true },
    onClearApiKey: () -> Unit = {}
) {
    var selectedPalette by remember { mutableStateOf(currentConfig.accentPalette) }
    var selectedFont by remember { mutableStateOf(currentConfig.fontStyle) }
    var selectedGlow by remember { mutableStateOf(currentConfig.glowLevel) }

    val previewConfig = remember(selectedPalette, selectedFont, selectedGlow) {
        SleekNexusThemeConfig(
            accentPalette = selectedPalette,
            fontStyle = selectedFont,
            glowLevel = selectedGlow
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "NexusGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulsePreview"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 700.dp)
                .semantics { testTag = "sleek_nexus_settings_dialog" }
                .clip(RoundedCornerShape(24.dp))
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                selectedPalette.primary.copy(alpha = 0.8f),
                                selectedPalette.glow.copy(alpha = 0.5f),
                                SleekBorderSubtle
                            )
                        )
                    ),
                    RoundedCornerShape(24.dp)
                ),
            color = SleekBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(selectedPalette.primaryContainer)
                                .border(1.dp, selectedPalette.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = selectedPalette.highlight,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "SLEEK NEXUS CUSTOMIZER",
                                style = MaterialTheme.typography.labelSmall,
                                color = selectedPalette.primary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Personalización Visual",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary,
                                fontSize = 18.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SleekSurfaceVariant)
                            .border(1.dp, SleekBorderSubtle, CircleShape)
                            .semantics { testTag = "close_nexus_settings_button" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = SleekTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // LIVE PREVIEW CARD
                Text(
                    text = "VISTA PREVIA EN TIEMPO REAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                LivePreviewMatrix(
                    config = previewConfig,
                    pulseScale = pulseScale
                )

                Spacer(modifier = Modifier.height(20.dp))

                // SECTION 1: ACCENT COLOR PALETTES
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = null,
                        tint = selectedPalette.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PALETAS DE ACENTO CUÁNTICO",
                        style = MaterialTheme.typography.labelSmall,
                        color = selectedPalette.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Grid/List of Accent Colors
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NexusAccentPalette.values().forEach { palette ->
                        val isSelected = selectedPalette == palette
                        val animatedBorderColor by animateColorAsState(
                            targetValue = if (isSelected) palette.primary else SleekBorderSubtle,
                            label = "paletteBorder"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) palette.surfaceHighlight else SleekSurface)
                                .border(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    animatedBorderColor,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedPalette = palette }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .semantics {
                                    testTag = "accent_palette_option_${palette.id}"
                                    contentDescription = "Seleccionar paleta ${palette.title}"
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Swatch Orb
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(palette.primary, palette.primaryContainer, palette.primaryDark)
                                        )
                                    )
                                    .border(1.5.dp, palette.glow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = palette.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) palette.primary else SleekTextPrimary,
                                        fontSize = 14.sp
                                    )

                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(palette.primaryContainer)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVA",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = palette.highlight
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = palette.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = SleekTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SECTION 2: FONT STYLES
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatSize,
                        contentDescription = null,
                        tint = selectedPalette.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ESTILOS TIPOGRÁFICOS FUTURISTAS",
                        style = MaterialTheme.typography.labelSmall,
                        color = selectedPalette.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NexusFontStyle.values().forEach { fontStyle ->
                        val isSelected = selectedFont == fontStyle
                        val animatedBorderColor by animateColorAsState(
                            targetValue = if (isSelected) selectedPalette.primary else SleekBorderSubtle,
                            label = "fontBorder"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) selectedPalette.surfaceHighlight else SleekSurface)
                                .border(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    animatedBorderColor,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedFont = fontStyle }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .semantics {
                                    testTag = "font_style_option_${fontStyle.id}"
                                    contentDescription = "Seleccionar estilo de fuente ${fontStyle.title}"
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) selectedPalette.primaryContainer else SleekSurfaceVariant)
                                    .border(1.dp, if (isSelected) selectedPalette.primary else SleekBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aa",
                                    fontFamily = fontStyle.displayFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) selectedPalette.highlight else SleekTextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fontStyle.title,
                                    fontFamily = fontStyle.displayFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) selectedPalette.primary else SleekTextPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = fontStyle.subtitle,
                                    fontFamily = fontStyle.primaryFontFamily,
                                    fontSize = 11.sp,
                                    color = SleekTextSecondary
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = selectedPalette.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SECTION 3: GLOW INTENSITY
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = selectedPalette.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ATMÓSFERA Y RESPLANDOR (GLOW)",
                        style = MaterialTheme.typography.labelSmall,
                        color = selectedPalette.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NexusGlowLevel.values().forEach { glow ->
                        val isSelected = selectedGlow == glow
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) selectedPalette.primaryContainer else SleekSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) selectedPalette.primary else SleekBorderSubtle,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedGlow = glow }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = glow.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) selectedPalette.highlight else SleekTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SECTION 4: SEGURIDAD & GEMINI API KEY LOCAL (EncryptedSharedPreferences)
                SleekNexusApiKeyComponent(
                    currentApiKey = currentApiKey,
                    accentPalette = selectedPalette,
                    onSaveApiKey = onSaveApiKey,
                    onClearApiKey = onClearApiKey
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ACTION BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset to default
                    OutlinedButton(
                        onClick = {
                            selectedPalette = NexusAccentPalette.HYPER_VIOLET
                            selectedFont = NexusFontStyle.NEXUS_TECH
                            selectedGlow = NexusGlowLevel.BALANCED
                        },
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = "reset_nexus_theme_btn" },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SleekBorderSubtle),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restablecer", fontSize = 12.sp)
                    }

                    // Apply Button
                    Button(
                        onClick = {
                            onApplyConfig(previewConfig)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .semantics { testTag = "apply_nexus_theme_btn" },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectedPalette.primary,
                            contentColor = selectedPalette.primaryDark
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Aplicar Nexus",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Matriz interactiva de demostración en tiempo real para visualizar cómo afecta la configuración
 * elegida a las tarjetas, tipografías, botones e indicadores de estado.
 */
@Composable
private fun LivePreviewMatrix(
    config: SleekNexusThemeConfig,
    pulseScale: Float
) {
    val palette = config.accentPalette
    val fontStyle = config.fontStyle

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(palette.primary.copy(alpha = 0.7f), palette.glow.copy(alpha = 0.3f), SleekBorder)
                ),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = SleekSurfaceHeader)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header demo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(palette.glow)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SISTEMA MEDUSA • ${palette.title.uppercase()}",
                        fontFamily = fontStyle.labelFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.primary,
                        letterSpacing = 1.sp
                    )
                }

                // Mini active badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(palette.primaryDark)
                        .border(1.dp, palette.primary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "V4.2 MATRIX",
                        fontFamily = fontStyle.labelFontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.highlight
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sample Display Headline
            Text(
                text = "Inteligencia Neural Activa",
                fontFamily = fontStyle.displayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = SleekTextPrimary
            )

            // Sample Body text
            Text(
                text = "Resonancia cuántica sincronizada con el modelo Gemini 2.5 Flash y la base de datos Room DB.",
                fontFamily = fontStyle.primaryFontFamily,
                fontSize = 12.sp,
                color = SleekTextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Sample Components Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sample Primary Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(palette.primary)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ejecutar Acción",
                        fontFamily = fontStyle.primaryFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = palette.primaryDark
                    )
                }

                // Sample Secondary Glow Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(palette.primaryContainer.copy(alpha = 0.5f))
                        .border(1.dp, palette.glow, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Memoria 98%",
                        fontFamily = fontStyle.labelFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.highlight
                    )
                }
            }
        }
    }
}
