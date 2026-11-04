package org.template.core.network

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import org.template.core.security.SecureStorage
import javax.inject.Inject

private const val TOKEN_KEY = "access_token"
private const val AUTH_HEADER = "Authorization"
private const val NO_AUTH_HEADER = "No-Auth"

/**
 * Adjunta el token de acceso a cada petición saliente.
 *
 * Referencia normativa: Manual de Seguridad, sección 9.4 (Secrets
 * Management Externo) — el token nunca se lee de una variable estática ni
 * de BuildConfig; se obtiene en cada petición desde [SecureStorage].
 *
 * TRACEABILITY.md > :core:network > "Secrets Management externo
 * configurado para secretos consultados en tiempo de ejecución" (Auditoría)
 *
 * Protección contra fugas cruzadas: Si la petición incluye la cabecera `No-Auth: true`,
 * omite la adjunción del token bearer para evitar fuga de credenciales en endpoints públicos.
 */
class AuthInterceptor @Inject constructor(
    private val secureStorage: SecureStorage,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Si la petición declara "No-Auth: true", se remueve dicha cabecera y no se adjunta token
        if (originalRequest.header(NO_AUTH_HEADER) != null) {
            val cleanRequest = originalRequest.newBuilder().removeHeader(NO_AUTH_HEADER).build()
            return chain.proceed(cleanRequest)
        }

        val token = runBlocking { secureStorage.getToken(TOKEN_KEY) }
        val request = originalRequest.newBuilder().apply {
            if (token != null && originalRequest.header(AUTH_HEADER) == null) {
                addHeader(AUTH_HEADER, "Bearer $token")
            }
        }.build()
        return chain.proceed(request)
    }
}
