package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Gestor de almacenamiento seguro para GEMINI_API_KEY utilizando EncryptedSharedPreferences (AES-256 GCM).
 */
class SecureApiKeyStorage(private val context: Context) {

    private val securePrefs: SharedPreferences by lazy {
        createEncryptedSharedPreferences(context)
    }

    private fun createEncryptedSharedPreferences(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                SECURE_PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("SecureApiKeyStorage", "No se pudo inicializar EncryptedSharedPreferences, usando fallback privado", e)
            context.getSharedPreferences("medusa_nexus_fallback_secure_prefs", Context.MODE_PRIVATE)
        }
    }

    fun getApiKey(): String {
        return try {
            val key = securePrefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
            if (key.isNotBlank()) {
                key
            } else {
                // Fallback de migración desde SharedPreferences anterior si existía
                val legacyPrefs = context.getSharedPreferences("medusa_nexus_prefs", Context.MODE_PRIVATE)
                val legacyKey = legacyPrefs.getString("medusa_gemini_api_key", "") ?: ""
                if (legacyKey.isNotBlank()) {
                    saveApiKey(legacyKey)
                    legacyPrefs.edit().remove("medusa_gemini_api_key").apply()
                }
                legacyKey
            }
        } catch (e: Exception) {
            Log.e("SecureApiKeyStorage", "Error leyendo clave segura", e)
            ""
        }
    }

    fun saveApiKey(apiKey: String): Boolean {
        val trimmed = apiKey.trim()
        if (trimmed.isBlank()) {
            return false
        }
        return try {
            securePrefs.edit()
                .putString(KEY_GEMINI_API_KEY, trimmed)
                .apply()
            true
        } catch (e: Exception) {
            Log.e("SecureApiKeyStorage", "Error guardando clave cifrada", e)
            false
        }
    }

    fun clearApiKey(): Boolean {
        return try {
            securePrefs.edit().remove(KEY_GEMINI_API_KEY).apply()
            val legacyPrefs = context.getSharedPreferences("medusa_nexus_prefs", Context.MODE_PRIVATE)
            legacyPrefs.edit().remove("medusa_gemini_api_key").apply()
            true
        } catch (e: Exception) {
            Log.e("SecureApiKeyStorage", "Error eliminando clave", e)
            false
        }
    }

    companion object {
        private const val SECURE_PREFS_FILE_NAME = "medusa_encrypted_nexus_prefs"
        private const val KEY_GEMINI_API_KEY = "encrypted_gemini_api_key"
    }
}
