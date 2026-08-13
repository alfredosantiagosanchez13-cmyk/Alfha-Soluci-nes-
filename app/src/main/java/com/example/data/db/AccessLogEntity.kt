package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "access_logs")
data class AccessLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val passCode: String,
    val residentName: String,
    val residentHouse: String,
    val visitorName: String,
    val accessType: String,
    val isGranted: Boolean,
    val resultReason: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val scannedByRole: String = "GUARDIA"
)
