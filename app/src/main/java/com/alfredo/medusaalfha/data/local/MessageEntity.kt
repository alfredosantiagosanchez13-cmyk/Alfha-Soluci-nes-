package com.alfredo.medusaalfha.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad de Room para estructurar cada mensaje individual del historial de chat.
 * Contiene metadatos detallados de aprendizaje, sentimiento, importancia cognitiva y timestamps.
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversation_id"]),
        Index(value = ["timestamp"]),
        Index(value = ["sender"]),
        Index(value = ["is_memory_extracted"]),
        Index(value = ["importance_score"])
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "conversation_id")
    val conversationId: String = "default_session",

    @ColumnInfo(name = "sender")
    val sender: String, // "USER", "MEDUSA", "SYSTEM"

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "tokens_used")
    val tokensUsed: Int = 0,

    @ColumnInfo(name = "sentiment")
    val sentiment: String = "NEUTRAL", // "POSITIVE", "NEGATIVE", "NEUTRAL", "URGENT", "DIRECTIVE"

    @ColumnInfo(name = "importance_score")
    val importanceScore: Float = 0.5f, // Escala 0.0 a 1.0 para priorización de memoria

    @ColumnInfo(name = "is_memory_extracted")
    val isMemoryExtracted: Boolean = false,

    @ColumnInfo(name = "extracted_memory_category")
    val extractedMemoryCategory: String? = null, // "PREFERENCE", "RULE", "FACT", "CREDENTIAL"

    @ColumnInfo(name = "extracted_memory_detail")
    val extractedMemoryDetail: String? = null,

    @ColumnInfo(name = "model_used")
    val modelUsed: String = "gemini-2.5-flash"
)
