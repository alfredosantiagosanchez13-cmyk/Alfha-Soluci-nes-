package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MedusaViewModel
import com.example.ui.components.medusaFrame
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
 * Pantalla dedicada de configuración dentro de 'Sleek Nexus'.
 * Incluye gestión de GEMINI_API_KEY con validación estricta, cifrado mediante
 * EncryptedSharedPreferences (Android Security-Crypto AES-256 GCM) y personalización visual.
 */
@Composable
fun SleekNexusSettingsScreen(
    viewModel: MedusaViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val currentConfig by viewModel.nexusThemeConfig.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()

    var selectedPalette by remember(currentConfig) { mutableStateOf(currentConfig.accentPalette) }
    var selectedFont by remember(currentConfig) { mutableStateOf(currentConfig.fontStyle) }
    var selectedGlow by remember(currentConfig) { mutableStateOf(currentConfig.glowLevel) }

    // State for GEMINI_API_KEY input & validation
    var apiKeyInput by remember(customApiKey) { mutableStateOf(customApiKey) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    val hasActiveKey = customApiKey.isNotBlank()
    val isModified = apiKeyInput.trim() != customApiKey.trim()

    val infiniteTransition = rememberInfiniteTransition(label = "NexusGlowScreen")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulsePreviewScreen"
    )

    val previewConfig = remember(selectedPalette, selectedFont, selectedGlow) {
        SleekNexusThemeConfig(
            accentPalette = selectedPalette,
            fontStyle = selectedFont,
            glowLevel = selectedGlow
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .semantics { testTag = "sleek_nexus_settings_screen" },
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // TOP HEADER BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SleekSurfaceVariant)
                        .border(1.dp, SleekBorderSubtle, CircleShape)
                        .semantics { testTag = "back_from_nexus_settings_btn" }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = selectedPalette.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(selectedPalette.highlight)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SLEEK NEXUS · SEGURIDAD & BÓVEDA",
                            style = MaterialTheme.typography.labelSmall,
                            color = selectedPalette.primary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Configuración del Sistema",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary,
                        fontSize = 20.sp
                    )
                }
            }

            // Security badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(selectedPalette.primaryContainer)
                    .border(1.dp, selectedPalette.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = selectedPalette.highlight,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AES-256 GCM",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = selectedPalette.highlight
                    )
                }
            }
        }

        // ==========================================
        // SECTION 1: GEMINI_API_KEY & CRIPTOGRAFÍA
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .medusaFrame(
                    accentColor = selectedPalette.primary,
                    isSelected = true,
                    chamferRadius = 12.dp
                )
                .semantics { testTag = "gemini_api_key_card" },
            colors = CardDefaults.cardColors(containerColor = SleekSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with Key icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(selectedPalette.primaryContainer)
                                .border(1.dp, selectedPalette.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Clave API",
                                tint = selectedPalette.highlight,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "GEMINI_API_KEY",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Almacenamiento en EncryptedSharedPreferences",
                                style = MaterialTheme.typography.labelSmall,
                                color = selectedPalette.primary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Active vault state
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (hasActiveKey) Color(0xFF064E3B) else SleekSurfaceVariant)
                            .border(
                                1.dp,
                                if (hasActiveKey) Color(0xFF10B981) else SleekBorderSubtle,
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (hasActiveKey) "● BÓVEDA ACTIVA" else "○ CLAVE POR DEFECTO",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasActiveKey) Color(0xFF6EE7B7) else SleekTextMuted
                        )
                    }
                }

                Text(
                    text = "Introduce tu clave personal de la API de Gemini. La clave se cifra inmediatamente en hardware mediante Android Security-Crypto y se almacena en EncryptedSharedPreferences para su recuperación segura sin exponerse.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = SleekTextSecondary,
                    lineHeight = 16.sp
                )

                // TEXT FIELD FOR GEMINI_API_KEY
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = {
                        apiKeyInput = it
                        if (validationError != null && it.isNotBlank()) {
                            validationError = null
                        }
                        saveSuccessMessage = null
                    },
                    label = {
                        Text(
                            text = "GEMINI_API_KEY",
                            color = if (validationError != null) Color(0xFFFF5252) else selectedPalette.primary,
                            fontSize = 12.sp
                        )
                    },
                    placeholder = {
                        Text(
                            text = if (hasActiveKey) "Clave cifrada activa (••••••••••••)" else "Pega tu clave (AIzaSy...)",
                            color = SleekTextMuted,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "sleek_nexus_api_key_input" },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { isPasswordVisible = !isPasswordVisible },
                            modifier = Modifier.semantics { testTag = "toggle_api_key_visibility_btn" }
                        ) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Ocultar clave" else "Mostrar clave",
                                tint = selectedPalette.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    isError = validationError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = selectedPalette.primary,
                        unfocusedBorderColor = SleekBorder,
                        errorBorderColor = Color(0xFFFF5252),
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary,
                        cursorColor = selectedPalette.primary,
                        errorTextColor = SleekTextPrimary
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // VALIDATOR ERROR MESSAGE (Non-empty verification)
                AnimatedVisibility(
                    visible = validationError != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF3B1219))
                            .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .semantics { testTag = "api_key_validation_error" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error de validación",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = validationError ?: "",
                            color = Color(0xFFFF8A80),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // SUCCESS FEEDBACK BANNER
                AnimatedVisibility(
                    visible = saveSuccessMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF063E28))
                            .border(1.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .semantics { testTag = "api_key_save_success" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Guardado con éxito",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = saveSuccessMessage ?: "",
                            color = Color(0xFFA7F3D0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // ACTION BUTTONS: GUARDAR & BORRAR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón para borrar clave (si existe)
                    if (hasActiveKey) {
                        OutlinedButton(
                            onClick = {
                                viewModel.clearApiKey()
                                apiKeyInput = ""
                                validationError = null
                                saveSuccessMessage = "Clave eliminada de la bóveda segura"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { testTag = "clear_api_key_btn" },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SleekBorderSubtle),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextSecondary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Eliminar clave",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Borrar Clave", fontSize = 12.sp)
                        }
                    }

                    // BOTÓN PRINCIPAL PARA GUARDAR LA CLAVE CON VALIDACIÓN & ENCRYPTEDSHAREDPREFERENCES
                    Button(
                        onClick = {
                            val trimmedKey = apiKeyInput.trim()
                            // Validador de no-vacío estricto
                            if (trimmedKey.isEmpty()) {
                                validationError = "La clave de API no puede estar vacía. Ingrese una GEMINI_API_KEY válida."
                                saveSuccessMessage = null
                            } else {
                                validationError = null
                                val saved = viewModel.updateApiKey(trimmedKey)
                                if (saved) {
                                    saveSuccessMessage = "Clave cifrada con AES-256 GCM y almacenada en EncryptedSharedPreferences"
                                } else {
                                    validationError = "Error al cifrar y guardar la clave en el almacenamiento seguro"
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(if (hasActiveKey) 1.5f else 1f)
                            .semantics { testTag = "save_api_key_btn" },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectedPalette.primary,
                            contentColor = selectedPalette.primaryDark
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Guardar Clave Cifrada",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Security Explanation Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SleekSurfaceVariant)
                        .border(1.dp, SleekBorderSubtle, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = selectedPalette.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Android Security-Crypto: Las claves son cifradas con MasterKey AES-256 GCM y aisladas del entorno del sistema.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = SleekTextMuted,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // ==========================================
        // SECTION 2: PALETAS DE COLOR DE ACENTO
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, SleekBorderSubtle, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SleekSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = null,
                        tint = selectedPalette.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PALETAS DE ACENTO SLEEK NEXUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = selectedPalette.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NexusAccentPalette.values().forEach { palette ->
                        val isSelected = selectedPalette == palette
                        val animatedBorderColor by animateColorAsState(
                            targetValue = if (isSelected) palette.primary else SleekBorderSubtle,
                            label = "paletteBorderScreen"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) palette.surfaceHighlight else SleekSurfaceVariant)
                                .border(if (isSelected) 1.5.dp else 1.dp, animatedBorderColor, RoundedCornerShape(12.dp))
                                .clickable { selectedPalette = palette }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .semantics {
                                    testTag = "screen_accent_palette_${palette.id}"
                                    contentDescription = "Seleccionar ${palette.title}"
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
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
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = palette.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) palette.primary else SleekTextPrimary,
                                    fontSize = 13.sp
                                )
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
            }
        }

        // ==========================================
        // SECTION 3: ESTILOS TIPOGRÁFICOS
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, SleekBorderSubtle, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SleekSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatSize,
                        contentDescription = null,
                        tint = selectedPalette.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TIPOGRAFÍA FUTURISTA",
                        style = MaterialTheme.typography.labelSmall,
                        color = selectedPalette.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NexusFontStyle.values().forEach { fontStyle ->
                        val isSelected = selectedFont == fontStyle
                        val animatedBorderColor by animateColorAsState(
                            targetValue = if (isSelected) selectedPalette.primary else SleekBorderSubtle,
                            label = "fontBorderScreen"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) selectedPalette.surfaceHighlight else SleekSurfaceVariant)
                                .border(if (isSelected) 1.5.dp else 1.dp, animatedBorderColor, RoundedCornerShape(12.dp))
                                .clickable { selectedFont = fontStyle }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .semantics {
                                    testTag = "screen_font_style_${fontStyle.id}"
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Aa",
                                fontFamily = fontStyle.displayFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isSelected) selectedPalette.highlight else SleekTextSecondary,
                                modifier = Modifier.width(30.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fontStyle.title,
                                    fontFamily = fontStyle.displayFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) selectedPalette.primary else SleekTextPrimary,
                                    fontSize = 13.sp
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
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECTION 4: RESPLANDOR & APLICAR CAMBIOS
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    selectedPalette = NexusAccentPalette.HYPER_VIOLET
                    selectedFont = NexusFontStyle.NEXUS_TECH
                    selectedGlow = NexusGlowLevel.BALANCED
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { testTag = "screen_reset_nexus_btn" },
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

            Button(
                onClick = {
                    viewModel.updateNexusTheme(previewConfig)
                    onNavigateBack()
                },
                modifier = Modifier
                    .weight(1.4f)
                    .semantics { testTag = "screen_apply_nexus_btn" },
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
                Text("Guardar y Aplicar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
