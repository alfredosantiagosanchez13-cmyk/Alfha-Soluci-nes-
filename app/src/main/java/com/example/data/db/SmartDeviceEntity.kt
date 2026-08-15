package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smart_devices")
data class SmartDeviceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deviceId: String,
    val name: String,
    val zone: String, // e.g. "Sala Alpha", "Recámara Principal", "Terraza & Jardín", "Caseta & Acceso", "Estudio Medusa"
    val deviceType: String, // "LIGHT", "CLIMATE", "GATEWAY", "CURTAIN", "LOCK"
    val protocol: String, // "REST_API", "BLUETOOTH_LE"
    val endpointUrl: String = "", // e.g. "http://192.168.1.105/api/v1/lights/salon"
    val bleMacOrUuid: String = "", // e.g. "00:1A:7D:DA:71:13" or GATT Service UUID
    val isOn: Boolean = false,
    val brightness: Int = 100, // 0 - 100%
    val colorHex: String = "#8B5CF6", // Hex RGB color
    val colorTempK: Int = 4000, // 2700K - 6500K
    val targetTempC: Float = 22.0f, // 16.0°C - 30.0°C
    val currentTempC: Float = 23.5f, // Sensor feedback
    val hvacMode: String = "COOL", // "COOL", "HEAT", "ECO", "AUTO", "FAN_ONLY", "OFF"
    val fanSpeed: String = "AUTO", // "AUTO", "LOW", "MED", "HIGH"
    val humidityPercent: Int = 48,
    val isOnline: Boolean = true,
    val lastUpdatedMs: Long = System.currentTimeMillis()
)
