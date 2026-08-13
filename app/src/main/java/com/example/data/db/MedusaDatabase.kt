package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChatMessageEntity::class,
        MemoryNodeEntity::class,
        ParcelEntity::class,
        MemoryEntity::class,
        InteractionEntity::class,
        AccessPassEntity::class,
        AccessLogEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class MedusaDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun memoryNodeDao(): MemoryNodeDao
    abstract fun parcelDao(): ParcelDao
    abstract fun memoryDao(): MemoryDao
    abstract fun interactionDao(): InteractionDao
    abstract fun accessPassDao(): AccessPassDao
    abstract fun accessLogDao(): AccessLogDao

    companion object {
        @Volatile
        private var INSTANCE: MedusaDatabase? = null

        fun getDatabase(context: Context): MedusaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedusaDatabase::class.java,
                    "medusa_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
