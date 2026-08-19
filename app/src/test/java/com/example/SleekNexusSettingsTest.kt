package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.security.SecureApiKeyStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SleekNexusSettingsTest {

    private lateinit var context: Context
    private lateinit var secureStorage: SecureApiKeyStorage

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        secureStorage = SecureApiKeyStorage(context)
        secureStorage.clearApiKey()
    }

    @Test
    fun validatorRejectsEmptyAndWhitespaceKeys() {
        assertFalse("Empty string must be rejected", secureStorage.saveApiKey(""))
        assertFalse("Whitespace string must be rejected", secureStorage.saveApiKey("   "))
        assertFalse("Newline whitespace must be rejected", secureStorage.saveApiKey("\n\t  "))
        assertEquals("Vault should remain empty", "", secureStorage.getApiKey())
    }

    @Test
    fun validatorAcceptsAndEncryptsValidGeminiKey() {
        val validKey = "AIzaSyD-sampleGeminiApiKeyTesting12345"
        val isSaved = secureStorage.saveApiKey(validKey)

        assertTrue("Valid key should be encrypted and saved in EncryptedSharedPreferences", isSaved)
        assertTrue("Storage should report valid key present", secureStorage.hasValidKey())
        assertEquals("Decrypted key should match original input", validKey, secureStorage.getApiKey())
    }

    @Test
    fun clearKeyRemovesFromEncryptedSharedPreferences() {
        val validKey = "AIzaSyD-toBeDeletedKey999"
        secureStorage.saveApiKey(validKey)
        assertEquals(validKey, secureStorage.getApiKey())

        secureStorage.clearApiKey()
        assertFalse("Storage should report no key present", secureStorage.hasValidKey())
        assertEquals("", secureStorage.getApiKey())
    }
}
