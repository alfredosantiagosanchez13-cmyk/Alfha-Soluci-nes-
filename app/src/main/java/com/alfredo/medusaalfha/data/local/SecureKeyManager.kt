package com.alfredo.medusaalfha.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom

/**
 * Gestor seguro de claves criptográficas para bases de datos SQLCipher
 * utilizando Android KeyStore y EncryptedSharedPreferences (AES-256 GCM / AES-256 SIV).
 *
 * Garantiza la confidencialidad de los mensajes, conversaciones e información confidencial
 * almacenada en MedusaDatabase.
 */
class SecureKeyManager private constructor(context: Context) {

    private val appContext: Context = context.applicationContext

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setRequestStrongBoxBacked(false)
            .build()
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            appContext,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Obtiene o genera la clave criptográfica para la base de datos SQLCipher.
     * Retorna la frase de paso en formato [ByteArray] lista para [SupportFactory].
     */
    @Synchronized
    fun getOrCreatePassphrase(): ByteArray {
        val existingPassphrase = encryptedPrefs.getString(KEY_DB_PASSPHRASE, null)
        if (!existingPassphrase.isNullOrBlank()) {
            return SQLiteDatabase.getBytes(existingPassphrase.toCharArray())
        }

        // Migración o comprobación de preferencias previas
        val legacyPass = checkLegacyPassphrase()
        if (!legacyPass.isNullOrBlank()) {
            encryptedPrefs.edit().putString(KEY_DB_PASSPHRASE, legacyPass).apply()
            return SQLiteDatabase.getBytes(legacyPass.toCharArray())
        }

        // Generar una nueva clave con 256 bits de entropía segura (SecureRandom)
        val newPassphrase = generateSecureEntropyKey()
        encryptedPrefs.edit().putString(KEY_DB_PASSPHRASE, newPassphrase).apply()
        Log.i(TAG, "Nueva clave criptográfica SQLCipher generada y guardada en EncryptedSharedPreferences.")
        return SQLiteDatabase.getBytes(newPassphrase.toCharArray())
    }

    /**
     * Genera una instancia de [SupportFactory] configurada con la clave segura.
     */
    fun getSupportFactory(): SupportFactory {
        return SupportFactory(getOrCreatePassphrase())
    }

    /**
     * Comprueba si ya existe una clave persistida en el almacén seguro.
     */
    fun hasDatabaseKey(): Boolean {
        return encryptedPrefs.contains(KEY_DB_PASSPHRASE) || !checkLegacyPassphrase().isNullOrBlank()
    }

    /**
     * Genera una clave criptográficamente robusta de 256 bits (32 bytes) codificada en Base64.
     */
    private fun generateSecureEntropyKey(): String {
        val randomBytes = ByteArray(32)
        SecureRandom().nextBytes(randomBytes)
        return Base64.encodeToString(randomBytes, Base64.NO_WRAP)
    }

    /**
     * Comprueba si existía una clave en la versión anterior de preferencias protegidas.
     */
    private fun checkLegacyPassphrase(): String? {
        return try {
            val legacyPrefs = EncryptedSharedPreferences.create(
                appContext,
                "medusa_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            legacyPrefs.getString("db_passphrase", null)
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Limpia la clave almacenada (utilizado únicamente en restablecimientos de seguridad o logout total).
     */
    @Synchronized
    fun clearKey() {
        try {
            encryptedPrefs.edit().remove(KEY_DB_PASSPHRASE).apply()
            Log.w(TAG, "Clave criptográfica de base de datos eliminada.")
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar clave de base de datos", e)
        }
    }

    companion object {
        private const val TAG = "SecureKeyManager"
        private const val PREFS_FILE_NAME = "medusa_encrypted_db_keys"
        private const val KEY_DB_PASSPHRASE = "secure_sqlcipher_passphrase_v1"

        @Volatile
        private var instance: SecureKeyManager? = null

        fun getInstance(context: Context): SecureKeyManager {
            return instance ?: synchronized(this) {
                instance ?: SecureKeyManager(context).also { instance = it }
            }
        }

        /**
         * Helper estático directo para obtener la frase de paso de SQLCipher.
         */
        fun getPassphrase(context: Context): ByteArray {
            return getInstance(context).getOrCreatePassphrase()
        }

        /**
         * Helper estático directo para construir el [SupportFactory] de Room.
         */
        fun getSupportFactory(context: Context): SupportFactory {
            return getInstance(context).getSupportFactory()
        }
    }
}
