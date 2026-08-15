package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room Entity representing an AI interaction memory entry.
 * Stores the user prompt, the AI generated response, and the timestamp.
 */
@Entity(tableName = "memory_entries")
data class MemoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userPrompt: String,
    val aiResponse: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface MemoryEntryDao {
    @Query("SELECT * FROM memory_entries ORDER BY timestamp DESC")
    fun getAllMemoryEntries(): Flow<List<MemoryEntry>>

    @Query("SELECT * FROM memory_entries WHERE id = :id LIMIT 1")
    suspend fun getMemoryEntryById(id: Long): MemoryEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemoryEntry(entry: MemoryEntry): Long

    @Delete
    suspend fun deleteMemoryEntry(entry: MemoryEntry)

    @Query("DELETE FROM memory_entries WHERE id = :id")
    suspend fun deleteMemoryEntryById(id: Long)

    @Query("DELETE FROM memory_entries")
    suspend fun clearAllMemoryEntries()
}
