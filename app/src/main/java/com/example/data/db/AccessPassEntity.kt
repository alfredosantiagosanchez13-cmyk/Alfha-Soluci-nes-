package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "access_passes")
data class AccessPassEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val passCode: String,
    val residentHouse: String,
    val residentName: String,
    val visitorName: String,
    val accessType: String = "VISITOR", // VISITOR, DELIVERY, SERVICE, FAMILY
    val validUntilTimestamp: Long,
    val isUsed: Boolean = false,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val createdByRole: String = "RESIDENTE"
)
