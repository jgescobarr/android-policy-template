package org.template.core.network

import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.template.domain.model.DomainError
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

class NetworkErrorMapperTest {

    private fun httpExceptionWithCode(code: Int): HttpException {
        val body = "".toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(code, body))
    }

    @Test
    fun `CancellationException se relanza sin convertirse en DomainError`() {
        assertThrows(CancellationException::class.java) {
            CancellationException("cancelado").toDomainError()
        }
    }

    @Test
    fun `HttpException 401 mapea a Unauthorized`() {
        val result = httpExceptionWithCode(401).toDomainError()
        assertTrue(result is DomainError.Unauthorized)
    }

    @Test
    fun `HttpException 403 mapea a Unauthorized`() {
        val result = httpExceptionWithCode(403).toDomainError()
        assertTrue(result is DomainError.Unauthorized)
    }

    @Test
    fun `HttpException 404 mapea a NotFound`() {
        val result = httpExceptionWithCode(404).toDomainError()
        assertTrue(result is DomainError.NotFound)
    }

    @Test
    fun `HttpException 400 mapea a Validation`() {
        val result = httpExceptionWithCode(400).toDomainError()
        assertTrue(result is DomainError.Validation)
    }

    @Test
    fun `HttpException 422 mapea a Validation`() {
        val result = httpExceptionWithCode(422).toDomainError()
        assertTrue(result is DomainError.Validation)
    }

    @Test
    fun `HttpException 500 mapea a Network`() {
        val result = httpExceptionWithCode(500).toDomainError()
        assertTrue(result is DomainError.Network)
    }

    @Test
    fun `fallo de certificate pinning mapea a SecurityViolation, no a Network`() {
        val result = SSLPeerUnverifiedException("pin mismatch").toDomainError()
        assertTrue(result is DomainError.SecurityViolation)
    }

    @Test
    fun `SSLHandshakeException genérico mapea a Network, no a SecurityViolation`() {
        val result = SSLHandshakeException("handshake failed").toDomainError()
        assertTrue(result is DomainError.Network)
    }

    @Test
    fun `UnknownHostException mapea a Network`() {
        assertEquals(DomainError.Network::class, UnknownHostException().toDomainError()::class)
    }

    @Test
    fun `ConnectException mapea a Network`() {
        assertEquals(DomainError.Network::class, ConnectException().toDomainError()::class)
    }

    @Test
    fun `SocketTimeoutException mapea a Network`() {
        assertEquals(DomainError.Network::class, SocketTimeoutException().toDomainError()::class)
    }

    @Test
    fun `IOException genérica mapea a Network`() {
        assertEquals(DomainError.Network::class, IOException().toDomainError()::class)
    }

    @Test
    fun `excepción no reconocida mapea a Unknown`() {
        assertEquals(DomainError.Unknown::class, RuntimeException().toDomainError()::class)
    }
}
