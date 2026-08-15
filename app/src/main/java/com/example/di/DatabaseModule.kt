package com.example.di

import android.content.Context
import com.alfredo.medusaalfha.data.local.ConversationDao
import com.alfredo.medusaalfha.data.local.MessageDao
import com.example.data.db.AccessLogDao
import com.example.data.db.AccessPassDao
import com.example.data.db.ChatMessageDao
import com.example.data.db.InteractionDao
import com.example.data.db.MedusaDatabase
import com.example.data.db.MemoryDao
import com.example.data.db.MemoryNodeDao
import com.example.data.db.ParcelDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt que provee la instancia cifrada de Room Database (MedusaDatabase)
 * y todos sus DAOs para inyección de dependencias.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMedusaDatabase(
        context: Context
    ): MedusaDatabase {
        return MedusaDatabase.getDatabase(context)
    }

    @Provides
    fun provideChatMessageDao(db: MedusaDatabase): ChatMessageDao {
        return db.chatMessageDao()
    }

    @Provides
    fun provideMemoryNodeDao(db: MedusaDatabase): MemoryNodeDao {
        return db.memoryNodeDao()
    }

    @Provides
    fun provideParcelDao(db: MedusaDatabase): ParcelDao {
        return db.parcelDao()
    }

    @Provides
    fun provideAccessPassDao(db: MedusaDatabase): AccessPassDao {
        return db.accessPassDao()
    }

    @Provides
    fun provideAccessLogDao(db: MedusaDatabase): AccessLogDao {
        return db.accessLogDao()
    }

    @Provides
    fun provideConversationDao(db: MedusaDatabase): ConversationDao {
        return db.conversationDao()
    }

    @Provides
    fun provideMessageDao(db: MedusaDatabase): MessageDao {
        return db.messageDao()
    }

    @Provides
    fun provideMemoryDao(db: MedusaDatabase): MemoryDao {
        return db.memoryDao()
    }

    @Provides
    fun provideInteractionDao(db: MedusaDatabase): InteractionDao {
        return db.interactionDao()
    }

    @Provides
    fun provideSmartDeviceDao(db: MedusaDatabase): com.example.data.db.SmartDeviceDao {
        return db.smartDeviceDao()
    }
}
