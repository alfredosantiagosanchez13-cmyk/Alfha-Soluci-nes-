package com.example.data.local

import com.alfredo.medusaalfha.data.local.SecurePassphraseProvider as DelegatedProvider
import android.content.Context
import net.sqlcipher.database.SupportFactory

/**
 * Proxy de compatibilidad para com.example.data.local
 */
object SecurePassphraseProvider {
    fun getPassphrase(context: Context): ByteArray = DelegatedProvider.getPassphrase(context)
    fun getSupportFactory(context: Context): SupportFactory = DelegatedProvider.getSupportFactory(context)
    fun clearCache() = DelegatedProvider.clearCache()
}
