package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartDeviceDao {

    @Query("SELECT * FROM smart_devices ORDER BY zone ASC, name ASC")
    fun getAllDevices(): Flow<List<SmartDeviceEntity>>

    @Query("SELECT * FROM smart_devices WHERE zone = :zone ORDER BY name ASC")
    fun getDevicesByZone(zone: String): Flow<List<SmartDeviceEntity>>

    @Query("SELECT * FROM smart_devices WHERE deviceType = :type ORDER BY name ASC")
    fun getDevicesByType(type: String): Flow<List<SmartDeviceEntity>>

    @Query("SELECT * FROM smart_devices WHERE id = :id LIMIT 1")
    suspend fun getDeviceById(id: Long): SmartDeviceEntity?

    @Query("SELECT * FROM smart_devices WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getDeviceByDeviceId(deviceId: String): SmartDeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: SmartDeviceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<SmartDeviceEntity>)

    @Update
    suspend fun updateDevice(device: SmartDeviceEntity)

    @Query("UPDATE smart_devices SET isOn = :isOn, lastUpdatedMs = :timestamp WHERE id = :id")
    suspend fun updatePowerState(id: Long, isOn: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE smart_devices SET isOn = :isOn, brightness = :brightness, colorHex = :colorHex, lastUpdatedMs = :timestamp WHERE id = :id")
    suspend fun updateLightSettings(id: Long, isOn: Boolean, brightness: Int, colorHex: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE smart_devices SET isOn = :isOn, targetTempC = :targetTemp, hvacMode = :hvacMode, fanSpeed = :fanSpeed, lastUpdatedMs = :timestamp WHERE id = :id")
    suspend fun updateClimateSettings(id: Long, isOn: Boolean, targetTemp: Float, hvacMode: String, fanSpeed: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE smart_devices SET isOn = :isOn, lastUpdatedMs = :timestamp WHERE deviceType = :type")
    suspend fun updateAllPowerByType(type: String, isOn: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE smart_devices SET isOn = :isOn, lastUpdatedMs = :timestamp")
    suspend fun setAllDevicesPower(isOn: Boolean, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteDevice(device: SmartDeviceEntity)

    @Query("DELETE FROM smart_devices")
    suspend fun clearAllDevices()
}
