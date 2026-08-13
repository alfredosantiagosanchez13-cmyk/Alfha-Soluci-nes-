package com.example.data.model

data class PackageScanResult(
    val houseNumber: String,
    val recipientName: String,
    val carrier: String,
    val description: String,
    val matchedResidentName: String = "",
    val matchedPhone: String = "",
    val confidence: String = "Alta"
)
