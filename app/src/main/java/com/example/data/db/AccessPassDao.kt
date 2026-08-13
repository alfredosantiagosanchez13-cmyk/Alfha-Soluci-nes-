package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccessPassDao {
    @Query("SELECT * FROM access_passes ORDER BY createdAtTimestamp DESC")
    fun getAllPasses(): Flow<List<AccessPassEntity>>

    @Query("SELECT * FROM access_passes WHERE residentHouse = :house ORDER BY createdAtTimestamp DESC")
    fun getPassesByHouse(house: String): Flow<List<AccessPassEntity>>

    @Query("SELECT * FROM access_passes WHERE passCode = :code LIMIT 1")
    suspend fun getPassByCode(code: String): AccessPassEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPass(pass: AccessPassEntity): Long

    @Update
    suspend fun updatePass(pass: AccessPassEntity)

    @Query("UPDATE access_passes SET isUsed = 1 WHERE passCode = :code")
    suspend fun markPassAsUsed(code: String): Int

    @Delete
    suspend fun deletePass(pass: AccessPassEntity)

    @Query("DELETE FROM access_passes WHERE validUntilTimestamp < :now")
    suspend fun deleteExpiredPasses(now: Long = System.currentTimeMillis()): Int

    @Query("DELETE FROM access_passes")
    suspend fun clearAll()
}
