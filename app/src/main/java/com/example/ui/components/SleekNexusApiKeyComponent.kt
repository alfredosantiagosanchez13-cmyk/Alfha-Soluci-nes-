package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NexusAccentPalette
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

/**
 * Componente Sleek Nexus para gestión segura de GEMINI_API_KEY.
 * Utiliza validación estricta de no-vacío y persistencia cifrada mediante EncryptedSharedPreferences (AES-256 GCM).
 */
@Composable
fun SleekNexusApiKeyComponent(
    currentApiKey: String,
    accentPalette: NexusAccentPalette,
    onSaveApiKey: (String) -> Boolean,
    onClearApiKey: () -> Unit,
    modifier: Modifier = Modifier
) {
    var apiKeyInput by remember(currentApiKey) { mutableStateOf(currentApiKey) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var saveSuccessFeedback by remember { mutableStateOf(false) }

    val hasActiveKey = currentApiKey.isNotBlank()
    val isModified = apiKeyInput.trim() != currentApiKey.trim()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, SleekBorderSubtle, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SleekSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with Security & Encryption Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Clave API",
                        tint = accentPalette.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GEMINI API KEY",
                        style = MaterialTheme.typography.titleSmall,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                // Security Badge (EncryptedSharedPreferences)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentPalette.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Cifrado Seguro",
                        tint = accentPalette.primary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AES-256 GCM",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentPalette.highlight
                    )
                }
            }

            Text(
                text = "Almacenamiento seguro en bóveda de claves del dispositivo (EncryptedSharedPreferences). Tu clave nunca se expone ni se sube a repositorios.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = SleekTextSecondary,
                lineHeight = 15.sp
            )

            // Input Field with validation
            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = {
                    apiKeyInput = it
                    if (validationError != null && it.isNotBlank()) {
                        validationError = null
                    }
                    saveSuccessFeedback = false
                },
                placeholder = {
                    Text(
                        text = if (hasActiveKey) "Clave activa guardada (••••••••••••)" else "Pega tu API Key (AIzaSy...)",
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
                            tint = accentPalette.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                isError = validationError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentPalette.primary,
                    unfocusedBorderColor = SleekBorder,
                    errorBorderColor = Color(0xFFFF5252),
                    focusedTextColor = SleekTextPrimary,
                    unfocusedTextColor = SleekTextPrimary,
                    cursorColor = accentPalette.primary
                ),
                singleLine = true
            )

            // Validation Error Message
            AnimatedVisibility(
                visible = validationError != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error de validación",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = validationError ?: "",
                        color = Color(0xFFFF5252),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Success feedback message
            AnimatedVisibility(
                visible = saveSuccessFeedback,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Guardado con éxito",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Clave guardada y cifrada correctamente",
                        color = Color(0xFF00E676),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Status Bar & Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (hasActiveKey) accentPalette.highlight else SleekTextMuted)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasActiveKey) "Bóveda Activa" else "Sin clave personalizada",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = if (hasActiveKey) accentPalette.highlight else SleekTextMuted
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clear Key button if key exists
                    if (hasActiveKey) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SleekSurfaceVariant)
                                .border(1.dp, SleekBorderSubtle, RoundedCornerShape(8.dp))
                                .clickable {
                                    onClearApiKey()
                                    apiKeyInput = ""
                                    validationError = null
                                    saveSuccessFeedback = false
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .semantics { testTag = "clear_api_key_btn" }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Eliminar clave",
                                    tint = SleekTextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Borrar",
                                    fontSize = 10.sp,
                                    color = SleekTextSecondary
                                )
                            }
                        }
                    }

                    // Save Key Action Button (with validation check)
                    if (isModified || !hasActiveKey) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentPalette.primaryContainer)
                                .border(1.dp, accentPalette.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable {
                                    val trimmedKey = apiKeyInput.trim()
                                    if (trimmedKey.isBlank()) {
                                        validationError = "La clave de API no puede estar vacía"
                                        saveSuccessFeedback = false
                                    } else {
                                        validationError = null
                                        val saved = onSaveApiKey(trimmedKey)
                                        if (saved) {
                                            saveSuccessFeedback = true
                                        } else {
                                            validationError = "Error al cifrar y guardar la clave"
                                        }
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .semantics { testTag = "save_api_key_btn" }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = accentPalette.highlight,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Guardar Cifrada",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentPalette.highlight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
