package com.example.ui.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class VoiceState {
    object Idle : VoiceState()
    object Initializing : VoiceState()
    data class Listening(val rmsDb: Float = 0f) : VoiceState()
    data class Transcribing(val partialText: String) : VoiceState()
    data class Success(val finalResult: String, val confidence: Float = 1.0f) : VoiceState()
    data class Error(val message: String) : VoiceState()
    object PermissionRequired : VoiceState()
}

class VoiceRecognitionManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    
    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _liveTranscribedText = MutableStateFlow("")
    val liveTranscribedText: StateFlow<String> = _liveTranscribedText.asStateFlow()

    private val _isHandsFreeAutoSendEnabled = MutableStateFlow(true)
    val isHandsFreeAutoSendEnabled: StateFlow<Boolean> = _isHandsFreeAutoSendEnabled.asStateFlow()

    var onAutoSendCallback: ((String) -> Unit)? = null

    fun setHandsFreeAutoSend(enabled: Boolean) {
        _isHandsFreeAutoSendEnabled.value = enabled
    }

    fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startListening(languageCode: String = "es-MX") {
        if (!hasRecordAudioPermission()) {
            _voiceState.value = VoiceState.PermissionRequired
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceState.value = VoiceState.Error("Reconocimiento de voz no disponible en este dispositivo.")
            return
        }

        stopListening()

        try {
            _voiceState.value = VoiceState.Initializing
            _liveTranscribedText.value = ""

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _voiceState.value = VoiceState.Listening(0f)
                    }

                    override fun onBeginningOfSpeech() {
                        _voiceState.value = VoiceState.Listening(2f)
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        if (_voiceState.value is VoiceState.Listening || _voiceState.value is VoiceState.Transcribing) {
                            _voiceState.value = VoiceState.Listening(rmsdB.coerceAtLeast(0f))
                        }
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _voiceState.value = VoiceState.Transcribing(_liveTranscribedText.value)
                    }

                    override fun onError(error: Int) {
                        val errorMessage = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Error de grabación de audio."
                            SpeechRecognizer.ERROR_CLIENT -> "Error del cliente de voz."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permiso de micrófono denegado."
                            SpeechRecognizer.ERROR_NETWORK -> "Error de red al procesar voz."
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tiempo de espera agotado de red."
                            SpeechRecognizer.ERROR_NO_MATCH -> "No se reconoció ninguna frase. Intenta hablar más claro."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "El motor de voz está ocupado."
                            SpeechRecognizer.ERROR_SERVER -> "Error en el servidor de reconocimiento."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó sonido. Habla cerca del micrófono."
                            else -> "Error en reconocimiento de voz ($error)."
                        }
                        _voiceState.value = VoiceState.Error(errorMessage)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        if (text.isNotBlank()) {
                            _liveTranscribedText.value = text
                            _voiceState.value = VoiceState.Success(text)
                            if (_isHandsFreeAutoSendEnabled.value) {
                                onAutoSendCallback?.invoke(text)
                            }
                        } else {
                            _voiceState.value = VoiceState.Error("No se capturó texto claro.")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull() ?: ""
                        if (partial.isNotBlank()) {
                            _liveTranscribedText.value = partial
                            _voiceState.value = VoiceState.Transcribing(partial)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _voiceState.value = VoiceState.Error("No se pudo iniciar el micrófono: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            // Ignore teardown errors
        }
    }

    fun resetState() {
        stopListening()
        _voiceState.value = VoiceState.Idle
        _liveTranscribedText.value = ""
    }
}
