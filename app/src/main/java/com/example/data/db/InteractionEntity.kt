package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interaction_logs")
data class InteractionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userPrompt: String,
    val aiResponse: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sentiment: String = "NEUTRAL", // POSITIVE, NEGATIVE, URGENT, INSTRUCTIVE
    val contextScope: String = "GENERAL", // PARCEL, CHAT, CORE_MATRIX, MEMORY_VAULT
    val importanceScore: Float = 0.5f,
    val extractedMemoryId: Long? = null
)
