package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parcels")
data class ParcelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val houseNumber: String,
    val recipientName: String,
    val carrier: String,
    val description: String,
    val phone: String = "",
    val status: String = "RECIBIDO", // "RECIBIDO", "ENTREGADO"
    val timestamp: Long = System.currentTimeMillis(),
    val isNotified: Boolean = false,
    val photoBase64: String = ""
)
