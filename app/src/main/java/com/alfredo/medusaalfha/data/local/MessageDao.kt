package com.alfredo.medusaalfha.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMessagesForConversation(conversationId: String, limit: Int): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessagesList(conversationId: String, limit: Int): List<MessageEntity>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getAllRecentMessagesList(limit: Int): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(query: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE is_memory_extracted = 0 AND sender = 'USER' ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getUnprocessedUserMessages(limit: Int = 10): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>): List<Long>

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET is_memory_extracted = 1, extracted_memory_category = :category, extracted_memory_detail = :detail WHERE id = :id")
    suspend fun markMemoryExtracted(id: Long, category: String, detail: String)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long): Int

    @Query("DELETE FROM messages WHERE conversation_id = :conversationId")
    suspend fun deleteMessagesByConversation(conversationId: String): Int

    @Query("DELETE FROM messages WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteMessagesOlderThan(cutoffTimestamp: Long): Int

    @Query("DELETE FROM messages WHERE conversation_id = :conversationId AND id NOT IN (SELECT id FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp DESC LIMIT :keepCount)")
    suspend fun pruneOldestMessagesInConversation(conversationId: String, keepCount: Int): Int

    @Query("SELECT COUNT(*) FROM messages WHERE conversation_id = :conversationId")
    fun getMessageCountForConversation(conversationId: String): Flow<Int>

    @Query("DELETE FROM messages")
    suspend fun clearAll()
}
