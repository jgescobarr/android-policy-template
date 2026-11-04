package org.template.core.network

import kotlinx.coroutines.CancellationException
import org.template.domain.model.DomainError
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * Traduce excepciones concretas de la capa de red a [DomainError].
 *
 * Referencia normativa: Manual de Arquitectura, sección 2.7 — "Toda
 * implementación de un Gateway debe capturar las excepciones específicas
 * del framework o SDK subyacente ... y traducirlas a un tipo de error de
 * dominio antes de que crucen hacia capas internas."
 *
 * Todo Repository de :data (pendiente) que use este módulo debe envolver
 * sus llamadas con este mapper antes de retornar un DomainResult — nunca
 * dejar que una IOException de OkHttp cruce hacia :domain directamente.
 *
 * Historial de correcciones (todas confirmadas necesarias en revisión,
 * no solo teóricas):
 * - CancellationException se relanza SIN mapear — Manual de Arquitectura,
 *   sección 7.2 (Structured Concurrency). Convertirla en un DomainError
 *   rompería la cancelación cooperativa de coroutines: quien cancela un
 *   viewModelScope espera que la cancelación se propague, no que se
 *   transforme silenciosamente en un error de negocio.
 * - SSLPeerUnverifiedException (fallo de certificate pinning, Manual de
 *   Seguridad sección 7.2) se distingue de un SSLHandshakeException
 *   genérico: el primero es señal de posible ataque MITM, el segundo es
 *   un problema de conectividad/protocolo ordinario.
 * - HttpException ahora distingue 401/403 (Unauthorized), 404 (NotFound)
 *   y 400/422 (Validation) — antes todo código HTTP que no fuera 401/403/404
 *   cafa en Network, mezclando errores de negocio con errores de conectividad.
 */
fun Throwable.toDomainError(): DomainError {
    // Nunca atrapar y convertir una cancelación — SIEMPRE relanzar.
    if (this is CancellationException) throw this

    return when (this) {
        is HttpException -> when (code()) {
            401, 403 -> DomainError.Unauthorized(this)
            404 -> DomainError.NotFound(message() ?: "Recurso remoto no encontrado")
            400, 422 -> DomainError.Validation(field = "unknown", reason = message() ?: "Solicitud inválida")
            else -> DomainError.Network(this)
        }
        is SSLPeerUnverifiedException -> DomainError.SecurityViolation(cause = this) // fallo de certificate pinning
        is SSLHandshakeException -> DomainError.Network(this) // problema de TLS genérico, no necesariamente MITM
        is UnknownHostException -> DomainError.Network(this) // sin DNS / sin internet
        is ConnectException -> DomainError.Network(this) // servidor caído o rechaza conexión
        is SocketTimeoutException -> DomainError.Network(this)
        is IOException -> DomainError.Network(this)
        else -> DomainError.Unknown(this)
    }
}
