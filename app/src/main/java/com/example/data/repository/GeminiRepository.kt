package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.BuildConfig
import com.example.data.api.ContentDto
import com.example.data.api.GeminiApiService
import com.example.data.api.GeminiRequest
import com.example.data.api.GeminiResponse
import com.example.data.api.InlineDataDto
import com.example.data.api.PartDto
import com.example.data.api.RetrofitClient
import com.example.data.api.SystemInstructionDto
import com.example.data.db.MemoryNodeEntity
import com.example.data.model.PackageScanResult
import com.example.data.model.ResidentDirectory
import com.example.data.security.SecureApiKeyProvider
import com.example.data.security.SecureApiKeyStorage
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de Google AI / Gemini API para Sistema Medusa OS.
 *
 * Utiliza [SecureApiKeyProvider] y [SecureApiKeyStorage] para recuperar de forma segura
 * la GEMINI_API_KEY almacenada en EncryptedSharedPreferences (AES-256 GCM / AES-256 SIV),
 * ejecutando consultas y procesando respuestas de forma 100% asíncrona mediante Kotlin Coroutines
 * (Dispatchers.IO) y Reactive Streams (Flow).
 */
@Singleton
class GeminiRepository @Inject constructor(
    private val context: Context? = null,
    private val secureApiKeyStorage: SecureApiKeyStorage? = null,
    private val apiService: GeminiApiService = RetrofitClient.geminiApi
) {

    companion object {
        private const val TAG = "GeminiRepository"
        const val DEFAULT_MODEL = "gemini-2.5-flash"
    }

    /**
     * Resuelve la clave de API con orden de prioridad de seguridad y sin hardcoding:
     * 1. Clave personalizada explícita (si se suministra en la llamada)
     * 2. Clave segura provista por [SecureApiKeyProvider] desde EncryptedSharedPreferences
     * 3. Clave cifrada guardada en [SecureApiKeyStorage]
     * 4. Variable de compilación BuildConfig.GEMINI_API_KEY (inyectada desde .env / Secrets)
     */
    fun resolveApiKey(customApiKey: String? = null, callContext: Context? = null): String? {
        if (!customApiKey.isNullOrBlank()) {
            return customApiKey.trim()
        }

        // Recuperar dinámicamente desde SecureApiKeyProvider usando EncryptedSharedPreferences
        val targetContext = callContext ?: context
        if (targetContext != null) {
            val providerKey = SecureApiKeyProvider.getApiKey(targetContext)
            if (!providerKey.isNullOrBlank()) {
                return providerKey.trim()
            }
        }

        val encryptedKey = secureApiKeyStorage?.getApiKey()?.trim()
        if (!encryptedKey.isNullOrBlank()) {
            return encryptedKey
        }

        val envKey = BuildConfig.GEMINI_API_KEY.trim()
        return if (envKey.isNotBlank() && envKey != "MY_GEMINI_API_KEY") envKey else null
    }

    private suspend fun executeWithModelFallback(
        apiKey: String,
        request: GeminiRequest
    ): GeminiResponse {
        val candidateModels = listOf("gemini-2.5-flash", "gemini-flash-latest", "gemini-2.5-pro")
        var lastError: Exception? = null

        for (model in candidateModels) {
            try {
                return apiService.generateContent(model = model, apiKey = apiKey, request = request)
            } catch (e: Exception) {
                lastError = e
                Log.w(TAG, "Llamada a modelo $model falló, probando alternativa...", e)
            }
        }
        throw (lastError ?: Exception("No se pudo obtener respuesta de los modelos de Gemini."))
    }

    /**
     * Consulta asíncrona principal para el chat neural contextual con memorias Room DB.
     */
    suspend fun generateResponse(
        prompt: String,
        chatHistory: List<Pair<String, String>> = emptyList(),
        memories: List<MemoryNodeEntity> = emptyList(),
        customApiKey: String? = null,
        userRoleLabel: String = "Santiago (Alfha)"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = resolveApiKey(customApiKey)
                ?: return@withContext Result.failure(
                    IllegalStateException("Clave API de Gemini no detectada. Configúrala de forma cifrada en Sleek Nexus Settings o en el Secrets Panel (.env).")
                )

            val systemInstructionText = buildSystemInstruction(userRoleLabel, memories)

            // Usar llamada asíncrona optimizada
            val contents = mutableListOf<ContentDto>()

            val recentHistory = chatHistory.takeLast(12)
            for ((sender, messageText) in recentHistory) {
                val role = if (sender.equals("USER", ignoreCase = true) || sender.equals("user", ignoreCase = true)) "user" else "model"
                contents.add(
                    ContentDto(
                        role = role,
                        parts = listOf(PartDto(text = messageText))
                    )
                )
            }

            contents.add(
                ContentDto(
                    role = "user",
                    parts = listOf(PartDto(text = prompt))
                )
            )

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = SystemInstructionDto(
                    parts = listOf(PartDto(text = systemInstructionText))
                )
            )

            val response = executeWithModelFallback(apiKey, request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!replyText.isNullOrBlank()) {
                Result.success(replyText)
            } else {
                Result.failure(Exception("Respuesta vacía de Google AI."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en consulta asíncrona a Google AI", e)
            Result.failure(e)
        }
    }

    /**
     * Consulta asíncrona genérica de generación de texto.
     */
    suspend fun generateContent(
        prompt: String,
        systemInstruction: String? = null,
        customApiKey: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = resolveApiKey(customApiKey)
                ?: return@withContext Result.failure(
                    IllegalStateException("GEMINI_API_KEY no disponible.")
                )

            val request = GeminiRequest(
                contents = listOf(
                    ContentDto(
                        role = "user",
                        parts = listOf(PartDto(text = prompt))
                    )
                ),
                systemInstruction = systemInstruction?.let {
                    SystemInstructionDto(parts = listOf(PartDto(text = it)))
                }
            )

            val response = executeWithModelFallback(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(IllegalStateException("Respuesta vacía de Gemini."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en generateContent asíncrono", e)
            Result.failure(e)
        }
    }

    /**
     * Emite la respuesta de forma asíncrona en tiempo real mediante un Flow reactivo.
     */
    fun streamResponse(
        prompt: String,
        customApiKey: String? = null,
        userRoleLabel: String = "Santiago (Alfha)"
    ): Flow<String> = flow {
        val apiKey = resolveApiKey(customApiKey)
            ?: throw IllegalStateException("GEMINI_API_KEY no configurada.")

        val model = GenerativeModel(
            modelName = DEFAULT_MODEL,
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
            }
        )

        model.generateContentStream(prompt).collect { chunk ->
            chunk.text?.let { emit(it) }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Análisis multimodal asíncrono para imágenes de paquetería y credenciales.
     */
    suspend fun analyzePackageImage(
        base64ImageClean: String,
        customApiKey: String? = null
    ): Result<PackageScanResult> = withContext(Dispatchers.IO) {
        try {
            val key = resolveApiKey(customApiKey)
                ?: return@withContext Result.failure(
                    IllegalStateException("Configura tu API Key de Gemini cifrada en el dispositivo para el escáner.")
                )

            val promptText = """
                Analiza detenidamente la etiqueta o paquete mostrado en la imagen.
                Extrae la información relevante y responde UNICAMENTE con un objeto JSON válido con esta estructura exacta:
                {
                  "houseNumber": "Número de casa, departamento, lote o unidad (ej: Casa 21, 14, Depto 302). Si no aparece pon ''",
                  "recipientName": "Nombre y apellidos del destinatario en la etiqueta. Si no aparece pon ''",
                  "carrier": "Empresa de mensajería detectada (ej: Amazon, Mercado Libre, FedEx, DHL, Estafeta, UPS). Si no se distingue pon 'Paquetería'",
                  "description": "Descripción breve del paquete (ej: Caja mediana de cartón Amazon, Sobre amarillo con logo MercadoLibre, Bolsa plástica)"
                }
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    ContentDto(
                        role = "user",
                        parts = listOf(
                            PartDto(text = promptText),
                            PartDto(inlineData = InlineDataDto(mimeType = "image/jpeg", data = base64ImageClean))
                        )
                    )
                )
            )

            val response = executeWithModelFallback(key, request)
            val textResult = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

            val startIdx = textResult.indexOf("{")
            val endIdx = textResult.lastIndexOf("}")

            if (startIdx != -1 && endIdx > startIdx) {
                val jsonSub = textResult.substring(startIdx, endIdx + 1)
                val obj = JSONObject(jsonSub)

                val rawHouse = obj.optString("houseNumber", "")
                val recipient = obj.optString("recipientName", "")
                val carrier = obj.optString("carrier", "Paquetería")
                val desc = obj.optString("description", "Paquete escaneado con IA")

                val contactByHouse = ResidentDirectory.findContactByHouse(rawHouse)
                val contactByName = if (contactByHouse == null && recipient.isNotBlank()) {
                    ResidentDirectory.findContactByName(recipient)
                } else null

                val matchedContact = contactByHouse ?: contactByName

                val finalResult = PackageScanResult(
                    houseNumber = if (rawHouse.isNotBlank()) rawHouse else (matchedContact?.let { "Casa ${it.houseNumber}" } ?: "Casa sin identificar"),
                    recipientName = if (recipient.isNotBlank()) recipient else (matchedContact?.name ?: "Destinatario no visible"),
                    carrier = carrier,
                    description = desc,
                    matchedResidentName = matchedContact?.name ?: "",
                    matchedPhone = matchedContact?.phone ?: ""
                )

                return@withContext Result.success(finalResult)
            }

            Result.failure(Exception("No se detectó información legible en la etiqueta."))
        } catch (e: Exception) {
            Log.e(TAG, "Error analizando paquete con IA", e)
            Result.failure(e)
        }
    }

    /**
     * Análisis de imagen con Bitmap utilizando Google AI SDK de forma asíncrona.
     */
    suspend fun analyzeMultimodalBitmap(
        bitmap: Bitmap,
        prompt: String,
        customApiKey: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val key = resolveApiKey(customApiKey)
                ?: return@withContext Result.failure(
                    IllegalStateException("GEMINI_API_KEY no disponible.")
                )

            val model = GenerativeModel(
                modelName = DEFAULT_MODEL,
                apiKey = key
            )

            val input = content {
                image(bitmap)
                text(prompt)
            }

            val response = model.generateContent(input)
            val text = response.text ?: ""
            Result.success(text)
        } catch (e: Exception) {
            Log.e(TAG, "Error en análisis multimodal de Bitmap", e)
            Result.failure(e)
        }
    }

    /**
     * Extrae información persistente del diálogo para memorización continua en Room DB de forma asíncrona.
     */
    suspend fun extractMemoryNode(
        userMessage: String,
        aiResponse: String,
        customApiKey: String? = null
    ): MemoryNodeEntity? = withContext(Dispatchers.IO) {
        try {
            val key = resolveApiKey(customApiKey) ?: return@withContext null

            val containsPersonalKeywords = userMessage.lowercase().run {
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

            if (!containsPersonalKeywords && userMessage.length < 15) return@withContext null

            val promptText = """
                Analiza el mensaje del usuario y determina si reveló alguna preferencia, instrucción, nombre, regla vecinal, dato de amenidades, mascotas o dato persistente para que la IA crezca con el condominio.
                Mensaje Usuario: "$userMessage"
                Respuesta IA: "$aiResponse"
                
                Si NO hay un dato persistente claro, responde únicamente: NONE
                Si SI hay un dato persistente, responde estrictamente con este formato JSON:
                {"category":"COMMUNITY|AMENITY|PREFERENCE|DIRECTIVE|SECURITY|FACT", "title":"Título corto de 2 a 4 palabras", "detail":"Detalle concreto memorizado en 1 oración clara"}
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    ContentDto(
                        role = "user",
                        parts = listOf(PartDto(text = promptText))
                    )
                )
            )

            val response = executeWithModelFallback(key, request)
            val textResult = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: return@withContext null

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
            Log.d(TAG, "No se extrajo memoria: ${e.message}")
            null
        }
    }

    private fun buildSystemInstruction(userRoleLabel: String, memories: List<MemoryNodeEntity>): String {
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
}
