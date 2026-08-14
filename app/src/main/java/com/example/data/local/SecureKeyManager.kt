package com.example.data.local

import android.content.Context
import com.alfredo.medusaalfha.data.local.SecureKeyManager as DelegatedKeyManager
import net.sqlcipher.database.SupportFactory

/**
 * Proxy y fachada de acceso seguro a SecureKeyManager en el espacio de nombres com.example.data.local.
 */
object SecureKeyManager {
    fun getInstance(context: Context): DelegatedKeyManager = DelegatedKeyManager.getInstance(context)
    fun getPassphrase(context: Context): ByteArray = DelegatedKeyManager.getPassphrase(context)
    fun getSupportFactory(context: Context): SupportFactory = DelegatedKeyManager.getSupportFactory(context)
}
