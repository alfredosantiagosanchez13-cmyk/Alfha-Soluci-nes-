package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccessLogDao {
    @Query("SELECT * FROM access_logs ORDER BY timestampMs DESC")
    fun getAllAccessLogs(): Flow<List<AccessLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccessLog(log: AccessLogEntity)

    @Delete
    suspend fun deleteAccessLog(log: AccessLogEntity)

    @Query("DELETE FROM access_logs")
    suspend fun clearAllAccessLogs()
}
