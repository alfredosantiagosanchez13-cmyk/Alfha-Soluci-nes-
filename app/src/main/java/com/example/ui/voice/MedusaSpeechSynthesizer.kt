package com.example.ui.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Text-to-Speech (TTS) engine for Medusa AI voice responses.
 * Speaks AI responses in natural Spanish with controls for volume, pitch, speed, and mute toggle.
 */
class MedusaSpeechSynthesizer(private val context: Context) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isVoiceOutputEnabled = MutableStateFlow(true)
    val isVoiceOutputEnabled: StateFlow<Boolean> = _isVoiceOutputEnabled.asStateFlow()

    private val _currentSpokenText = MutableStateFlow<String?>(null)
    val currentSpokenText: StateFlow<String?> = _currentSpokenText.asStateFlow()

    init {
        try {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("MedusaSpeech", "Failed to construct TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val tts = textToSpeech ?: return
            // Try es-MX first, then es-ES, then default locale
            val locSpanishMx = Locale("es", "MX")
            val locSpanishEs = Locale("es", "ES")
            val locSpanish = Locale("es")

            val langResult = when {
                tts.isLanguageAvailable(locSpanishMx) >= TextToSpeech.LANG_AVAILABLE -> tts.setLanguage(locSpanishMx)
                tts.isLanguageAvailable(locSpanishEs) >= TextToSpeech.LANG_AVAILABLE -> tts.setLanguage(locSpanishEs)
                tts.isLanguageAvailable(locSpanish) >= TextToSpeech.LANG_AVAILABLE -> tts.setLanguage(locSpanish)
                else -> tts.setLanguage(Locale.getDefault())
            }

            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("MedusaSpeech", "Spanish TTS not directly supported, using default locale: ${tts.language}")
            }

            // Tune vocal cadence to sound like a sleek, confident, intelligent AI assistant
            tts.setPitch(1.05f)
            tts.setSpeechRate(1.02f)

            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentSpokenText.value = null
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentSpokenText.value = null
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    _currentSpokenText.value = null
                    Log.w("MedusaSpeech", "TTS Utterance error code: $errorCode")
                }
            })

            isInitialized = true
            Log.d("MedusaSpeech", "MedusaSpeechSynthesizer ready in language: ${tts.language}")
        } else {
            Log.e("MedusaSpeech", "TextToSpeech init failed with status: $status")
        }
    }

    fun setVoiceOutputEnabled(enabled: Boolean) {
        _isVoiceOutputEnabled.value = enabled
        if (!enabled) {
            stopSpeaking()
        }
    }

    fun toggleVoiceOutput(): Boolean {
        val newState = !_isVoiceOutputEnabled.value
        setVoiceOutputEnabled(newState)
        return newState
    }

    /**
     * Clean markdown formatting, emojis, bullets and technical artifacts so the TTS speaks naturally.
     */
    private fun sanitizeTextForSpeech(rawText: String): String {
        return rawText
            // Remove markdown code blocks
            .replace(Regex("```[\\s\\S]*?```"), " [bloque de código omitido] ")
            // Remove inline code ticks
            .replace("`", "")
            // Remove bold/italic asterisks and underscores
            .replace(Regex("\\*\\*|\\*|__|_"), "")
            // Clean markdown bullet markers
            .replace(Regex("^[\\s]*[-•*][\\s]+", RegexOption.MULTILINE), "")
            // Clean headers #
            .replace(Regex("^[\\s]*#{1,6}[\\s]+", RegexOption.MULTILINE), "")
            // Clean URLs
            .replace(Regex("https?://\\S+"), "enlace web")
            // Clean multiple whitespace / newlines into conversational pauses
            .replace(Regex("\\n+"), ". ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (!_isVoiceOutputEnabled.value) return
        val clean = sanitizeTextForSpeech(text)
        if (clean.isBlank()) return

        _currentSpokenText.value = clean

        if (!isInitialized || textToSpeech == null) {
            Log.w("MedusaSpeech", "TTS not fully initialized yet, retrying speech after brief delay")
            try {
                textToSpeech?.speak(clean, queueMode, null, "medusa_${System.currentTimeMillis()}")
            } catch (e: Exception) {
                Log.e("MedusaSpeech", "Failed to speak", e)
            }
            return
        }

        try {
            val utteranceId = "medusa_tts_${System.currentTimeMillis()}"
            textToSpeech?.speak(clean, queueMode, null, utteranceId)
        } catch (e: Exception) {
            Log.e("MedusaSpeech", "Error in speak()", e)
        }
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
            _isSpeaking.value = false
            _currentSpokenText.value = null
        } catch (e: Exception) {
            // Ignore stop errors
        }
    }

    fun shutdown() {
        try {
            stopSpeaking()
            textToSpeech?.shutdown()
            textToSpeech = null
            isInitialized = false
        } catch (e: Exception) {
            // Ignore shutdown errors
        }
    }
}
