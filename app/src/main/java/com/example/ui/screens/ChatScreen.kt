package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.ui.components.FuturisticHandsFreeVoiceDialog
import com.example.ui.components.FuturisticPulsingIndicator
import com.example.ui.components.VoiceInputMicButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ChatMessageEntity
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.theme.SleekVioletContainer
import com.example.ui.theme.SleekVioletDark
import com.example.ui.theme.SleekVioletPrimary
import com.example.ui.voice.VoiceRecognitionManager
import com.example.ui.voice.VoiceState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    messages: List<ChatMessageEntity>,
    isGenerating: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    isVoiceOutputEnabled: Boolean = true,
    isSpeakingAi: Boolean = false,
    onToggleVoiceOutput: () -> Unit = {},
    onSpeakMessage: (String) -> Unit = {},
    onStopSpeaking: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val voiceManager = remember { VoiceRecognitionManager(context) }
    var showHandsFreeDialog by remember { mutableStateOf(false) }

    val liveVoiceText by voiceManager.liveTranscribedText.collectAsState()
    val voiceState by voiceManager.voiceState.collectAsState()

    // Sync live partial speech to input box when talking in inline mode
    LaunchedEffect(liveVoiceText) {
        if (liveVoiceText.isNotBlank() && !showHandsFreeDialog) {
            inputText = liveVoiceText
        }
    }

    // Scroll to bottom when messages update
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val samplePrompts = listOf(
        "¿Cuál es la esencia y normas del residencial?",
        "¿Cuáles son los horarios de amenidades y silencio?",
        "Guarda que en Casa 01 mi perrito se llama Toby",
        "¿Qué directivas y memorias tienes registradas?"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBackground)
    ) {
        // Chat subheader with controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SleekVioletPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HISTORIAL GUARDADO EN ROOM DB (${messages.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = SleekTextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Voice Output Audio Speaker Toggle
                IconButton(
                    onClick = onToggleVoiceOutput,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isVoiceOutputEnabled) SleekVioletDark else SleekSurface)
                        .border(
                            1.dp,
                            if (isVoiceOutputEnabled) SleekVioletPrimary.copy(alpha = 0.6f) else SleekBorder,
                            CircleShape
                        )
                        .semantics { testTag = "toggle_voice_output_button" }
                ) {
                    Icon(
                        imageVector = if (isVoiceOutputEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = if (isVoiceOutputEnabled) "Desactivar voz de Medusa" else "Activar voz de Medusa",
                        tint = if (isSpeakingAi) SleekVioletPrimary else if (isVoiceOutputEnabled) SleekVioletPrimary else SleekTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Hands-Free Trigger Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SleekVioletDark)
                        .border(1.dp, SleekVioletPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { showHandsFreeDialog = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Modo Manos Libres",
                            tint = SleekVioletPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Voz IA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekVioletPrimary
                        )
                    }
                }

                if (messages.isNotEmpty()) {
                    IconButton(
                        onClick = onClearChat,
                        modifier = Modifier
                            .size(32.dp)
                            .semantics { testTag = "clear_chat_button" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Limpiar Historial",
                            tint = SleekTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SleekSurfaceVariant)
                                .border(1.dp, SleekBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = SleekVioletPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Canal Neural Iniciado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tus mensajes y aprendizajes se guardan automáticamente en la base de datos Room local.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SleekTextMuted,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    message = msg,
                    onSpeak = { onSpeakMessage(msg.content) }
                )
            }

            if (isGenerating) {
                item {
                    FuturisticPulsingIndicator(
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }

        // Quick prompt chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(samplePrompts) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SleekSurface)
                        .border(1.dp, SleekBorderSubtle, RoundedCornerShape(20.dp))
                        .clickable {
                            inputText = prompt
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 11.sp,
                        color = SleekTextSecondary
                    )
                }
            }
        }

        // Input pill container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(SleekSurfaceVariant)
                    .border(1.dp, SleekBorder, RoundedCornerShape(28.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Pregunta o instruye al Núcleo Medusa...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SleekTextMuted,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            testTag = "chat_input_field"
                            contentDescription = "Campo de texto para charlar con IA"
                        },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = SleekTextPrimary,
                        fontSize = 14.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = SleekVioletPrimary
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Voice-to-Text Microphone Button
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

                IconButton(
                    onClick = {
                        val text = inputText
                        inputText = ""
                        onSendMessage(text)
                    },
                    enabled = inputText.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank() && !isGenerating) SleekVioletPrimary else SleekSurface)
                        .semantics { testTag = "send_message_button" }
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar mensaje",
                        tint = if (inputText.isNotBlank() && !isGenerating) SleekVioletDark else SleekTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Fullscreen / Modal Voice Interaction Matrix
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

@Composable
fun ChatMessageBubble(
    message: ChatMessageEntity,
    onSpeak: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == "USER"
    val formattedTime = remember(message.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SleekVioletDark)
                    .border(1.dp, SleekVioletPrimary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🪼", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
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
                    .background(if (isUser) SleekVioletContainer else SleekSurface)
                    .border(
                        1.dp,
                        if (isUser) SleekVioletPrimary.copy(alpha = 0.4f) else SleekBorder,
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isUser) 20.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 20.dp
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isUser) "TÚ" else "NÚCLEO MEDUSA AI",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUser) SleekVioletPrimary else SleekVioletPrimary.copy(alpha = 0.8f)
                        )

                        if (!isUser && onSpeak != null) {
                            IconButton(
                                onClick = onSpeak,
                                modifier = Modifier
                                    .size(20.dp)
                                    .semantics { testTag = "replay_audio_button_${message.id}" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Escuchar respuesta de Medusa",
                                    tint = SleekVioletPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
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
