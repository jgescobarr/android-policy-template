package org.template.core.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.template.core.logging.AppLogger
import javax.inject.Singleton

private const val PREFERENCES_NAME = "secure_storage"
private const val KEYSET_NAME = "secure_storage_keyset"
private const val MASTER_KEY_URI = "android-keystore://secure_storage_master_key"

private val Context.secureDataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCES_NAME,
)

/**
 * Módulo de Hilt para :core:security. Toda la configuración de Tink (que
 * requiere inicialización explícita) vive aquí, para que
 * [SecureStorageManager] reciba un [Aead] ya listo sin conocer cómo se
 * construyó (Manual de Arquitectura, sección 2.7 — el dominio/consumidor
 * de un Gateway no conoce los detalles del otro lado).
 *
 * Incluye un mecanismo de recuperación automática ante corrupción de la
 * clave maestra de Android Keystore o desincronización por backup.
 */
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.secureDataStore

    @Provides
    @Singleton
    fun provideAead(
        @ApplicationContext context: Context,
        appLogger: AppLogger,
    ): Aead {
        AeadConfig.register()
        return try {
            buildAead(context)
        } catch (e: Exception) {
            appLogger.warn(
                "SecurityModule: Error al cargar AndroidKeysetManager (%s). Recuperando clave maestra...",
                e.message ?: "error desconocido",
            )
            recoverCorruptedKeyset(context)
            buildAead(context)
        }
    }

    private fun buildAead(context: Context): Aead {
        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREFERENCES_NAME)
            .withKeyTemplate(com.google.crypto.tink.aead.AeadKeyTemplates.AES256_GCM)
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
        return keysetHandle.getPrimitive(Aead::class.java)
    }

    private fun recoverCorruptedKeyset(context: Context) {
        runCatching {
            context.getSharedPreferences(KEYSET_NAME, Context.MODE_PRIVATE).edit().clear().commit()
            val ks = java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val alias = MASTER_KEY_URI.removePrefix("android-keystore://")
            if (ks.containsAlias(alias)) {
                ks.deleteEntry(alias)
            }
        }
    }

    @Provides
    @Singleton
    fun provideRootSignalSource(
        @ApplicationContext context: Context,
        appLogger: AppLogger,
    ): RootSignalSource = DefaultRootSignalSource(context, appLogger)

    @Provides
    @Singleton
    fun provideSecureStorage(impl: SecureStorageManager): SecureStorage = impl

    @Provides
    @Singleton
    fun provideKeyProvider(impl: KeystoreManager): KeyProvider = impl

}
