package com.example.ui.components

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.LocalSleekNexusTheme
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
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
import com.example.ui.voice.VoiceRecognitionManager
import com.example.ui.voice.VoiceState
import kotlinx.coroutines.launch

/**
 * Compact Microphone Button with state awareness and Permission Launcher.
 */
@Composable
fun VoiceInputMicButton(
    voiceManager: VoiceRecognitionManager,
    onSpeechResult: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpenFullHandsFree: (() -> Unit)? = null
) {
    val voiceState by voiceManager.voiceState.collectAsState()
    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceManager.startListening()
        } else {
            showPermissionDialog = true
        }
    }

    val isListening = voiceState is VoiceState.Listening || voiceState is VoiceState.Transcribing || voiceState is VoiceState.Initializing

    // Animated glow pulse when active
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val nexusTheme = LocalSleekNexusTheme.current.accentPalette

    Box(
        modifier = modifier
            .size(40.dp)
            .scale(if (isListening) pulseScale else 1f)
            .clip(CircleShape)
            .background(
                if (isListening) Brush.radialGradient(
                    listOf(nexusTheme.primary, nexusTheme.primaryDark)
                ) else Brush.verticalGradient(
                    listOf(SleekSurfaceVariant, SleekSurface)
                )
            )
            .border(
                width = if (isListening) 1.5.dp else 1.dp,
                color = if (isListening) nexusTheme.secondary else SleekBorder,
                shape = CircleShape
            )
            .clickable {
                if (isListening) {
                    voiceManager.stopListening()
                } else {
                    if (voiceManager.hasRecordAudioPermission()) {
                        voiceManager.onAutoSendCallback = onSpeechResult
                        voiceManager.startListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }
            .semantics {
                testTag = "voice_to_text_mic_button"
                contentDescription = if (isListening) "Detener reconocimiento de voz" else "Hablar a Sistema Medusa"
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.Mic,
            contentDescription = "Micrófono Manos Libres",
            tint = if (isListening) Color.White else nexusTheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }

    if (showPermissionDialog) {
        MicrophonePermissionExplanationDialog(
            onDismiss = { showPermissionDialog = false },
            onRetry = {
                showPermissionDialog = false
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        )
    }
}

/**
 * Full Futuristic Hands-Free Voice Dialog & Live Equalizer Interface
 */
@Composable
fun FuturisticHandsFreeVoiceDialog(
    voiceManager: VoiceRecognitionManager,
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val voiceState by voiceManager.voiceState.collectAsState()
    val liveText by voiceManager.liveTranscribedText.collectAsState()
    val isAutoSend by voiceManager.isHandsFreeAutoSendEnabled.collectAsState()
    val nexusTheme = LocalSleekNexusTheme.current.accentPalette

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceManager.startListening()
        }
    }

    DisposableEffect(Unit) {
        voiceManager.onAutoSendCallback = { result ->
            onSendMessage(result)
            onDismiss()
        }
        if (voiceManager.hasRecordAudioPermission()) {
            voiceManager.startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        onDispose {
            voiceManager.stopListening()
        }
    }

    val rmsDb = when (val state = voiceState) {
        is VoiceState.Listening -> state.rmsDb
        else -> 0f
    }

    val voicePrompts = listOf(
        "¿Quién vive en Casa 04?",
        "Registrar paquete de Amazon para Casa 12",
        "¿Cuáles son los horarios de la alberca?",
        "Generar pase de visita para Juan Pérez",
        "Consultar directivas de seguridad"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                SleekSurfaceVariant.copy(alpha = 0.95f),
                                SleekBackground.copy(alpha = 0.98f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(nexusTheme.primary, nexusTheme.secondary.copy(alpha = 0.6f))
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(nexusTheme.secondary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "INTERFAZ VOCAL MEDUSA",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary,
                                letterSpacing = 1.sp,
                                fontSize = 13.sp
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = SleekTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Futuristic Equalizer / Pulsing Core
                    FuturisticAudioEqualizerMatrix(
                        voiceState = voiceState,
                        rmsDb = rmsDb,
                        accentColor = nexusTheme.primary,
                        glowColor = nexusTheme.secondary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Status Indicator
                    val statusText = when (voiceState) {
                        is VoiceState.Initializing -> "Sincronizando canal de audio..."
                        is VoiceState.Listening -> "Escuchando tu voz en tiempo real..."
                        is VoiceState.Transcribing -> "Transcribiendo comando neural..."
                        is VoiceState.Success -> "Comando reconocido con éxito"
                        is VoiceState.Error -> (voiceState as VoiceState.Error).message
                        is VoiceState.PermissionRequired -> "Se requiere permiso de Micrófono"
                        VoiceState.Idle -> "En reposo"
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (voiceState is VoiceState.Error) Color(0xFFEF4444) else nexusTheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Transcribed Text Display Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SleekSurface)
                            .border(1.dp, SleekBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (liveText.isNotBlank()) {
                            Text(
                                text = "“$liveText”",
                                style = MaterialTheme.typography.bodyLarge,
                                color = SleekTextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        } else {
                            Text(
                                text = "Habla ahora... '¿Quién vive en Casa 1?' o 'Registrar paquete DHL'",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SleekTextMuted,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hands-Free Auto-Send Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SleekSurfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Modo Manos Libres (Auto-Envío)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Text(
                                text = "Envía la instrucción a Medusa IA al pausar la voz",
                                fontSize = 10.sp,
                                color = SleekTextMuted
                            )
                        }
                        Switch(
                            checked = isAutoSend,
                            onCheckedChange = { voiceManager.setHandsFreeAutoSend(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = nexusTheme.primary,
                                uncheckedThumbColor = SleekTextMuted,
                                uncheckedTrackColor = SleekSurface
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Command Chips
                    Text(
                        text = "COMANDOS DE VOZ SUGERIDOS:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextMuted,
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        voicePrompts.take(2).forEach { prompt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SleekSurface)
                                    .border(1.dp, SleekBorderSubtle, RoundedCornerShape(10.dp))
                                    .clickable {
                                        onSendMessage(prompt)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = prompt,
                                    fontSize = 10.sp,
                                    color = SleekTextSecondary,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Control Buttons (Retry / Send / Close)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (voiceManager.hasRecordAudioPermission()) {
                                    voiceManager.startListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekSurface,
                                contentColor = SleekTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reintentar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                if (liveText.isNotBlank()) {
                                    onSendMessage(liveText)
                                    onDismiss()
                                }
                            },
                            enabled = liveText.isNotBlank(),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = nexusTheme.primary,
                                contentColor = Color.White,
                                disabledContainerColor = SleekSurfaceVariant,
                                disabledContentColor = SleekTextMuted
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enviar a Medusa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated Sound Wave Equalizer Matrix reacting to dB level
 */
@Composable
fun FuturisticAudioEqualizerMatrix(
    voiceState: VoiceState,
    rmsDb: Float,
    accentColor: Color,
    glowColor: Color
) {
    val isListening = voiceState is VoiceState.Listening || voiceState is VoiceState.Transcribing
    val infiniteTransition = rememberInfiniteTransition(label = "eqAnim")

    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p1"
    )

    val normalizedDb = (rmsDb / 10f).coerceIn(0.1f, 1.5f)

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer Glow Rings
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulse1 * (1f + (normalizedDb * 0.2f)))
                    .clip(CircleShape)
                    .background(glowColor.copy(alpha = 0.15f))
            )
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .scale(pulse1)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.25f))
            )
        }

        // Center Orb
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(accentColor, SleekVioletDark)
                    )
                )
                .border(2.dp, if (isListening) glowColor else SleekBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Waveform Bars
    Row(
        modifier = Modifier.height(28.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barCount = 12
        for (i in 0 until barCount) {
            val animProgress by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400 + (i * 50), easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar$i"
            )

            val heightMultiplier = if (isListening) {
                (animProgress * normalizedDb).coerceIn(0.2f, 1.0f)
            } else {
                0.15f
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((28 * heightMultiplier).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (i % 2 == 0) accentColor else glowColor
                    )
            )
        }
    }
}

/**
 * Clean Permission Dialog explaining why microphone access is needed.
 */
@Composable
fun MicrophonePermissionExplanationDialog(
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = SleekVioletPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Permiso de Micrófono",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            }
        },
        text = {
            Text(
                text = "Sistema Medusa utiliza el micrófono para permitirte interactuar mediante comandos de voz y dictado en modo manos libres. No se graba ni comparte ningún audio externo fuera de tu dispositivo.",
                style = MaterialTheme.typography.bodyMedium,
                color = SleekTextSecondary,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekVioletPrimary,
                    contentColor = SleekVioletDark
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Permitir Micrófono", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekSurfaceVariant,
                    contentColor = SleekTextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar")
            }
        },
        containerColor = SleekSurfaceVariant,
        shape = RoundedCornerShape(20.dp)
    )
}
