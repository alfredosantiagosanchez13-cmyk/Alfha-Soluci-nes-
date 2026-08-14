package com.example.di

import com.alfredo.medusaalfha.data.local.ConversationDao
import com.alfredo.medusaalfha.data.local.MessageDao
import com.example.data.api.GeminiApiService
import com.example.data.api.GeminiRepository
import com.example.data.db.ChatMessageDao
import com.example.data.db.InteractionDao
import com.example.data.db.MemoryDao
import com.example.data.db.MemoryNodeDao
import com.example.data.db.ParcelDao
import com.example.data.repository.AiLearningContextRepository
import com.example.data.repository.AiMemoryRepository
import com.example.data.repository.FirebaseAuthRepository
import com.example.data.repository.GoogleAiGeminiRepository
import com.example.data.repository.ParcelRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para repositorios de datos, memoria persistente de IA y autenticación.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideGoogleAiGeminiRepository(): GoogleAiGeminiRepository {
        return GoogleAiGeminiRepository()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuthRepository(): FirebaseAuthRepository {
        return FirebaseAuthRepository()
    }

    @Provides
    @Singleton
    fun provideParcelRepository(parcelDao: ParcelDao): ParcelRepository {
        return ParcelRepository(parcelDao = parcelDao)
    }

    @Provides
    @Singleton
    fun provideAiMemoryRepository(
        chatMessageDao: ChatMessageDao,
        messageDao: MessageDao,
        conversationDao: ConversationDao,
        memoryDao: MemoryDao,
        interactionDao: InteractionDao,
        memoryNodeDao: MemoryNodeDao,
        geminiRepository: GeminiRepository
    ): AiMemoryRepository {
        return AiMemoryRepository(
            chatMessageDao = chatMessageDao,
            messageDao = messageDao,
            conversationDao = conversationDao,
            memoryDao = memoryDao,
            interactionDao = interactionDao,
            memoryNodeDao = memoryNodeDao,
            geminiRepository = geminiRepository
        )
    }

    @Provides
    @Singleton
    fun provideAiLearningContextRepository(
        memoryDao: MemoryDao,
        interactionDao: InteractionDao,
        memoryNodeDao: MemoryNodeDao,
        geminiRepository: GeminiRepository,
        apiService: GeminiApiService
    ): AiLearningContextRepository {
        return AiLearningContextRepository(
            memoryDao = memoryDao,
            interactionDao = interactionDao,
            memoryNodeDao = memoryNodeDao,
            geminiRepository = geminiRepository,
            apiService = apiService
        )
    }
}
