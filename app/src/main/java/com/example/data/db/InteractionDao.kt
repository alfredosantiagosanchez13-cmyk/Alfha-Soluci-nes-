package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InteractionDao {
    @Query("SELECT * FROM interaction_logs ORDER BY timestamp DESC")
    fun getAllInteractions(): Flow<List<InteractionEntity>>

    @Query("SELECT * FROM interaction_logs WHERE id = :id")
    suspend fun getInteractionById(id: Long): InteractionEntity?

    @Query("SELECT * FROM interaction_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentInteractions(limit: Int): Flow<List<InteractionEntity>>

    @Query("SELECT * FROM interaction_logs WHERE contextScope = :scope ORDER BY timestamp DESC")
    fun getInteractionsByScope(scope: String): Flow<List<InteractionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: InteractionEntity): Long

    @Delete
    suspend fun deleteInteraction(interaction: InteractionEntity)

    @Query("DELETE FROM interaction_logs WHERE id = :id")
    suspend fun deleteInteractionById(id: Long)

    @Query("DELETE FROM interaction_logs WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteInteractionsOlderThan(cutoffTimestamp: Long): Int

    @Query("DELETE FROM interaction_logs")
    suspend fun clearAll()
}
