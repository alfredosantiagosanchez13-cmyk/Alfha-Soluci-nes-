package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ParcelDao {
    @Query("SELECT * FROM parcels ORDER BY timestamp DESC")
    fun getAllParcels(): Flow<List<ParcelEntity>>

    @Query("SELECT * FROM parcels WHERE status = 'RECIBIDO' ORDER BY timestamp DESC")
    fun getPendingParcels(): Flow<List<ParcelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParcel(parcel: ParcelEntity): Long

    @Query("UPDATE parcels SET status = :status, isNotified = :isNotified WHERE id = :id")
    suspend fun updateParcelStatus(id: Long, status: String, isNotified: Boolean)

    @Delete
    suspend fun deleteParcel(parcel: ParcelEntity)

    @Query("DELETE FROM parcels")
    suspend fun clearAllParcels()
}
