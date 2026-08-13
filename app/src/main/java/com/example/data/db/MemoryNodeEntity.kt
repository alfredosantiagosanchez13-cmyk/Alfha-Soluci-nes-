package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_nodes")
data class MemoryNodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "PREFERENCE", "DIRECTIVE", "SECURITY", "FACT"
    val title: String,
    val detail: String,
    val confidenceScore: Float = 0.95f,
    val timestamp: Long = System.currentTimeMillis(),
    val isUserAdded: Boolean = false
)
