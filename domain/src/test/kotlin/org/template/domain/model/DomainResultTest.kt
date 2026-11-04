package org.template.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Manual de Buenas Prácticas de Código, sección 7.1 (Unit Tests
 * Obligatorios para Lógica de Negocio) — JUnit 5 como framework estándar.
 */
class DomainResultTest {

    @Test
    fun `fold invoca onSuccess cuando el resultado es Success`() {
        val result: DomainResult<Int> = DomainResult.Success(42)
        var captured: Int? = null

        result.fold(
            onSuccess = { captured = it },
            onFailure = { throw AssertionError("no debería llamarse onFailure") },
        )

        assertEquals(42, captured)
    }

    @Test
    fun `fold invoca onFailure cuando el resultado es Failure`() {
        val error = DomainError.NotFound(entityId = "user-1")
        val result: DomainResult<Int> = DomainResult.Failure(error)
        var captured: DomainError? = null

        result.fold(
            onSuccess = { throw AssertionError("no debería llamarse onSuccess") },
            onFailure = { captured = it },
        )

        assertEquals(error, captured)
    }

    @Test
    fun `map transforma el valor de Success sin afectar Failure`() {
        val success: DomainResult<Int> = DomainResult.Success(2)
        val failure: DomainResult<Int> = DomainResult.Failure(DomainError.Unknown(RuntimeException()))

        val mappedSuccess = success.map { it * 10 }
        val mappedFailure = failure.map { it * 10 }

        assertEquals(DomainResult.Success(20), mappedSuccess)
        assertEquals(failure, mappedFailure)
    }

    @Test
    fun `flatMap encadena operaciones DomainResult correctamente`() {
        val success: DomainResult<Int> = DomainResult.Success(5)
        val flatMapped = success.flatMap { DomainResult.Success(it * 2) }

        assertEquals(DomainResult.Success(10), flatMapped)
    }

    @Test
    fun `getOrNull retorna valor en Success y null en Failure`() {
        val success: DomainResult<String> = DomainResult.Success("ok")
        val failure: DomainResult<String> = DomainResult.Failure(DomainError.Unauthorized())

        assertEquals("ok", success.getOrNull())
        assertEquals(null, failure.getOrNull())
    }

    @Test
    fun `onSuccess y onFailure ejecutan side-effects correctamente`() {
        var successExecuted = false
        var failureExecuted = false

        DomainResult.Success("test").onSuccess { successExecuted = true }
        DomainResult.Failure(DomainError.Unauthorized()).onFailure { failureExecuted = true }

        assertEquals(true, successExecuted)
        assertEquals(true, failureExecuted)
    }
}
