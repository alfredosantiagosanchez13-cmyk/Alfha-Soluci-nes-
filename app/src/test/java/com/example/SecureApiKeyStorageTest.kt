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
class SecureApiKeyStorageTest {

    private lateinit var context: Context
    private lateinit var secureStorage: SecureApiKeyStorage

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        secureStorage = SecureApiKeyStorage(context)
        secureStorage.clearApiKey()
    }

    @Test
    fun saveAndRetrieveApiKeySuccessfully() {
        val testKey = "AIzaSyFakeTestKey1234567890ABCDEF"
        val saved = secureStorage.saveApiKey(testKey)

        assertTrue("Key should be saved successfully", saved)
        assertEquals("Retrieved key should match stored key", testKey, secureStorage.getApiKey())
    }

    @Test
    fun rejectEmptyOrBlankApiKey() {
        val savedBlank = secureStorage.saveApiKey("   ")
        assertFalse("Blank key should be rejected", savedBlank)
        assertEquals("", secureStorage.getApiKey())
    }

    @Test
    fun clearApiKeySuccessfully() {
        val testKey = "AIzaSyTemporaryKey999"
        secureStorage.saveApiKey(testKey)
        assertEquals(testKey, secureStorage.getApiKey())

        val cleared = secureStorage.clearApiKey()
        assertTrue("Clearing key should return true", cleared)
        assertEquals("", secureStorage.getApiKey())
    }
}
