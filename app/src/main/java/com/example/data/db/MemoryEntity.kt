package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "continuous_memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val value: String,
    val category: String = "GENERAL", // PREFERENCE, DIRECTIVE, SECURITY, FACT, LOGISTICS
    val importance: Int = 3, // 1 (Low) to 5 (Critical)
    val sourceInteractionId: Long? = null,
    val lastAccessedTimestamp: Long = System.currentTimeMillis(),
    val accessCount: Int = 1,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
