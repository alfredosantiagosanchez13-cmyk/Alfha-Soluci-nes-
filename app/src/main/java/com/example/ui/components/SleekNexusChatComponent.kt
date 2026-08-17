package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.db.ChatMessageEntity
import com.example.ui.theme.LocalSleekNexusTheme
import com.example.ui.theme.NexusAccentPalette
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.voice.VoiceRecognitionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * Componente interactivo de Chat 'Sleek Nexus' conectado a GeminiRepository.
 * Cuenta con diseño futurista de burbujas asimétricas, soporte de voz,
 * sincronización con Room DB y transiciones reactivas.
 */
@Composable
fun SleekNexusChatComponent(
    messages: List<ChatMessageEntity>,
    isGenerating: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier,
    isVoiceOutputEnabled: Boolean = true,
    isSpeakingAi: Boolean = false,
    onToggleVoiceOutput: () -> Unit = {},
    onSpeakMessage: (String) -> Unit = {},
    onStopSpeaking: () -> Unit = {},
    quickPrompts: List<String> = listOf(
        "¿Cuál es la esencia y normas del residencial?",
        "¿Cuáles son los horarios de amenidades y silencio?",
        "Guarda que en Casa 01 mi perrito se llama Toby",
        "¿Qué directivas y memorias tienes registradas?"
    )
) {
    val context = LocalContext.current
    val themeConfig = LocalSleekNexusTheme.current
    val palette = themeConfig.accentPalette
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val voiceManager = remember { VoiceRecognitionManager(context) }
    var showHandsFreeDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    val liveVoiceText by voiceManager.liveTranscribedText.collectAsState()

    // Sincronizar texto capturado por voz
    LaunchedEffect(liveVoiceText) {
        if (liveVoiceText.isNotBlank() && !showHandsFreeDialog) {
            inputText = liveVoiceText
        }
    }

    // Auto-scroll al último mensaje recibido o enviado
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Sleek Nexus Neural Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Status Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Pulsing Online LED
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse_led")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.85f,
                        targetValue = 1.25f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "led_scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(palette.highlight)
                    )

                    Text(
                        text = "GEMINI 2.5 FLASH",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.highlight,
                        letterSpacing = 0.5.sp
                    )

                    // Room DB Security Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(palette.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(9.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "ROOM DB (${messages.size})",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.primary
                        )
                    }
                }

                // Right Action Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // TTS Speaker Toggle
                    IconButton(
                        onClick = onToggleVoiceOutput,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isVoiceOutputEnabled) palette.primaryContainer else SleekSurface)
                            .border(
                                1.dp,
                                if (isVoiceOutputEnabled) palette.primary.copy(alpha = 0.6f) else SleekBorder,
                                CircleShape
                            )
                            .semantics { testTag = "sleek_nexus_toggle_voice_btn" }
                    ) {
                        Icon(
                            imageVector = if (isVoiceOutputEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (isVoiceOutputEnabled) "Desactivar voz de Medusa" else "Activar voz de Medusa",
                            tint = if (isSpeakingAi) palette.highlight else if (isVoiceOutputEnabled) palette.primary else SleekTextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Hands-Free Trigger Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.primaryContainer)
                            .border(1.dp, palette.primary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable { showHandsFreeDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .semantics { testTag = "sleek_nexus_hands_free_btn" }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voz Neural",
                                tint = palette.highlight,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Voz IA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.highlight
                            )
                        }
                    }

                    // Clear Chat Button (Direct Trigger with Dialog)
                    if (messages.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier
                                .size(32.dp)
                                .semantics { testTag = "sleek_nexus_clear_chat_btn" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Limpiar Conversación",
                                tint = SleekTextMuted,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Options Menu Dropdown Trigger
                    Box {
                        IconButton(
                            onClick = { showOptionsMenu = true },
                            modifier = Modifier
                                .size(32.dp)
                                .semantics { testTag = "sleek_nexus_chat_options_menu_btn" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opciones de conversación",
                                tint = SleekTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                            modifier = Modifier
                                .background(SleekSurface)
                                .border(1.dp, SleekBorder, RoundedCornerShape(8.dp))
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Limpiar historial de chat (Room DB)",
                                        color = if (messages.isNotEmpty()) Color(0xFFFF5252) else SleekTextMuted,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = null,
                                        tint = if (messages.isNotEmpty()) Color(0xFFFF5252) else SleekTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                enabled = messages.isNotEmpty(),
                                onClick = {
                                    showOptionsMenu = false
                                    showClearConfirmDialog = true
                                },
                                modifier = Modifier.semantics { testTag = "sleek_nexus_menu_clear_chat_item" }
                            )
                        }
                    }
                }
            }

            // Messages List Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { testTag = "sleek_nexus_messages_list" },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Empty State
                    if (messages.isEmpty()) {
                        item {
                            SleekNexusEmptyChatState(palette = palette)
                        }
                    }

                    // Chat Messages with 'Sleek Nexus' Styled Bubbles
                    items(messages, key = { it.id }) { msg ->
                        SleekNexusChatBubble(
                            message = msg,
                            palette = palette,
                            onSpeak = { onSpeakMessage(msg.content) }
                        )
                    }

                    // AI Generating / Neural Pulsing Indicator
                    if (isGenerating) {
                        item {
                            SleekNexusGeneratingBubble(palette = palette)
                        }
                    }
                }

                // Floating Action Button to Clear Session Chat
                androidx.compose.animation.AnimatedVisibility(
                    visible = messages.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 12.dp)
                ) {
                    FloatingActionButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier
                            .size(42.dp)
                            .semantics {
                                testTag = "clear_chat_floating_button"
                                contentDescription = "Botón flotante para limpiar historial de chat"
                            },
                        shape = CircleShape,
                        containerColor = SleekSurfaceVariant,
                        contentColor = Color(0xFFFF5252),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Limpiar sesión",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Quick Suggestion Chips Carousel
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPrompts) { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(SleekSurface)
                            .border(1.dp, SleekBorderSubtle, RoundedCornerShape(18.dp))
                            .clickable { inputText = prompt }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .semantics { testTag = "quick_prompt_chip" }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = palette.primary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 11.sp,
                                color = SleekTextSecondary
                            )
                        }
                    }
                }
            }

            // Sleek Nexus Glowing Input Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(SleekSurfaceVariant)
                        .border(1.dp, if (inputText.isNotBlank()) palette.primary.copy(alpha = 0.6f) else SleekBorder, RoundedCornerShape(26.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Pregunta o instruye a Medusa OS...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SleekTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .semantics {
                                testTag = "sleek_nexus_chat_input_field"
                                contentDescription = "Campo de entrada para charlar con el núcleo Gemini"
                            },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = SleekTextPrimary,
                            fontSize = 14.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = palette.primary
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Voice Microphone Button
                    VoiceInputMicButton(
                        voiceManager = voiceManager,
                        onSpeechResult = { recognizedText ->
                            inputText = recognizedText
                            if (recognizedText.isNotBlank() && voiceManager.isHandsFreeAutoSendEnabled.value) {
                                inputText = ""
                                onSendMessage(recognizedText)
                            }
                        },
                        onOpenFullHandsFree = { showHandsFreeDialog = true }
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Send Button with Nexus Ripple Feedback
                    IconButton(
                        onClick = {
                            val text = inputText.trim()
                            if (text.isNotBlank() && !isGenerating) {
                                inputText = ""
                                onSendMessage(text)
                            }
                        },
                        enabled = inputText.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isGenerating) palette.primary else SleekSurface)
                            .semantics { testTag = "sleek_nexus_send_button" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar mensaje",
                            tint = if (inputText.isNotBlank() && !isGenerating) palette.primaryDark else SleekTextMuted,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }

        // Confirmation Dialog for Clearing Chat History
        if (showClearConfirmDialog) {
            SleekNexusClearChatConfirmationDialog(
                messageCount = messages.size,
                palette = palette,
                onConfirm = {
                    showClearConfirmDialog = false
                    onClearChat()
                    Toast.makeText(context, "Historial de conversación eliminado de Room DB", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showClearConfirmDialog = false }
            )
        }

        // Fullscreen Hands-Free Voice Dialog
        if (showHandsFreeDialog) {
            FuturisticHandsFreeVoiceDialog(
                voiceManager = voiceManager,
                onSendMessage = { spokenText ->
                    inputText = ""
                    onSendMessage(spokenText)
                },
                onDismiss = { showHandsFreeDialog = false }
            )
        }
    }
}

/**
 * Burbuja de mensaje con estilo 'Sleek Nexus' asimétrico y soporte para copiar y TTS.
 */
@Composable
fun SleekNexusChatBubble(
    message: ChatMessageEntity,
    palette: NexusAccentPalette,
    onSpeak: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isUser = message.sender == "USER"
    val formattedTime = remember(message.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        // AI Avatar Badge
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(palette.primaryDark)
                    .border(1.dp, palette.primary.copy(alpha = 0.5f), CircleShape)
                    .padding(1.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_medusa_logo),
                    contentDescription = "Medusa Alpha Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Bubble Container
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isUser) 20.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 20.dp
                        )
                    )
                    .background(
                        if (isUser) {
                            Brush.linearGradient(
                                listOf(palette.primaryContainer, palette.surfaceHighlight)
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(SleekSurface, SleekSurfaceVariant)
                            )
                        }
                    )
                    .border(
                        1.dp,
                        if (isUser) palette.primary.copy(alpha = 0.5f) else SleekBorder,
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isUser) 20.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 20.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    // Header inside bubble
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isUser) "TÚ" else "NÚCLEO MEDUSA AI",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUser) palette.highlight else palette.primary,
                                letterSpacing = 0.5.sp
                            )
                            if (!isUser) {
                                Spacer(modifier = Modifier.width(5.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(palette.primary.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "v4.2",
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.highlight
                                    )
                                }
                            }
                        }

                        // Bubble Actions: Copy & Speaker
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("MedusaMessage", message.content)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Mensaje copiado", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(20.dp)
                                    .semantics { testTag = "copy_message_btn_${message.id}" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiar texto",
                                    tint = SleekTextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                            }

                            if (!isUser && onSpeak != null) {
                                IconButton(
                                    onClick = onSpeak,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .semantics { testTag = "replay_audio_button_${message.id}" }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Escuchar respuesta",
                                        tint = palette.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Message Content Text
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = SleekTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Message Timestamp
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = SleekTextMuted,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

/**
 * Indicador animado 'Medusa is thinking...' con diseño 'Sleek Nexus'.
 * Muestra animación de respiración cuántica, puntos de razonamiento rebotantes
 * y fases de procesamiento neural mientras se aguarda la respuesta de Gemini API.
 */
@Composable
fun SleekNexusGeneratingBubble(
    palette: NexusAccentPalette,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MedusaThinkingAnimation")

    // Breathing pulse for avatar
    val avatarPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatarPulse"
    )

    // Glowing border oscillation
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderGlow"
    )

    // Animated bouncing dots for "thinking"
    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1Offset"
    )
    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2Offset"
    )
    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3Offset"
    )

    // Rotating cognitive status phases
    var phaseIndex by remember { mutableIntStateOf(0) }
    val phases = remember {
        listOf(
            "Medusa está pensando...",
            "Consultando Núcleo Gemini 2.5 Flash...",
            "Accediendo a memorias persistentes Room DB...",
            "Sintetizando respuesta contextual..."
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            phaseIndex = (phaseIndex + 1) % phases.size
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                testTag = "medusa_thinking_indicator"
                contentDescription = "Medusa is thinking: ${phases[phaseIndex]}"
            },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // Glowing & Pulsing Jellyfish Avatar
        Box(
            modifier = Modifier
                .size(34.dp)
                .scale(avatarPulse)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(palette.primary, palette.primaryDark)
                    )
                )
                .border(
                    1.5.dp,
                    palette.highlight.copy(alpha = borderGlow),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🪼", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Thinking Bubble Container
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 20.dp
                    )
                )
                .background(
                    Brush.linearGradient(
                        listOf(SleekSurface, SleekSurfaceVariant)
                    )
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            palette.primary.copy(alpha = borderGlow),
                            palette.highlight.copy(alpha = borderGlow * 0.7f),
                            palette.primaryDark
                        )
                    ),
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 20.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Header with "MEDUSA IS THINKING..." and Bouncing Dots
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = palette.highlight,
                        modifier = Modifier.size(13.dp)
                    )

                    Text(
                        text = "MEDUSA IS THINKING",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.highlight,
                        letterSpacing = 0.8.sp
                    )

                    // 3 Animated Glowing Bouncing Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(y = dot1Offset.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(palette.highlight)
                        )
                        Box(
                            modifier = Modifier
                                .offset(y = dot2Offset.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(palette.primary)
                        )
                        Box(
                            modifier = Modifier
                                .offset(y = dot3Offset.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(palette.glow)
                        )
                    }
                }

                // Dynamic Neural Subtitle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(palette.primary.copy(alpha = 0.8f))
                    )
                    Text(
                        text = phases[phaseIndex],
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = SleekTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Estado vacío cuando no hay mensajes aún en Room DB.
 */
@Composable
fun SleekNexusEmptyChatState(
    palette: NexusAccentPalette,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFFFD54F).copy(alpha = 0.25f), Color.Transparent)
                    )
                )
                .border(2.dp, palette.primary.copy(alpha = 0.6f), CircleShape)
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_medusa_logo),
                contentDescription = "Medusa Alpha Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "MEDUSA ALFHA · CANAL NEURAL",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFFD54F),
            letterSpacing = 1.5.sp,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "TIEMPO ", fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F), fontSize = 11.sp)
            Text(text = "= ", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
            Text(text = "FAMILIA", fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF), fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Pregunta normas condominales, reserva amenidades, reporta incidencias o instruye memorias persistentes.",
            style = MaterialTheme.typography.bodyMedium,
            color = SleekTextMuted,
            modifier = Modifier.padding(horizontal = 28.dp),
            fontSize = 11.sp,
            lineHeight = 15.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Delegación compatible para llamadas previas a ChatMessageBubble.
 */
@Composable
fun ChatMessageBubble(
    message: ChatMessageEntity,
    modifier: Modifier = Modifier,
    palette: NexusAccentPalette = LocalSleekNexusTheme.current.accentPalette,
    onSpeak: (() -> Unit)? = null
) {
    SleekNexusChatBubble(
        message = message,
        palette = palette,
        onSpeak = onSpeak,
        modifier = modifier
    )
}

/**
 * Diálogo de confirmación para eliminar el historial de la sesión del chat y de Room DB.
 */
@Composable
fun SleekNexusClearChatConfirmationDialog(
    messageCount: Int,
    palette: NexusAccentPalette,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
        containerColor = SleekSurface,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5252).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = "Limpiar Historial de Chat",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (messageCount == 1) {
                        "¿Deseas eliminar permanentemente el mensaje de esta conversación?"
                    } else {
                        "¿Deseas eliminar permanentemente los $messageCount mensajes de esta conversación?"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = SleekTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Text(
                    text = "• Se eliminarán de la pantalla y de la base de datos Room DB local.\n• Las directivas y memorias fijas del núcleo no se verán afectadas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5252),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.semantics { testTag = "confirm_clear_chat_dialog_button" }
            ) {
                Text(
                    text = "Eliminar Todo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SleekTextSecondary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, SleekBorder),
                modifier = Modifier.semantics { testTag = "cancel_clear_chat_dialog_button" }
            ) {
                Text(
                    text = "Cancelar",
                    fontSize = 12.sp
                )
            }
        }
    )
}


