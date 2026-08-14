package com.alfredo.medusaalfha.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.db.AccessLogDao
import com.example.data.db.AccessLogEntity
import com.example.data.db.AccessPassDao
import com.example.data.db.AccessPassEntity
import com.example.data.db.ChatMessageDao
import com.example.data.db.ChatMessageEntity
import com.example.data.db.InteractionDao
import com.example.data.db.InteractionEntity
import com.example.data.db.MemoryDao
import com.example.data.db.MemoryEntity
import com.example.data.db.MemoryNodeDao
import com.example.data.db.MemoryNodeEntity
import com.example.data.db.ParcelDao
import com.example.data.db.ParcelEntity
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
        InteractionEntity::class,
        AccessPassEntity::class,
        AccessLogEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryNodeDao(): MemoryNodeDao
    abstract fun parcelDao(): ParcelDao
    abstract fun memoryDao(): MemoryDao
    abstract fun interactionDao(): InteractionDao
    abstract fun accessPassDao(): AccessPassDao
    abstract fun accessLogDao(): AccessLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    System.loadLibrary("sqlcipher")
                } catch (_: UnsatisfiedLinkError) {
                    SQLiteDatabase.loadLibs(context)
                }

                val factory = SecureKeyManager.getSupportFactory(context)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medusa_alfha_encrypted.db"
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
