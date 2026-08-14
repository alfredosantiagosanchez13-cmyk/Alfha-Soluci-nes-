package com.alfredo.medusaalfha.data.local

import android.content.Context
import net.sqlcipher.database.SupportFactory

/**
 * Proveedor de frase de paso criptográfica para SQLCipher, respaldado por [SecureKeyManager]
 * y EncryptedSharedPreferences con Android KeyStore.
 */
object SecurePassphraseProvider {
    fun getPassphrase(context: Context): ByteArray {
        return SecureKeyManager.getPassphrase(context)
    }

    fun getSupportFactory(context: Context): SupportFactory {
        return SecureKeyManager.getSupportFactory(context)
    }

    fun clearCache() {
        // No-op or cache management
    }
}
