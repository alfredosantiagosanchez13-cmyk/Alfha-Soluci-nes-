package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM continuous_memories ORDER BY isPinned DESC, importance DESC, lastAccessedTimestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM continuous_memories WHERE id = :id")
    suspend fun getMemoryById(id: Long): MemoryEntity?

    @Query("SELECT * FROM continuous_memories WHERE category = :category ORDER BY importance DESC")
    fun getMemoriesByCategory(category: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM continuous_memories WHERE `key` LIKE '%' || :query || '%' OR `value` LIKE '%' || :query || '%' ORDER BY importance DESC")
    fun searchMemories(query: String): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Query("UPDATE continuous_memories SET lastAccessedTimestamp = :timestamp, accessCount = accessCount + 1 WHERE id = :id")
    suspend fun updateAccessStats(id: Long, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)

    @Query("DELETE FROM continuous_memories WHERE lastAccessedTimestamp < :cutoffTimestamp AND isPinned = 0")
    suspend fun deleteMemoriesOlderThan(cutoffTimestamp: Long): Int

    @Query("DELETE FROM continuous_memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM continuous_memories")
    suspend fun clearAll()
}
