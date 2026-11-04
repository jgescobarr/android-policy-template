package org.template.core.security

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.google.crypto.tink.Aead
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FakeDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow<Preferences>(emptyPreferences())

    override val data: Flow<Preferences> get() = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

class SecureStorageManagerTest {

    private val fakeAead = object : Aead {
        override fun encrypt(plaintext: ByteArray, associatedData: ByteArray): ByteArray =
            plaintext.map { (it + 1).toByte() }.toByteArray()

        override fun decrypt(ciphertext: ByteArray, associatedData: ByteArray): ByteArray =
            ciphertext.map { (it - 1).toByte() }.toByteArray()
    }

    private val dataStore = FakeDataStore()
    private lateinit var storageManager: SecureStorageManager

    @BeforeEach
    fun setup() {
        storageManager = SecureStorageManager(dataStore, fakeAead)
    }

    @Test
    fun `saveToken guarda y getToken recupera el token cifrado correctamente`() = runTest {
        storageManager.saveToken("user_token", "secret123")

        val retrieved = storageManager.getToken("user_token")

        assertEquals("secret123", retrieved)
    }

    @Test
    fun `deleteToken elimina el token del almacenamiento`() = runTest {
        storageManager.saveToken("user_token", "secret123")
        storageManager.deleteToken("user_token")

        val retrieved = storageManager.getToken("user_token")

        assertNull(retrieved)
    }

    @Test
    fun `clearAll borra todas las claves`() = runTest {
        storageManager.saveToken("token1", "val1")
        storageManager.saveToken("token2", "val2")
        storageManager.clearAll()

        assertNull(storageManager.getToken("token1"))
        assertNull(storageManager.getToken("token2"))
    }

    @Test
    fun `getToken retorna null y limpia la entrada si el descifrado falla por datos corruptos`() = runTest {
        val throwingAead = object : Aead {
            override fun encrypt(plaintext: ByteArray, associatedData: ByteArray): ByteArray = plaintext
            override fun decrypt(ciphertext: ByteArray, associatedData: ByteArray): ByteArray =
                throw java.security.GeneralSecurityException("Clave o dato corrupto")
        }
        val corruptStorageManager = SecureStorageManager(dataStore, throwingAead)
        corruptStorageManager.saveToken("corrupt_key", "data")

        val result = corruptStorageManager.getToken("corrupt_key")

        assertNull(result)
    }
}
