package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryNodeDao {
    @Query("SELECT * FROM memory_nodes ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryNodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(node: MemoryNodeEntity): Long

    @Delete
    suspend fun deleteMemory(node: MemoryNodeEntity)

    @Query("DELETE FROM memory_nodes WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM memory_nodes WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteMemoriesOlderThan(cutoffTimestamp: Long): Int

    @Query("DELETE FROM memory_nodes")
    suspend fun clearAllMemories()

    @Query("SELECT COUNT(*) FROM memory_nodes")
    fun getMemoryCount(): Flow<Int>

    @Query("SELECT * FROM memory_nodes WHERE title LIKE '%' || :query || '%' OR detail LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMemories(query: String): Flow<List<MemoryNodeEntity>>
}
