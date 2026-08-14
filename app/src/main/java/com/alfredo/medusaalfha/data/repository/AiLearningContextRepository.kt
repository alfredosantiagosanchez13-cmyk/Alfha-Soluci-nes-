package com.alfredo.medusaalfha.data.repository

import android.util.Log
import com.example.data.api.GeminiApiService
import com.example.data.api.GeminiRepository
import com.example.data.api.RetrofitClient
import com.example.data.db.InteractionDao
import com.example.data.db.InteractionEntity
import com.example.data.db.MemoryDao
import com.example.data.db.MemoryEntity
import com.example.data.db.MemoryNodeDao
import com.example.data.db.MemoryNodeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

/**
 * Repositorio de servicios para conectar Firebase Genkit / Firebase AI Logic
 * y el motor de IA generativa con Room Database.
 *
 * Permite que la IA guarde, consulte, clasifique y recupere contextos históricos
 * de aprendizaje y memoria continua a largo plazo.
 */
class AiLearningContextRepository(
    private val memoryDao: MemoryDao,
    private val interactionDao: InteractionDao,
    private val memoryNodeDao: MemoryNodeDao? = null,
    private val geminiRepository: GeminiRepository = GeminiRepository(),
    private val apiService: GeminiApiService = RetrofitClient.geminiApi
) {

    companion object {
        private const val TAG = "AiLearningContextRepo"
    }

    /**
     * Obtiene el flujo reactivo de todas las memorias y contextos de aprendizaje almacenados en Room.
     */
    val allLearningContexts: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    /**
     * Obtiene el flujo reactivo de interacciones históricas recientes.
     */
    fun getRecentInteractions(limit: Int = 20): Flow<List<InteractionEntity>> {
        return interactionDao.getRecentInteractions(limit)
    }

    /**
     * Guarda explícitamente un contexto o directiva de aprendizaje en Room.
     */
    suspend fun saveLearningContext(
        key: String,
        value: String,
        category: String = "GENERAL",
        importance: Int = 3,
        isPinned: Boolean = false,
        sourceInteractionId: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        val memory = MemoryEntity(
            key = key.trim(),
            value = value.trim(),
            category = category.uppercase(),
            importance = importance.coerceIn(1, 5),
            sourceInteractionId = sourceInteractionId,
            lastAccessedTimestamp = System.currentTimeMillis(),
            accessCount = 1,
            isPinned = isPinned,
            createdAt = System.currentTimeMillis()
        )
        val memoryId = memoryDao.insertMemory(memory)

        // Sincronizar también con MemoryNodeDao si está presente
        memoryNodeDao?.insertMemory(
            MemoryNodeEntity(
                category = category.uppercase(),
                title = key.trim(),
                detail = value.trim(),
                confidenceScore = (importance.toFloat() / 5f).coerceAtLeast(0.7f),
                timestamp = System.currentTimeMillis(),
                isUserAdded = true
            )
        )

        Log.d(TAG, "Contexto de aprendizaje guardado en Room con ID $memoryId: [$category] $key -> $value")
        memoryId
    }

    /**
     * Recupera y ordena los contextos históricos relevantes en función de la consulta actual,
     * priorizando memorias fijadas (isPinned), importancia y coincidencia por palabras clave.
     */
    suspend fun retrieveRelevantHistoricalContext(
        query: String? = null,
        limit: Int = 10
    ): List<MemoryEntity> = withContext(Dispatchers.IO) {
        try {
            val allMemories = if (!query.isNullOrBlank()) {
                val filtered = memoryDao.searchMemories(query.trim()).firstOrNull() ?: emptyList()
                if (filtered.isNotEmpty()) {
                    filtered
                } else {
                    memoryDao.getAllMemories().firstOrNull() ?: emptyList()
                }
            } else {
                memoryDao.getAllMemories().firstOrNull() ?: emptyList()
            }

            val topMemories = allMemories.take(limit)

            // Actualizar estadísticas de acceso para las memorias recuperadas
            topMemories.forEach { mem ->
                memoryDao.updateAccessStats(mem.id)
            }

            topMemories
        } catch (e: Exception) {
            Log.e(TAG, "Error recuperando contexto histórico de Room", e)
            emptyList()
        }
    }

    /**
     * Construye un bloque de instrucciones de sistema enriquecido con el contexto
     * histórico de aprendizaje recuperado desde Room Database.
     */
    suspend fun buildContextualSystemPrompt(
        baseInstruction: String,
        currentQuery: String,
        userRoleLabel: String = "Santiago (Alfha)"
    ): String = withContext(Dispatchers.IO) {
        val historicalContexts = retrieveRelevantHistoricalContext(currentQuery, limit = 8)

        val memoryBlock = if (historicalContexts.isNotEmpty()) {
            val formattedMemories = historicalContexts.joinToString("\n") { m ->
                val pinBadge = if (m.isPinned) "[📌 FIJADO] " else ""
                "• $pinBadge[${m.category}] ${m.key}: ${m.value} (Nivel Importancia: ${m.importance}/5)"
            }
            """
            === CONTEXTO HISTÓRICO Y APRENDIZAJE PERSISTENTE (ROOM ENCRYPTED DB) ===
            $formattedMemories
            ========================================================================
            Usa el contexto anterior para personalizar tu respuesta y recordar acuerdos o directivas previas.
            """.trimIndent()
        } else {
            "=== CONTEXTO HISTÓRICO: Base de conocimiento local vacía o sin coincidencias específicas ==="
        }

        """
        $baseInstruction
        
        AUTORIZACIÓN / ROL: $userRoleLabel
        
        $memoryBlock
        """.trimIndent()
    }

    /**
     * Ejecuta una generación de IA conectando la llamada con el historial y extrayendo
     * aprendizajes para persistirlos de forma continua en Room.
     */
    suspend fun generateWithHistoricalLearning(
        prompt: String,
        chatHistory: List<Pair<String, String>> = emptyList(),
        userRoleLabel: String = "Santiago (Alfha)",
        contextScope: String = "NEURAL_CHAT",
        customApiKey: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val nodes = memoryNodeDao?.getAllMemories()?.firstOrNull() ?: emptyList()

            // 1. Generar respuesta utilizando el repositorio neural
            val result = geminiRepository.generateResponse(
                prompt = prompt,
                chatHistory = chatHistory,
                memories = nodes,
                customApiKey = customApiKey,
                userRoleLabel = userRoleLabel
            )

            if (result.isSuccess) {
                val aiResponse = result.getOrNull().orEmpty()

                // 2. Registrar la interacción en Room
                val interaction = InteractionEntity(
                    userPrompt = prompt,
                    aiResponse = aiResponse,
                    timestamp = System.currentTimeMillis(),
                    sentiment = analyzeSentiment(prompt),
                    contextScope = contextScope,
                    importanceScore = if (prompt.length > 50) 0.8f else 0.5f
                )
                val interactionId = interactionDao.insertInteraction(interaction)

                // 3. Evaluar y extraer nuevos aprendizajes automáticamente
                val extractedNode = geminiRepository.extractMemoryNode(prompt, aiResponse, customApiKey)
                if (extractedNode != null) {
                    saveLearningContext(
                        key = extractedNode.title,
                        value = extractedNode.detail,
                        category = extractedNode.category,
                        importance = 4,
                        sourceInteractionId = interactionId
                    )
                }

                Result.success(aiResponse)
            } else {
                result
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en generateWithHistoricalLearning", e)
            Result.failure(e)
        }
    }

    /**
     * Fija o desfija un contexto de aprendizaje para evitar que sea eliminado en podas.
     */
    suspend fun setPinned(id: Long, isPinned: Boolean) = withContext(Dispatchers.IO) {
        val memory = memoryDao.getMemoryById(id)
        if (memory != null) {
            memoryDao.updateMemory(memory.copy(isPinned = isPinned))
        }
    }

    /**
     * Elimina un contexto de aprendizaje por su ID.
     */
    suspend fun deleteLearningContext(id: Long) = withContext(Dispatchers.IO) {
        memoryDao.deleteMemoryById(id)
        memoryNodeDao?.deleteMemoryById(id)
    }

    /**
     * Limpia o poda registros antiguos no fijados que superen una cantidad de días.
     */
    suspend fun pruneOldContexts(daysOld: Int = 30): Int = withContext(Dispatchers.IO) {
        val cutoff = System.currentTimeMillis() - (daysOld * 24L * 60L * 60L * 1000L)
        val deleted = memoryDao.deleteMemoriesOlderThan(cutoff)
        interactionDao.deleteInteractionsOlderThan(cutoff)
        memoryNodeDao?.deleteMemoriesOlderThan(cutoff)
        deleted
    }

    /**
     * Limpia completamente las tablas de memoria e interacción.
     */
    suspend fun clearAll() = withContext(Dispatchers.IO) {
        memoryDao.clearAll()
        interactionDao.clearAll()
        memoryNodeDao?.clearAllMemories()
    }

    private fun analyzeSentiment(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("urgente") || lower.contains("alerta") || lower.contains("peligro") -> "URGENT"
            lower.contains("recuerda") || lower.contains("guarda") || lower.contains("instruccion") -> "INSTRUCTIVE"
            lower.contains("gracias") || lower.contains("excelente") || lower.contains("bien") -> "POSITIVE"
            lower.contains("error") || lower.contains("mal") || lower.contains("falla") -> "NEGATIVE"
            else -> "NEUTRAL"
        }
    }
}
