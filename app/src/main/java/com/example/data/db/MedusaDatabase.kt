package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alfredo.medusaalfha.data.local.ConversationDao
import com.alfredo.medusaalfha.data.local.ConversationEntity
import com.alfredo.medusaalfha.data.local.MessageDao
import com.alfredo.medusaalfha.data.local.MessageEntity
import com.alfredo.medusaalfha.data.local.SecureKeyManager
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        ChatMessageEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        MemoryNodeEntity::class,
        ParcelEntity::class,
        MemoryEntity::class,
        MemoryEntry::class,
        InteractionEntity::class,
        AccessPassEntity::class,
        AccessLogEntity::class,
        SmartDeviceEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class MedusaDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryNodeDao(): MemoryNodeDao
    abstract fun parcelDao(): ParcelDao
    abstract fun memoryDao(): MemoryDao
    abstract fun memoryEntryDao(): MemoryEntryDao
    abstract fun interactionDao(): InteractionDao
    abstract fun accessPassDao(): AccessPassDao
    abstract fun accessLogDao(): AccessLogDao
    abstract fun smartDeviceDao(): SmartDeviceDao

    companion object {
        @Volatile
        private var INSTANCE: MedusaDatabase? = null

        fun getDatabase(context: Context): MedusaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): MedusaDatabase {
            val appContext = context.applicationContext
            return try {
                try {
                    System.loadLibrary("sqlcipher")
                } catch (_: UnsatisfiedLinkError) {
                    SQLiteDatabase.loadLibs(appContext)
                }

                val factory = SecureKeyManager.getSupportFactory(appContext)
                Room.databaseBuilder(
                    appContext,
                    MedusaDatabase::class.java,
                    "medusa_alfha_encrypted.db"
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
            } catch (e: Throwable) {
                android.util.Log.e("MedusaDatabase", "Fallback to unencrypted Room due to initialization notice: ${e.message}")
                Room.databaseBuilder(
                    appContext,
                    MedusaDatabase::class.java,
                    "medusa_alfha_fallback.db"
                )
                .fallbackToDestructiveMigration()
                .build()
            }
        }
    }
}
