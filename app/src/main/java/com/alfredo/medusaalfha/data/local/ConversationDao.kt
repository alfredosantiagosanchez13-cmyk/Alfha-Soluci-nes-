package com.alfredo.medusaalfha.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversations ORDER BY is_pinned DESC, last_active_timestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE is_archived = 0 ORDER BY is_pinned DESC, last_active_timestamp DESC")
    fun getActiveConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET last_active_timestamp = :timestamp, total_messages_count = total_messages_count + :increment WHERE id = :id")
    suspend fun updateActivity(id: String, timestamp: Long = System.currentTimeMillis(), increment: Int = 1)

    @Query("UPDATE conversations SET learning_summary = :summary WHERE id = :id")
    suspend fun updateLearningSummary(id: String, summary: String)

    @Query("UPDATE conversations SET is_pinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: String, isPinned: Boolean)

    @Query("UPDATE conversations SET is_archived = :isArchived WHERE id = :id")
    suspend fun setArchived(id: String, isArchived: Boolean)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: String): Int

    @Query("DELETE FROM conversations WHERE is_archived = 1 AND is_pinned = 0 AND last_active_timestamp < :cutoffTimestamp")
    suspend fun pruneArchivedConversations(cutoffTimestamp: Long): Int

    @Query("DELETE FROM conversations")
    suspend fun clearAll()
}
