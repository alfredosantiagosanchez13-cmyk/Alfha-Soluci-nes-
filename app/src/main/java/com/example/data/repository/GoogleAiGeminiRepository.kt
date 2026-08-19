package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.BuildConfig
import com.example.data.db.MemoryNodeEntity
import com.example.data.model.PackageScanResult
import com.example.data.model.ResidentDirectory
import com.example.data.security.SecureApiKeyProvider
import com.example.data.security.SecureApiKeyStorage
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio que utiliza el SDK oficial de Google AI Client (com.google.ai.client.generativeai)
 * para interactuar con los modelos Gemini y generar respuestas conversacionales contextuales,
 * utilizando la clave segura provista por [SecureApiKeyProvider] / [SecureApiKeyStorage] o .env.
 */
@Singleton
class GoogleAiGeminiRepository @Inject constructor(
    private val context: Context? = null,
    private val secureApiKeyStorage: SecureApiKeyStorage? = null
) {

    companion object {
        private const val TAG = "GoogleAiGeminiRepo"
        const val DEFAULT_MODEL = "gemini-2.5-flash"
        val FALLBACK_MODELS = listOf("gemini-2.5-flash", "gemini-flash-latest", "gemini-2.5-pro")
    }

    /**
     * Resuelve la clave de API con prioridad:
     * 1. Clave personalizada pasada en la llamada
     * 2. Clave segura provista por [SecureApiKeyProvider] desde EncryptedSharedPreferences
     * 3. Clave cifrada guardada en [SecureApiKeyStorage]
     * 4. Variable GEMINI_API_KEY del archivo .env a través de BuildConfig
     */
    fun resolveApiKey(customKey: String? = null, callContext: Context? = null): String? {
        if (!customKey.isNullOrBlank()) return customKey.trim()

        val targetContext = callContext ?: context
        if (targetContext != null) {
            val keyFromProvider = SecureApiKeyProvider.getApiKey(targetContext)
            if (!keyFromProvider.isNullOrBlank()) {
                return keyFromProvider.trim()
            }
        }

        val storedKey = secureApiKeyStorage?.getApiKey()?.trim()
        if (!storedKey.isNullOrBlank()) {
            return storedKey
        }

        val envKey = BuildConfig.GEMINI_API_KEY
        return if (envKey.isNotBlank() && envKey != "MY_GEMINI_API_KEY") envKey.trim() else null
    }

    /**
     * Construye una instancia configurada de [GenerativeModel] del SDK de Google AI Client.
     */
    fun createGenerativeModel(
        modelName: String = DEFAULT_MODEL,
        customApiKey: String? = null,
        systemInstructionText: String? = null,
        temperature: Float = 0.7f,
        topK: Int = 40,
        topP: Float = 0.95f
    ): GenerativeModel {
        val apiKey = resolveApiKey(customApiKey)
            ?: throw IllegalStateException("GEMINI_API_KEY no detectada. Configúrala en Sleek Nexus Settings o en el Secrets Panel (.env).")

        val config = generationConfig {
            this.temperature = temperature
            this.topK = topK
            this.topP = topP
        }

        val systemInstructionContent = systemInstructionText?.takeIf { it.isNotBlank() }?.let {
            content { text(it) }
        }

        return GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = config,
            systemInstruction = systemInstructionContent
        )
    }

    /**
     * Genera una respuesta conversacional sincrónica/unidireccional con contexto del sistema y memoria Room.
     * Con soporte de fallback automático de modelos ante errores de endpoint (404).
     */
    suspend fun generateConversationalResponse(
        prompt: String,
        chatHistory: List<Pair<String, String>> = emptyList(),
        memories: List<MemoryNodeEntity> = emptyList(),
        userRoleLabel: String = "Santiago (Alfha)",
        customApiKey: String? = null,
        modelName: String = DEFAULT_MODEL
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(customApiKey)
            ?: return@withContext Result.failure(
                IllegalStateException("Clave API de Gemini no detectada. Configúrala de forma cifrada en Sleek Nexus Settings o en el Secrets Panel (.env).")
            )

        val systemInstruction = buildSystemPrompt(userRoleLabel, memories)
        val formattedHistory = mapHistoryToContents(chatHistory)
        val modelsToTry = (listOf(modelName) + FALLBACK_MODELS).distinct()
        var lastError: Exception? = null

        for (candidateModel in modelsToTry) {
            try {
                val model = GenerativeModel(
                    modelName = candidateModel,
                    apiKey = apiKey,
                    generationConfig = generationConfig {
                        temperature = 0.7f
                        topK = 40
                        topP = 0.95f
                    },
                    systemInstruction = systemInstruction.takeIf { it.isNotBlank() }?.let { content { text(it) } }
                )

                val chat: Chat = model.startChat(history = formattedHistory)
                val response = chat.sendMessage(prompt)
                val responseText = response.text ?: ""

                if (responseText.isNotBlank()) {
                    return@withContext Result.success(responseText)
                }
            } catch (e: Exception) {
                lastError = e
                Log.w(TAG, "Intento con modelo Google AI '$candidateModel' falló, evaluando fallback...", e)
            }
        }

        Log.e(TAG, "Error generando respuesta conversacional con Google AI SDK en todos los modelos candidatos", lastError)
        Result.failure(lastError ?: IllegalStateException("La respuesta generada por Gemini está vacía."))
    }

    /**
     * Transmite la respuesta conversacional en tiempo real token por token mediante Kotlin Flow.
     */
    fun streamConversationalResponse(
        prompt: String,
        chatHistory: List<Pair<String, String>> = emptyList(),
        memories: List<MemoryNodeEntity> = emptyList(),
        userRoleLabel: String = "Santiago (Alfha)",
        customApiKey: String? = null,
        modelName: String = DEFAULT_MODEL
    ): Flow<String> {
        val systemInstruction = buildSystemPrompt(userRoleLabel, memories)
        val model = createGenerativeModel(
            modelName = modelName,
            customApiKey = customApiKey,
            systemInstructionText = systemInstruction
        )

        val formattedHistory = mapHistoryToContents(chatHistory)
        val chat: Chat = model.startChat(history = formattedHistory)

        return chat.sendMessageStream(prompt)
            .map { response -> response.text ?: "" }
            .catch { e ->
                Log.e(TAG, "Error en stream conversacional con Google AI SDK", e)
                throw e
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Analiza una imagen de paquete o acceso utilizando visión multimodal del SDK de Google AI.
     */
    suspend fun analyzeMultimodalImage(
        bitmap: Bitmap,
        prompt: String,
        customApiKey: String? = null,
        modelName: String = DEFAULT_MODEL
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val model = createGenerativeModel(
                modelName = modelName,
                customApiKey = customApiKey
            )

            val inputContent = content {
                image(bitmap)
                text(prompt)
            }

            val response = model.generateContent(inputContent)
            val text = response.text ?: ""
            Result.success(text)
        } catch (e: Exception) {
            Log.e(TAG, "Error en análisis multimodal con Google AI SDK", e)
            Result.failure(e)
        }
    }

    /**
     * Extrae un nodo de memoria estructurado a partir del diálogo para persistencia en Room DB.
     */
    suspend fun extractMemoryNode(
        userMessage: String,
        aiResponse: String,
        customApiKey: String? = null
    ): MemoryNodeEntity? = withContext(Dispatchers.IO) {
        try {
            val containsKeywords = userMessage.lowercase().run {
                contains("me llamo") || contains("mi nombre") || contains("me gusta") ||
                contains("prefiero") || contains("recuerda") || contains("guarda") ||
                contains("mi correo") || contains("mi proyecto") || contains("mi clave") ||
                contains("regla") || contains("importante") || contains("siempre") ||
                contains("casa") || contains("departamento") || contains("depa") || contains("torre") ||
                contains("vecino") || contains("mascota") || contains("perro") || contains("gato") ||
                contains("paquete") || contains("visita") || contains("alberca") || contains("asador") ||
                contains("pádel") || contains("ruido") || contains("estacionamiento") || contains("auto") ||
                contains("horario") || contains("caseta") || contains("amenidad") || contains("condominio")
            }

            if (!containsKeywords && userMessage.length < 15) return@withContext null

            val extractionPrompt = """
                Analiza el mensaje del usuario y determina si reveló alguna preferencia, instrucción, nombre, regla vecinal, dato de amenidades, mascotas o dato persistente para que la IA crezca con el condominio.
                Mensaje Usuario: "$userMessage"
                Respuesta IA: "$aiResponse"
                
                Si NO hay un dato persistente claro, responde únicamente: NONE
                Si SI hay un dato persistente, responde estrictamente con este formato JSON:
                {"category":"COMMUNITY|AMENITY|PREFERENCE|DIRECTIVE|SECURITY|FACT", "title":"Título corto de 2 a 4 palabras", "detail":"Detalle concreto memorizado en 1 oración clara"}
            """.trimIndent()

            val model = createGenerativeModel(
                modelName = DEFAULT_MODEL,
                customApiKey = customApiKey
            )

            val response = model.generateContent(extractionPrompt)
            val textResult = response.text?.trim() ?: return@withContext null

            if (textResult.contains("NONE") || !textResult.contains("{")) return@withContext null

            val startIdx = textResult.indexOf("{")
            val endIdx = textResult.lastIndexOf("}")
            if (startIdx != -1 && endIdx > startIdx) {
                val jsonSub = textResult.substring(startIdx, endIdx + 1)
                val nodeObj = JSONObject(jsonSub)
                val category = nodeObj.optString("category", "FACT")
                val title = nodeObj.optString("title", "Dato Recordado")
                val detail = nodeObj.optString("detail", userMessage)

                return@withContext MemoryNodeEntity(
                    category = category,
                    title = title,
                    detail = detail,
                    confidenceScore = 0.96f,
                    timestamp = System.currentTimeMillis(),
                    isUserAdded = false
                )
            }
            null
        } catch (e: Exception) {
            Log.d(TAG, "No se extrajo nodo de memoria: ${e.message}")
            null
        }
    }

    /**
     * Construye la instrucción de sistema del Núcleo Medusa inyectando memorias persistidas.
     */
    private fun buildSystemPrompt(userRoleLabel: String, memories: List<MemoryNodeEntity>): String {
        val memoryContext = if (memories.isNotEmpty()) {
            "SISTEMA MEDUSA - MEMORIAS ALMACENADAS EN ROOM DB:\n" + memories.joinToString("\n") { node ->
                "• [${node.category}] ${node.title}: ${node.detail} (Confianza: ${(node.confidenceScore * 100).toInt()}%)"
            }
        } else {
            "SISTEMA MEDUSA - MEMORIA LARGO PLAZO: Vacía. Aprende y registra detalles clave del usuario conforme converse."
        }

        return """
            Eres el Núcleo de Inteligencia Artificial de Sistema Medusa OS (v4.2 Neural Condominium Core).
            Tu propósito primordial es ser el alma, consejero y cerebro operativo del Condominio Residencial, creciendo y evolucionando continuamente con la comunidad para adoptar y preservar su verdadera esencia, tranquilidad, seguridad y armonía vecinal.
            
            ROL Y NIVEL DE AUTORIZACIÓN ACTUAL DEL USUARIO: $userRoleLabel
            
            $memoryContext
            
            PRINCIPIOS DE EVOLUCIÓN Y ESENCIA CONDOMINAL:
            1. IDENTIDAD Y CALIDEZ: Trata a los residentes con calidez, respeto y personalización, recordando sus preferencias de hogar, paquetería y visitas.
            2. ARMONÍA Y CONVIVENCIA: Promueve activamente el respeto a los horarios de silencio (22:00 - 08:00 hrs), la tenencia responsable de mascotas, la sustentabilidad y el cuidado de amenidades (Alberca, Casa Club, Canchas, Áreas Verdes).
            3. DELIMITACIÓN POR ROLES:
               • Si interactúas con "Santiago (Alfha)", reconócelo explícitamente como el Alpha / Root Commander y Administrador Principal de Medusa OS con autorización suprema.
               • Si interactúas con "Residente", apóyalo en su vida diaria, reservación de amenidades, consulta de paquetería y generación de pases QR para sus visitas y servicios.
               • Si interactúas con "Guardia", apóyalo con rapidez operativa en caseta de acceso, validación de códigos QR, paquetería y reporte de incidencias.
               • Si interactúas con "Administración", asístelo en auditorías de acceso, estado de la memoria comunitaria Room DB, avisos a residentes y métricas de seguridad.
            4. EVOLUCIÓN CONTINUA: Aprende de cada acuerdo, regla y hábito expresado por los vecinos para que el condominio sea cada día más seguro, organizado y acogedor.
            5. TONO: Claro, directo, elegante, profesional, empático y futurista en español.
        """.trimIndent()
    }

    /**
     * Mapea el historial de chat (Sender, Text) al formato [Content] requerido por el SDK de Google AI Client.
     */
    private fun mapHistoryToContents(history: List<Pair<String, String>>): List<Content> {
        val recentHistory = history.takeLast(14)
        return recentHistory.map { (sender, messageText) ->
            val role = if (sender.equals("USER", ignoreCase = true) || sender.equals("user", ignoreCase = true)) {
                "user"
            } else {
                "model"
            }
            content(role = role) {
                text(messageText)
            }
        }
    }
}
