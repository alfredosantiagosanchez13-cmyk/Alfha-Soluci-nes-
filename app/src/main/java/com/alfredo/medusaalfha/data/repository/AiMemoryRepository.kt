package com.alfredo.medusaalfha.data.repository

import android.util.Log
import com.alfredo.medusaalfha.data.local.ConversationDao
import com.alfredo.medusaalfha.data.local.ConversationEntity
import com.alfredo.medusaalfha.data.local.MessageDao
import com.alfredo.medusaalfha.data.local.MessageEntity
import com.example.data.api.ContentDto
import com.example.data.api.GeminiRepository
import com.example.data.api.PartDto
import com.example.data.db.ChatMessageDao
import com.example.data.db.ChatMessageEntity
import com.example.data.db.InteractionDao
import com.example.data.db.InteractionEntity
import com.example.data.db.MemoryDao
import com.example.data.db.MemoryEntity
import com.example.data.db.MemoryNodeDao
import com.example.data.db.MemoryNodeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * AiMemoryRepository acts as the single source of truth connecting
 * Firebase AI / Gemini models to the local Room database, providing clean methods
 * to save, query, and prune conversation history and contextual learning metadata.
 */
class AiMemoryRepository(
    private val chatMessageDao: ChatMessageDao,
    private val messageDao: MessageDao? = null,
    private val conversationDao: ConversationDao? = null,
    private val memoryDao: MemoryDao? = null,
    private val interactionDao: InteractionDao? = null,
    private val memoryNodeDao: MemoryNodeDao? = null,
    private val geminiRepository: GeminiRepository = GeminiRepository()
) {

    companion object {
        private const val TAG = "AiMemoryRepository"
        const val SENDER_USER = "USER"
        const val SENDER_AI = "MEDUSA"
        const val DEFAULT_CONVERSATION_ID = "default_session"
        const val DEFAULT_MAX_HISTORY_MESSAGES = 100
    }

    // ==========================================
    // 1. CONVERSATION MANAGEMENT & OBSERVABILITY
    // ==========================================

    /**
     * Observable stream of all active conversation threads.
     */
    val activeConversationsFlow: Flow<List<ConversationEntity>>? =
        conversationDao?.getActiveConversations()

    /**
     * Observable stream of all conversation messages in chronological order.
     */
    val conversationFlow: Flow<List<ChatMessageEntity>> = chatMessageDao.getAllMessages()

    /**
     * Observable stream of structured [MessageEntity] for a specific conversation session.
     */
    fun getMessagesForConversation(conversationId: String = DEFAULT_CONVERSATION_ID): Flow<List<MessageEntity>>? {
        return messageDao?.getMessagesForConversation(conversationId)
    }

    /**
     * Creates or retrieves a conversation session thread.
     */
    suspend fun getOrCreateConversation(
        conversationId: String = DEFAULT_CONVERSATION_ID,
        title: String = "Sesión Medusa IA",
        topicCategory: String = "GENERAL"
    ): ConversationEntity = withContext(Dispatchers.IO) {
        val existing = conversationDao?.getConversationById(conversationId)
        if (existing != null) {
            existing
        } else {
            val newConversation = ConversationEntity(
                id = conversationId,
                title = title,
                topicCategory = topicCategory,
                createdAt = System.currentTimeMillis(),
                lastActiveTimestamp = System.currentTimeMillis()
            )
            conversationDao?.insertConversation(newConversation)
            newConversation
        }
    }

    // ==========================================
    // 2. SAVING MESSAGES & LEARNING METADATA
    // ==========================================

    /**
     * Saves a user message with sentiment and importance metadata.
     */
    suspend fun saveUserMessage(
        content: String,
        conversationId: String = DEFAULT_CONVERSATION_ID
    ): Long = withContext(Dispatchers.IO) {
        val sentiment = evaluateSentiment(content)
        val importance = if (content.length > 50 || sentiment == "ALERT" || sentiment == "DIRECTIVE") 0.85f else 0.5f

        // Ensure parent conversation exists
        getOrCreateConversation(conversationId)

        val legacyId = chatMessageDao.insertMessage(
            ChatMessageEntity(
                sender = SENDER_USER,
                content = content.trim(),
                timestamp = System.currentTimeMillis()
            )
        )

        messageDao?.insertMessage(
            MessageEntity(
                conversationId = conversationId,
                sender = SENDER_USER,
                content = content.trim(),
                timestamp = System.currentTimeMillis(),
                sentiment = sentiment,
                importanceScore = importance
            )
        )

        conversationDao?.updateActivity(conversationId, System.currentTimeMillis(), 1)
        legacyId
    }

    /**
     * Saves an AI response message with learning tags and token/model metadata.
     */
    suspend fun saveAiMessage(
        content: String,
        conversationId: String = DEFAULT_CONVERSATION_ID,
        modelUsed: String = "gemini-2.5-flash",
        extractedMemoryCategory: String? = null,
        extractedMemoryDetail: String? = null
    ): Long = withContext(Dispatchers.IO) {
        // Ensure parent conversation exists
        getOrCreateConversation(conversationId)

        val legacyId = chatMessageDao.insertMessage(
            ChatMessageEntity(
                sender = SENDER_AI,
                content = content.trim(),
                timestamp = System.currentTimeMillis()
            )
        )

        messageDao?.insertMessage(
            MessageEntity(
                conversationId = conversationId,
                sender = SENDER_AI,
                content = content.trim(),
                timestamp = System.currentTimeMillis(),
                sentiment = "NEUTRAL",
                importanceScore = 0.6f,
                modelUsed = modelUsed,
                isMemoryExtracted = extractedMemoryCategory != null,
                extractedMemoryCategory = extractedMemoryCategory,
                extractedMemoryDetail = extractedMemoryDetail
            )
        )

        conversationDao?.updateActivity(conversationId, System.currentTimeMillis(), 1)
        legacyId
    }

    /**
     * Atomically records a full conversation turn, extracts cognitive nodes,
     * and updates conversation summaries.
     */
    suspend fun recordConversationTurn(
        userPrompt: String,
        aiResponse: String,
        conversationId: String = DEFAULT_CONVERSATION_ID,
        contextScope: String = "CONVERSATION",
        customApiKey: String? = null
    ): Pair<Long, Long> = withContext(Dispatchers.IO) {
        // Extract cognitive memory
        var extractedCategory: String? = null
        var extractedDetail: String? = null

        try {
            val node = geminiRepository.extractMemoryNode(userPrompt, aiResponse, customApiKey)
            if (node != null) {
                extractedCategory = node.category
                extractedDetail = node.detail
                memoryNodeDao?.insertMemory(node)
                memoryDao?.insertMemory(
                    MemoryEntity(
                        key = node.title,
                        value = node.detail,
                        category = node.category,
                        importance = (node.confidenceScore * 5).toInt().coerceIn(1, 5),
                        createdAt = System.currentTimeMillis()
                    )
                )
                Log.d(TAG, "Learning node persisted: [${node.category}] ${node.title}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Learning extraction skipped", e)
        }

        val userMsgId = saveUserMessage(userPrompt, conversationId)
        val aiMsgId = saveAiMessage(
            content = aiResponse,
            conversationId = conversationId,
            extractedMemoryCategory = extractedCategory,
            extractedMemoryDetail = extractedDetail
        )

        // Save interaction telemetry
        interactionDao?.let { dao ->
            try {
                dao.insertInteraction(
                    InteractionEntity(
                        userPrompt = userPrompt,
                        aiResponse = aiResponse,
                        timestamp = System.currentTimeMillis(),
                        sentiment = evaluateSentiment(userPrompt),
                        contextScope = contextScope,
                        importanceScore = if (userPrompt.length > 60) 0.8f else 0.5f
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Non-critical: could not log interaction", e)
            }
        }

        Pair(userMsgId, aiMsgId)
    }

    // ==========================================
    // 3. QUERYING FOR FIREBASE AI / GEMINI SDK
    // ==========================================

    /**
     * Retrieves recent conversation turns as (User, AI) pairs for chat sessions.
     */
    suspend fun getFormattedChatHistory(
        conversationId: String = DEFAULT_CONVERSATION_ID,
        limit: Int = 20
    ): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        val messages = if (messageDao != null) {
            val list = messageDao.getRecentMessagesList(conversationId, limit * 2)
            list.reversed().map { Pair(it.sender, it.content) }
        } else {
            val list = chatMessageDao.getRecentMessagesList(limit * 2)
            list.reversed().map { Pair(it.sender, it.content) }
        }

        val pairs = mutableListOf<Pair<String, String>>()
        var pendingUser: String? = null

        for ((sender, content) in messages) {
            if (sender == SENDER_USER) {
                pendingUser = content
            } else if (sender == SENDER_AI && pendingUser != null) {
                pairs.add(Pair(pendingUser, content))
                pendingUser = null
            }
        }
        pairs
    }

    /**
     * Retrieves recent messages as [ContentDto] objects ready for Firebase AI payloads.
     */
    suspend fun getAiContentHistory(
        conversationId: String = DEFAULT_CONVERSATION_ID,
        limit: Int = 20
    ): List<ContentDto> = withContext(Dispatchers.IO) {
        val messages = if (messageDao != null) {
            val list = messageDao.getRecentMessagesList(conversationId, limit)
            list.reversed().map { Pair(it.sender, it.content) }
        } else {
            val list = chatMessageDao.getRecentMessagesList(limit)
            list.reversed().map { Pair(it.sender, it.content) }
        }

        messages.map { (sender, content) ->
            ContentDto(
                role = if (sender == SENDER_USER) "user" else "model",
                parts = listOf(PartDto(text = content))
            )
        }
    }

    // ==========================================
    // 4. PRUNING & RETENTION POLICIES
    // ==========================================

    /**
     * Prunes conversation history, keeping only the most recent [keepCount] messages.
     */
    suspend fun pruneHistoryToCount(
        conversationId: String = DEFAULT_CONVERSATION_ID,
        keepCount: Int = DEFAULT_MAX_HISTORY_MESSAGES
    ): Int = withContext(Dispatchers.IO) {
        val deletedMessages = messageDao?.pruneOldestMessagesInConversation(conversationId, keepCount) ?: 0
        val deletedLegacy = chatMessageDao.pruneOldestMessages(keepCount)
        Log.d(TAG, "Pruned oldest messages, kept last $keepCount (messages: $deletedMessages, legacy: $deletedLegacy)")
        deletedMessages + deletedLegacy
    }

    /**
     * Prunes messages older than a given number of days.
     */
    suspend fun pruneHistoryOlderThan(days: Int = 30): Int = withContext(Dispatchers.IO) {
        val cutoff = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L)
        val deletedMessages = messageDao?.deleteMessagesOlderThan(cutoff) ?: 0
        val deletedLegacy = chatMessageDao.deleteMessagesOlderThan(cutoff)
        conversationDao?.pruneArchivedConversations(cutoff)
        Log.d(TAG, "Pruned messages older than $days days. Removed: ${deletedMessages + deletedLegacy} rows.")
        deletedMessages + deletedLegacy
    }

    /**
     * Clears all conversation history from the Room database.
     */
    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        chatMessageDao.clearAllMessages()
        messageDao?.clearAll()
        conversationDao?.clearAll()
        Log.i(TAG, "All conversation history and sessions cleared from Room.")
    }

    private fun evaluateSentiment(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("urgente") || lower.contains("alerta") || lower.contains("seguridad") -> "ALERT"
            lower.contains("recuerda") || lower.contains("guarda") || lower.contains("aprende") -> "DIRECTIVE"
            lower.contains("gracias") || lower.contains("bien") || lower.contains("perfecto") -> "POSITIVE"
            lower.contains("error") || lower.contains("falla") || lower.contains("mal") -> "NEGATIVE"
            else -> "NEUTRAL"
        }
    }
}
