package org.template.feature.auth.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.exceptions.GetCredentialException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import org.template.core.common.DispatcherProvider
import org.template.core.security.SecureStorage
import org.template.domain.model.DomainError
import org.template.domain.model.DomainResult
import org.template.domain.model.UserId
import org.template.domain.model.UserProfile
import org.template.domain.repository.AuthRepository
import javax.inject.Inject

private const val TOKEN_KEY = "access_token"

/**
 * Implementación de [AuthRepository] usando Credential Manager.
 *
 * Referencia normativa:
 * - Manual de Arquitectura, sección 10.0 — este Gateway traduce cualquier
 *   excepción de Credential Manager a un [DomainError] antes de que cruce
 *   hacia el UseCase; el dominio nunca ve [GetCredentialException].
 * - Manual de Seguridad, sección 5.0 — "Validación de origen en
 *   integraciones federadas: el token recibido debe validarse siempre en
 *   el backend contra el emisor esperado (issuer) y la audiencia
 *   (audience) del token, nunca confiando únicamente en la respuesta del
 *   cliente." Esa validación ocurre en el backend, no en esta clase — este
 *   Gateway solo transporta la credencial, no la autoriza.
 * - Manual de Seguridad, sección 5.0 — "la aplicación no debe implementar
 *   un almacenamiento propio de contraseñas o tokens de passkey en
 *   paralelo a Credential Manager." El único dato que esta clase persiste
 *   es el token de sesión emitido por EL BACKEND tras validar la
 *   credencial — nunca la credencial (contraseña/passkey) en sí misma.
 *
 * TRACEABILITY.md > :feature:auth > "Credential Manager integrado" (Crítico · PR)
 */
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val credentialManager: CredentialManager,
    private val secureStorage: SecureStorage,
    private val dispatchers: DispatcherProvider,
    // TODO: inyectar AuthApi (:core:network) cuando exista el contrato
    // Retrofit real del backend de autenticación — fuera del alcance de
    // este esqueleto de referencia.
) : AuthRepository {

    override suspend fun login(): DomainResult<UserProfile> {
        val request = GetCredentialRequest(credentialOptions = listOf(GetPasswordOption()))

        return try {
            // getCredential() puede necesitar mostrar UI del sistema (selector
            // de passkey/cuenta) — se invoca en el hilo llamador (Main desde
            // el ViewModel), NUNCA envuelta en withContext(dispatchers.io).
            // Manual de Arquitectura, sección 7.4 (evitar bloqueo del Main
            // Thread) no aplica aquí: esta suspensión no bloquea, cede el
            // hilo mientras espera al usuario.
            val response = credentialManager.getCredential(context, request)

            // La persistencia en disco sí es trabajo de IO explícito.
            withContext(dispatchers.io) {
                // TODO: enviar response.credential al backend, recibir token +
                // perfil, y solo entonces persistir el token. Placeholder
                // abajo para que el flujo de UI sea demostrable de extremo a
                // extremo.
                val issuedToken = "placeholder-token-pendiente-de-backend-real"
                secureStorage.saveToken(TOKEN_KEY, issuedToken)
            }

            DomainResult.Success(
                UserProfile(id = UserId("pending"), fullName = "Pendiente de backend", email = ""),
            )
        } catch (e: GetCredentialException) {
            DomainResult.Failure(e.toCredentialDomainError())
        }
    }

    override suspend fun logout() {
        withContext(dispatchers.io) {
            secureStorage.deleteToken(TOKEN_KEY)
        }
    }
}

/**
 * Traduce excepciones de Credential Manager a [DomainError]. Nombrada
 * distinto de la extensión homónima de :core:network (que traduce
 * excepciones de OkHttp) para evitar resolución ambigua entre ambas —
 * ambas existen legítimamente para excepciones de orígenes distintos.
 * Ver Manual de Arquitectura, sección 2.7 (traducción de errores en el
 * borde del Gateway).
 */
private fun GetCredentialException.toCredentialDomainError(): DomainError =
    DomainError.Unauthorized(cause = this)
