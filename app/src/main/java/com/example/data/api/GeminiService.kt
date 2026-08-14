package com.example.data.api

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Servicio Gemini impulsado por la librería oficial Firebase AI (`firebase-ai`).
 *
 * Proporciona métodos para:
 * - Enviar mensajes individuales y generar respuestas completas
 * - Manejar streaming reactivo de respuestas en tiempo real (Flow<String>)
 * - Iniciar y gestionar sesiones de chat multi-turno con historial contextual
 * - Soporte para instrucciones de sistema y contenido multimodal (imágenes Bitmap)
 */
class GeminiService(
    val modelName: String = DEFAULT_MODEL
) {
    companion object {
        private const val TAG = "GeminiService"
        const val DEFAULT_MODEL = "gemini-2.5-flash"
    }

    /**
     * Construye una instancia configurada de [GenerativeModel].
     */
    fun createGenerativeModel(
        systemInstruction: String? = null,
        temperature: Float = 0.7f,
        topK: Int = 40,
        topP: Float = 0.95f
    ): GenerativeModel {
        val config = generationConfig {
            this.temperature = temperature
            this.topK = topK
            this.topP = topP
        }

        return if (!systemInstruction.isNullOrBlank()) {
            Firebase.ai.generativeModel(
                modelName = modelName,
                systemInstruction = content { text(systemInstruction) },
                generationConfig = config
            )
        } else {
            Firebase.ai.generativeModel(
                modelName = modelName,
                generationConfig = config
            )
        }
    }

    /**
     * Envía un mensaje con o sin historial y genera la respuesta completa (no streaming).
     */
    suspend fun sendMessage(
        prompt: String,
        history: List<Pair<String, String>> = emptyList(),
        systemInstruction: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val model = createGenerativeModel(systemInstruction)

            val responseText = if (history.isEmpty()) {
                val response = model.generateContent(prompt)
                response.text ?: ""
            } else {
                val chat = model.startChat(history = mapHistoryToContents(history))
                val response = chat.sendMessage(prompt)
                response.text ?: ""
            }

            Result.success(responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Error in sendMessage with Firebase AI", e)
            Result.failure(e)
        }
    }

    /**
     * Envía un mensaje y transmite el flujo continuo de fragmentos (tokens)
     * a medida que el modelo genera la respuesta en tiempo real.
     */
    fun streamMessage(
        prompt: String,
        history: List<Pair<String, String>> = emptyList(),
        systemInstruction: String? = null
    ): Flow<String> {
        val model = createGenerativeModel(systemInstruction)

        val streamFlow = if (history.isEmpty()) {
            model.generateContentStream(prompt)
        } else {
            val chat = model.startChat(history = mapHistoryToContents(history))
            chat.sendMessageStream(prompt)
        }

        return streamFlow
            .map { response -> response.text ?: "" }
            .catch { e ->
                Log.e(TAG, "Error in streamMessage with Firebase AI", e)
                throw e
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Envía un mensaje multimodal con una imagen Bitmap y obtiene streaming en tiempo real.
     */
    fun streamMultimodalMessage(
        prompt: String,
        image: Bitmap,
        systemInstruction: String? = null
    ): Flow<String> {
        val model = createGenerativeModel(systemInstruction)
        val content = content {
            image(image)
            text(prompt)
        }

        return model.generateContentStream(content)
            .map { response -> response.text ?: "" }
            .catch { e ->
                Log.e(TAG, "Error in streamMultimodalMessage with Firebase AI", e)
                throw e
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Inicia una sesión de chat interactiva [Chat] con historial previo.
     */
    fun startChatSession(
        history: List<Pair<String, String>> = emptyList(),
        systemInstruction: String? = null
    ): Chat {
        val model = createGenerativeModel(systemInstruction)
        return model.startChat(history = mapHistoryToContents(history))
    }

    /**
     * Convierte una lista de pares (Sender, Content) a la lista de [Content] del SDK Firebase AI.
     */
    private fun mapHistoryToContents(history: List<Pair<String, String>>): List<Content> {
        return history.map { (sender, text) ->
            val role = if (sender.equals("USER", ignoreCase = true) || sender.equals("user", ignoreCase = true)) {
                "user"
            } else {
                "model"
            }
            content(role = role) {
                text(text)
            }
        }
    }
}
