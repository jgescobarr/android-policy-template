package org.template.feature.auth.biometric

import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import org.template.core.security.KeyProvider
import javax.crypto.Cipher
import javax.inject.Inject

private const val HIGH_RISK_KEY_ALIAS = "biometric_high_risk_operation_key"

/**
 * Autenticación biométrica para operaciones de la aplicación.
 *
 * Referencia normativa: Manual de Seguridad, sección 5.1 — uso exclusivo
 * de BiometricPrompt de Jetpack (nunca FingerprintManager).
 *
 * TRACEABILITY.md > :feature:auth >
 *   "Uso exclusivo de BiometricPrompt de Jetpack" (Bloqueante · PR)
 *   "Fallback seguro implementado" (Crítico · PR)
 */
class BiometricAuthenticator @Inject constructor(
    private val keyProvider: KeyProvider,
) {
    /**
     * Autenticación de bajo riesgo (p. ej. desbloquear la vista de un saldo
     * ya autenticado en la sesión). Solo confirmación booleana — nunca
     * usar este método para autorizar pagos o cambios de credenciales.
     */
    fun authenticateLowRisk(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val prompt = buildPrompt(activity, onSuccess = { onSuccess() }, onError = onError)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirma tu identidad")
            .setAllowedAuthenticators(Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL)
            .build()
        prompt.authenticate(info)
    }

    /**
     * Autenticación de ALTO riesgo (pagos, cambio de credenciales,
     * revelado de datos sensibles).
     *
     * Referencia normativa: Manual de Seguridad, sección 5.2 (corrección
     * v2.0) — "estas operaciones de alto riesgo deben exigir explícitamente
     * Authenticators.BIOMETRIC_STRONG (Class 3) ... la autenticación debe
     * realizarse mediante BiometricPrompt.CryptoObject, vinculando
     * criptográficamente el resultado de la autenticación a una operación
     * específica de Android Keystore."
     *
     * A diferencia de [authenticateLowRisk], aquí el resultado no puede
     * falsificarse manipulando el callback — la operación criptográfica
     * subyacente solo se completa si la autenticación ocurrió realmente a
     * nivel de hardware.
     *
     * TRACEABILITY.md > :core:security > "Operaciones de alto riesgo usan
     * CryptoObject + Authenticators.BIOMETRIC_STRONG" (Bloqueante · PR)
     */
    fun authenticateHighRisk(
        activity: FragmentActivity,
        onSuccess: (BiometricPrompt.CryptoObject) -> Unit,
        onError: (String) -> Unit,
    ) {
        val secretKey = keyProvider.getOrCreateKey(
            alias = HIGH_RISK_KEY_ALIAS,
            requireUserAuthentication = true,
        )
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, secretKey)
        }
        val cryptoObject = BiometricPrompt.CryptoObject(cipher)

        val prompt = buildPrompt(
            activity,
            onSuccess = { result -> result.cryptoObject?.let(onSuccess) },
            onError = onError,
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autoriza esta operación")
            .setSubtitle("Se requiere tu huella o rostro para continuar")
            .setAllowedAuthenticators(Authenticators.BIOMETRIC_STRONG) // Class 3 obligatorio
            .setNegativeButtonText("Cancelar")
            .build()
        prompt.authenticate(info, cryptoObject)
    }

    private fun buildPrompt(
        activity: FragmentActivity,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit,
    ): BiometricPrompt {
        val executor = androidx.core.content.ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess(result)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Manual de Seguridad, sección 5.3 — el fallback seguro
                // (PIN/patrón vía DEVICE_CREDENTIAL) ya está incluido en
                // los Authenticators permitidos; este callback solo maneja
                // el caso de cancelación/error definitivo.
                onError(errString.toString())
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }
}
