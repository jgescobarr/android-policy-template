package org.template.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import kotlin.math.pow

private const val MAX_RETRIES = 3
private const val BASE_BACKOFF_MILLIS = 500L
private val RETRYABLE_STATUS_CODES = 500..599
private val IDEMPOTENT_METHODS = setOf("GET", "HEAD", "OPTIONS")
private const val IDEMPOTENCY_HEADER = "Idempotency-Key"

/**
 * Reintenta peticiones fallidas por errores transitorios de red con backoff
 * exponencial e incorporación de Jitter aleatorio.
 *
 * Referencia normativa: Manual de Seguridad, sección 8.4 — "Retry
 * Interceptor: reintenta peticiones fallidas por errores transitorios de
 * red (timeouts, errores 5xx) con backoff exponencial, según la política
 * de cada endpoint."
 *
 * TRACEABILITY.md > :core:network — parte de "OkHttp Interceptors
 * (Logging, Auth, Retry) configurados por entorno" (Arq., Crítico · PR)
 *
 * Protección de Idempotencia: Solo reintenta peticiones seguras (GET, HEAD) o
 * peticiones que incluyan el encabezado `Idempotency-Key`, evitando duplicar
 * operaciones de mutación no idempotentes (POST/PUT).
 */
class RetryInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var attempt = 0
        var response = chain.proceed(request)

        val isIdempotent = request.method in IDEMPOTENT_METHODS || request.header(IDEMPOTENCY_HEADER) != null

        while (isIdempotent && !response.isSuccessful && response.code in RETRYABLE_STATUS_CODES && attempt < MAX_RETRIES) {
            response.close()
            Thread.sleep(backoffMillisWithJitter(attempt))
            attempt++
            response = chain.proceed(request)
        }
        return response
    }

    private fun backoffMillisWithJitter(attempt: Int): Long {
        val backoff = (BASE_BACKOFF_MILLIS * 2.0.pow(attempt)).toLong()
        val jitter = kotlin.random.Random.nextLong(0, 100L)
        return backoff + jitter
    }
}
