package org.template.core.security

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.crypto.tink.Aead
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Contrato Gateway para almacenamiento seguro de datos sensibles.
 *
 * Referencia normativa: Manual de Seguridad, sección 4.1 — "Se debe
 * implementar una clase SecureStorageManager que actúe como el único punto
 * de acceso a cualquier mecanismo de almacenamiento de datos sensibles en
 * la aplicación. Ningún componente de UI, ViewModel, UseCase ni repositorio
 * distinto al de seguridad puede acceder directamente a
 * EncryptedSharedPreferences, DataStore cifrado, Android Keystore."
 *
 * Es, en términos del Manual de Arquitectura (sección 2.7), una
 * especialización del patrón Gateway: aísla al resto de la aplicación de
 * los detalles concretos del mecanismo de almacenamiento subyacente.
 *
 * TRACEABILITY.md > :core:security >
 *   "SecureStorageManager implementado como único punto de acceso" (Bloqueante · PR)
 *   "Datos sensibles cifrados en reposo" (Bloqueante · PR)
 */
interface SecureStorage {
    suspend fun saveString(key: String, value: String)
    suspend fun getString(key: String): String?
    suspend fun saveBoolean(key: String, value: Boolean)
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    suspend fun remove(key: String)
    suspend fun clearAll()

    // Métodos de conveniencia/compatibilidad
    suspend fun saveToken(key: String, token: String) = saveString(key, token)
    suspend fun getToken(key: String): String? = getString(key)
    suspend fun deleteToken(key: String) = remove(key)
}

/**
 * Implementación genérica de referencia de [SecureStorage] usando DataStore + Tink AEAD + Android Keystore.
 *
 * Referencia normativa: Manual de Seguridad, sección 3.3 y 4.1 — "Se debe
 * implementar una clase SecureStorageManager que actúe como el único punto
 * de acceso a cualquier mecanismo de almacenamiento de datos sensibles en
 * la aplicación."
 */
@Singleton
class SecureStorageManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val aead: Aead,
) : SecureStorage {

    override suspend fun saveString(key: String, value: String) {
        val encrypted = aead.encrypt(value.toByteArray(), /* associatedData = */ ByteArray(0))
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey(key)] = encrypted.toBase64()
        }
    }

    override suspend fun getString(key: String): String? {
        val stored = dataStore.data.first()[stringPreferencesKey(key)] ?: return null
        return try {
            val decrypted = aead.decrypt(stored.fromBase64(), /* associatedData = */ ByteArray(0))
            String(decrypted)
        } catch (_: Exception) {
            remove(key)
            null
        }
    }

    override suspend fun saveBoolean(key: String, value: Boolean) {
        saveString(key, value.toString())
    }

    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getString(key)?.toBooleanStrictOrNull() ?: defaultValue
    }

    override suspend fun remove(key: String) {
        dataStore.edit { prefs -> prefs.remove(stringPreferencesKey(key)) }
    }

    override suspend fun clearAll() {
        dataStore.edit { prefs -> prefs.clear() }
    }

    private fun ByteArray.toBase64(): String =
        java.util.Base64.getEncoder().encodeToString(this)

    private fun String.fromBase64(): ByteArray =
        java.util.Base64.getDecoder().decode(this)
}
