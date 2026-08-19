package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Proveedor seguro singleton para la recuperación de la GEMINI_API_KEY almacenada
 * en EncryptedSharedPreferences (Android Security-Crypto AES-256 GCM / AES-256 SIV).
 */
object SecureApiKeyProvider {

    private const val TAG = "SecureApiKeyProvider"
    private const val SECURE_PREFS_FILE_NAME = "medusa_encrypted_nexus_prefs"
    private const val KEY_GEMINI_API_KEY = "encrypted_gemini_api_key"

    @Volatile
    private var cachedEncryptedPrefs: SharedPreferences? = null

    private fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        return cachedEncryptedPrefs ?: synchronized(this) {
            cachedEncryptedPrefs ?: try {
                val masterKey = MasterKey.Builder(context.applicationContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                EncryptedSharedPreferences.create(
                    context.applicationContext,
                    SECURE_PREFS_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                ).also { cachedEncryptedPrefs = it }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing EncryptedSharedPreferences, using SecureApiKeyStorage fallback", e)
                context.applicationContext.getSharedPreferences("medusa_nexus_fallback_secure_prefs", Context.MODE_PRIVATE)
            }
        }
    }

    /**
     * Obtiene la clave de API almacenada de forma segura en EncryptedSharedPreferences.
     * Retorna null si no hay clave almacenada o si está vacía.
     */
    fun getApiKey(context: Context): String? {
        return try {
            // Intentar primero desde SecureApiKeyStorage para asegurar migración y consistencia
            val secureStorage = SecureApiKeyStorage(context.applicationContext)
            val storedKey = secureStorage.getApiKey().trim()
            if (storedKey.isNotBlank()) {
                return storedKey
            }

            // Fallback directo a EncryptedSharedPreferences
            val prefs = getEncryptedSharedPreferences(context)
            val key = prefs.getString(KEY_GEMINI_API_KEY, "")?.trim()
            if (!key.isNullOrBlank()) key else null
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving API key from EncryptedSharedPreferences", e)
            null
        }
    }
}
