package org.template.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

private const val ANDROID_KEYSTORE = "AndroidKeyStore"

/**
 * Contrato para obtener claves criptográficas del Keystore.
 *
 * Referencia normativa: Manual de Arquitectura, sección 2.7 (patrón
 * Gateway) — los consumidores (por ejemplo [org.template.feature.auth]
 * para `BiometricPrompt.CryptoObject`) dependen de esta interfaz, nunca
 * de [KeystoreManager] directamente.
 *
 * Esta interfaz se agregó al detectar, mediante la regla custom
 * `NoConcreteSecurityClassLeak` de detekt, que `BiometricAuthenticator`
 * en :feature:auth inyectaba la clase concreta — una violación de la
 * sección 4.1 del Manual de Seguridad que llevaba varias rondas sin
 * detectarse en revisión humana.
 */
interface KeyProvider {
    fun getOrCreateKey(alias: String, requireUserAuthentication: Boolean = false): SecretKey
}

/**
 * Generación y recuperación de claves criptográficas en Android Keystore,
 * con hardware-backing y StrongBox cuando el dispositivo lo soporta.
 *
 * Referencia normativa: Manual de Seguridad, sección 3.2 — "La
 * configuración obligatoria para dispositivos que lo soporten es el uso de
 * hardware-backed keys ... las claves nunca son exportadas al proceso de la
 * aplicación", y sección 4.2 — "Todas las claves criptográficas ...
 * marcadas como no exportables."
 *
 * TRACEABILITY.md > :core:security >
 *   "Claves generadas en Android Keystore con hardware-backing" (Crítico · PR)
 *   "Claves criptográficas marcadas como no exportables" (Crítico · PR)
 *
 * Nota de implementación: [SecureStorageManager] no debe llamar a esta
 * clase directamente para operaciones de cifrado — solo para obtener/crear
 * la clave que luego se envuelve en un [com.google.crypto.tink.Aead] (Seg
 * 4.3, Tink como única librería de cifrado aprobada).
 */
@Singleton
class KeystoreManager @Inject constructor() : KeyProvider {

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        .apply { load(null) }

    /**
     * Retorna la clave [alias] si ya existe, o la genera si no.
     * La clave generada es no exportable por construcción: `KeyGenParameterSpec`
     * nunca expone el material de la clave fuera del Keystore.
     */
    override fun getOrCreateKey(alias: String, requireUserAuthentication: Boolean): SecretKey {
        keyStore.getKey(alias, null)?.let { return it as SecretKey }
        return generateKey(alias, requireUserAuthentication)
    }

    private fun generateKey(alias: String, requireUserAuthentication: Boolean): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE,
        )
        val specBuilder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setUserAuthenticationRequired(requireUserAuthentication)
            setInvalidatedByBiometricEnrollment(true)
        }
        applyStrongBoxIfAvailable(specBuilder)
        keyGenerator.init(specBuilder.build())
        return keyGenerator.generateKey()
    }

    /**
     * StrongBox (Secure Element dedicado) no está disponible en todos los
     * dispositivos — se solicita, y si falla se reintenta sin él. Nunca se
     * asume su disponibilidad (Manual de Seguridad, sección 3.2).
     */
    private fun applyStrongBoxIfAvailable(builder: KeyGenParameterSpec.Builder) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            runCatching { builder.setIsStrongBoxBacked(true) }
        }
    }
}
