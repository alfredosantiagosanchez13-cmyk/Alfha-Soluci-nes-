package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.db.ChatMessageEntity
import com.example.ui.components.ChatMessageBubble
import com.example.ui.components.SleekNexusChatBubble
import com.example.ui.components.SleekNexusChatComponent

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
    SleekNexusChatComponent(
        messages = messages,
        isGenerating = isGenerating,
        onSendMessage = onSendMessage,
        onClearChat = onClearChat,
        isVoiceOutputEnabled = isVoiceOutputEnabled,
        isSpeakingAi = isSpeakingAi,
        onToggleVoiceOutput = onToggleVoiceOutput,
        onSpeakMessage = onSpeakMessage,
        onStopSpeaking = onStopSpeaking,
        modifier = modifier
    )
}

