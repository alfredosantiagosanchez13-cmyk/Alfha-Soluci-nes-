package com.example.data.model

import com.example.ui.UserRole

data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String,
    val role: UserRole,
    val houseNumber: Int? = null,
    val isEmailVerified: Boolean = false,
    val photoUrl: String? = null
)
