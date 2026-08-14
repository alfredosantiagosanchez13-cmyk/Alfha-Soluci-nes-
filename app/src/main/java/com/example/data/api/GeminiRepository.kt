package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.db.MemoryNodeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GeminiRepository(
    private val apiService: GeminiApiService = RetrofitClient.geminiApi
) {

    private fun getApiKey(customApiKey: String?): String? {
        return customApiKey?.takeIf { it.isNotBlank() }
            ?: BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }
    }

    suspend fun generateResponse(
        prompt: String,
        chatHistory: List<Pair<String, String>>, // Pair(sender, message)
        memories: List<MemoryNodeEntity>,
        customApiKey: String? = null,
        userRoleLabel: String = "Santiago (Alfha)"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey(customApiKey)
                ?: return@withContext Result.failure(
                    IllegalStateException("Clave API de Gemini no detectada. Configura la API Key en AI Studio Secrets (.env) o en la vista de ajustes.")
                )

            val memoryContext = if (memories.isNotEmpty()) {
                "SISTEMA MEDUSA - MEMORIAS ALMACENADAS EN ROOM DB:\n" + memories.joinToString("\n") { node ->
                    "• [${node.category}] ${node.title}: ${node.detail} (Confianza: ${(node.confidenceScore * 100).toInt()}%)"
                }
            } else {
                "SISTEMA MEDUSA - MEMORIA LARGO PLAZO: Vacía. Aprende y registra detalles clave del usuario conforme converse."
            }

            val systemInstructionText = """
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

            val contents = mutableListOf<ContentDto>()

            val recentHistory = chatHistory.takeLast(10)
            for ((sender, messageText) in recentHistory) {
                val role = if (sender == "USER") "user" else "model"
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

            val response = apiService.generateContent(apiKey, request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!replyText.isNullOrBlank()) {
                Result.success(replyText)
            } else {
                Result.failure(Exception("Formato de respuesta no reconocido por el sistema Medusa."))
            }
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Exception generating response via Retrofit", e)
            Result.failure(e)
        }
    }

    suspend fun extractMemoryNode(
        userMessage: String,
        aiResponse: String,
        apiKey: String?
    ): MemoryNodeEntity? = withContext(Dispatchers.IO) {
        try {
            val key = getApiKey(apiKey) ?: return@withContext null

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

            val response = apiService.generateContent(key, request)
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
            Log.d("GeminiRepository", "No memory extracted via Retrofit: ${e.message}")
            null
        }
    }

    suspend fun analyzePackageImage(
        base64ImageClean: String,
        customApiKey: String?
    ): Result<com.example.data.model.PackageScanResult> = withContext(Dispatchers.IO) {
        try {
            val key = getApiKey(customApiKey)
                ?: return@withContext Result.failure(
                    IllegalStateException("Configura tu API Key de Gemini en AI Studio Secrets (.env) para el escáner.")
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

            val response = apiService.generateContent(key, request)
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

                val contactByHouse = com.example.data.model.ResidentDirectory.findContactByHouse(rawHouse)
                val contactByName = if (contactByHouse == null && recipient.isNotBlank()) {
                    com.example.data.model.ResidentDirectory.findContactByName(recipient)
                } else null

                val matchedContact = contactByHouse ?: contactByName

                val finalResult = com.example.data.model.PackageScanResult(
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
            Log.e("GeminiRepository", "Error analyzing package image via Retrofit", e)
            Result.failure(e)
        }
    }
}
