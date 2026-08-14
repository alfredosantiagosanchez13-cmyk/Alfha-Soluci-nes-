package com.alfredo.medusaalfha.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad de Room para agrupar y gestionar hilos de conversación con la IA.
 * Almacena metadatos de aprendizaje contextual, resúmenes automáticos y marcas temporales.
 */
@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["last_active_timestamp"]),
        Index(value = ["topic_category"]),
        Index(value = ["is_pinned"])
    ]
)
data class ConversationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "title")
    val title: String = "Nueva Sesión",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_active_timestamp")
    val lastActiveTimestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "topic_category")
    val topicCategory: String = "GENERAL", // SECURITY, DIRECTIVE, LOGISTICS, GENERAL

    @ColumnInfo(name = "total_messages_count")
    val totalMessagesCount: Int = 0,

    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false,

    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,

    @ColumnInfo(name = "learning_summary")
    val learningSummary: String? = null // Síntesis de aprendizaje contextual extraído de la sesión
)
