package com.example.data.repository

import android.util.Log
import com.example.data.model.UserProfile
import com.example.ui.UserRole
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository {

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.w("FirebaseAuthRepository", "FirebaseAuth unavailable or not initialized: ${e.message}")
            null
        }

    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.w("FirebaseAuthRepository", "FirebaseFirestore unavailable or not initialized: ${e.message}")
            null
        }

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val currentAuth = auth
        if (currentAuth == null) {
            Log.i("FirebaseAuthRepository", "FirebaseAuth is null, emitting null auth state.")
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        try {
            currentAuth.addAuthStateListener(listener)
        } catch (e: Throwable) {
            Log.w("FirebaseAuthRepository", "Error adding auth state listener", e)
            trySend(null)
        }

        awaitClose {
            try {
                currentAuth.removeAuthStateListener(listener)
            } catch (e: Throwable) {
                Log.w("FirebaseAuthRepository", "Error removing auth state listener", e)
            }
        }
    }

    val currentUser: FirebaseUser?
        get() = try {
            auth?.currentUser
        } catch (e: Throwable) {
            Log.e("FirebaseAuthRepository", "Error getting current user", e)
            null
        }

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> {
        val currentAuth = auth ?: return Result.failure(Exception("Firebase no está configurado en este entorno."))
        return try {
            val result = currentAuth.signInWithEmailAndPassword(email.trim(), pass.trim()).await()
            val user = result.user ?: throw Exception("Usuario no encontrado")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "Inicio de sesión fallido", e)
            Result.failure(e)
        }
    }

    suspend fun registerUser(
        email: String,
        pass: String,
        displayName: String,
        role: UserRole,
        houseNumber: Int? = null
    ): Result<FirebaseUser> {
        val currentAuth = auth ?: return Result.failure(Exception("Firebase no está configurado en este entorno."))
        return try {
            val result = currentAuth.createUserWithEmailAndPassword(email.trim(), pass.trim()).await()
            val user = result.user ?: throw Exception("Error al crear usuario en Firebase Auth")

            // Update display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName.ifBlank { email.substringBefore("@") })
                .build()
            user.updateProfile(profileUpdates).await()

            // Save user profile metadata in Firestore (if available)
            try {
                firestore?.let { db ->
                    val userData = mapOf(
                        "uid" to user.uid,
                        "email" to (user.email ?: email),
                        "displayName" to displayName,
                        "role" to role.name,
                        "houseNumber" to houseNumber,
                        "createdAt" to System.currentTimeMillis()
                    )
                    db.collection("users").document(user.uid).set(userData).await()
                }
            } catch (e: Exception) {
                Log.w("FirebaseAuthRepository", "No se pudo sincronizar en Firestore, continuando localmente", e)
            }

            Result.success(user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "Error al registrar usuario", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithCredential(credential: AuthCredential): Result<FirebaseUser> {
        val currentAuth = auth ?: return Result.failure(Exception("Firebase no está disponible."))
        return try {
            val result = currentAuth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Credencial inválida")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "Error con credencial", e)
            Result.failure(e)
        }
    }

    suspend fun fetchUserProfile(uid: String, fallbackEmail: String, fallbackName: String): UserProfile {
        val currentFirestore = firestore
        if (currentFirestore != null) {
            try {
                val doc = currentFirestore.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    val roleStr = doc.getString("role") ?: UserRole.RESIDENTES.name
                    val houseNum = doc.getLong("houseNumber")?.toInt()
                    val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.RESIDENTES }
                    val name = doc.getString("displayName") ?: fallbackName

                    return UserProfile(
                        uid = uid,
                        email = fallbackEmail,
                        displayName = name,
                        role = role,
                        houseNumber = houseNum,
                        isEmailVerified = currentUser?.isEmailVerified ?: false,
                        photoUrl = currentUser?.photoUrl?.toString()
                    )
                }
            } catch (e: Exception) {
                Log.w("FirebaseAuthRepository", "Error leyendo perfil Firestore", e)
            }
        }

        // Default or inferred profile
        val inferredRole = when {
            fallbackEmail.contains("admin", ignoreCase = true) -> UserRole.ADMINISTRACION
            fallbackEmail.contains("guardia", ignoreCase = true) -> UserRole.GUARDIA
            fallbackEmail.contains("alfha", ignoreCase = true) -> UserRole.ALFHA_SANTIAGO
            else -> UserRole.RESIDENTES
        }
        return UserProfile(
            uid = uid,
            email = fallbackEmail,
            displayName = fallbackName,
            role = inferredRole,
            isEmailVerified = currentUser?.isEmailVerified ?: false
        )
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        val currentAuth = auth ?: return Result.failure(Exception("Firebase no está disponible."))
        return try {
            currentAuth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "Error al cerrar sesión", e)
        }
    }
}
