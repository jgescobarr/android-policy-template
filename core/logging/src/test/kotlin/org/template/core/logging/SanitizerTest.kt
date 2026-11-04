package org.template.core.logging

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class SanitizerTest {

    private val sanitizer = Sanitizer()

    @Test
    fun `enmascara un token Bearer`() {
        val input = "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.abc123.signature"
        val result = sanitizer.sanitize(input)

        assertFalse(result.contains("eyJhbGciOiJIUzI1NiJ9"))
        assertEquals("Authorization: [REDACTADO]", result)
    }

    @Test
    fun `enmascara un número de tarjeta`() {
        val input = "Tarjeta rechazada: 4111 1111 1111 1111"
        val result = sanitizer.sanitize(input)

        assertFalse(result.contains("4111"))
    }

    @Test
    fun `enmascara un correo electrónico`() {
        val input = "Usuario ada@example.com no encontrado"
        val result = sanitizer.sanitize(input)

        assertEquals("Usuario [REDACTADO] no encontrado", result)
    }

    @Test
    fun `enmascara campos de contraseña y secreta`() {
        val input = "Login con password=superSecret123&user=admin"
        val result = sanitizer.sanitize(input)

        assertFalse(result.contains("superSecret123"))
    }

    @Test
    fun `no modifica un mensaje sin datos sensibles`() {
        val input = "Perfil cargado correctamente"
        assertEquals(input, sanitizer.sanitize(input))
    }

    @Test
    fun `bloquea intentos de Script Injection XSS`() {
        val input = "Comentario: <script>alert('xss')</script>"
        val result = sanitizer.sanitize(input)

        assertFalse(result.contains("<script>"))
        assertEquals("Comentario: [SCRIPT_INJECTION_BLOQUEADO]", result)
    }

    @Test
    fun `bloquea esquemas javascript y manejadores de eventos`() {
        val input = "URL sospechosa javascript:alert(1) o img onerror=alert(2)"
        val result = sanitizer.sanitize(input)

        assertFalse(result.contains("javascript:"))
        assertFalse(result.contains("onerror="))
    }

    @Test
    fun `normaliza saltos de linea para prevenir Log Forgery CRLF Injection`() {
        val input = "Usuario admin\n[ERROR] Falsa entrada de log"
        val result = sanitizer.sanitize(input)

        assertFalse(result.contains("\n"))
        assertEquals("Usuario admin | [ERROR] Falsa entrada de log", result)
    }

    @Test
    fun `bloquea secuencias de control ANSI Path Traversal y SQL Injection`() {
        val input = "\u001B[31mError\u001B[0m en ../../etc/passwd o UNION SELECT * FROM users"
        val result = sanitizer.sanitize(input)

        assertFalse(result.contains("\u001B[31m"))
        assertFalse(result.contains("../../"))
        assertFalse(result.contains("UNION SELECT"))
    }
}
