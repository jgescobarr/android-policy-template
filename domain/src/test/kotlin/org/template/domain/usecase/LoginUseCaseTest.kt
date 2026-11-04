package org.template.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.template.domain.model.DomainError
import org.template.domain.model.DomainResult
import org.template.domain.model.UserId
import org.template.domain.model.UserProfile
import org.template.domain.repository.AuthRepository

/**
 * Pruebas unitarias para [LoginUseCase].
 *
 * Referencia normativa: Manual de Buenas Prácticas de Código, sección 7.1 —
 * Mockk para aislación de dependencias, Kotlin Coroutines Test para
 * asincronía determinista.
 */
class LoginUseCaseTest {

    private val authRepository: AuthRepository = mockk()
    private val useCase = LoginUseCase(authRepository)

    @Test
    fun `invoke retorna el perfil del usuario autenticado en caso de exito`() = runTest {
        val expectedProfile = UserProfile(id = UserId("u123"), fullName = "Ada Lovelace", email = "ada@example.com")
        coEvery { authRepository.login() } returns DomainResult.Success(expectedProfile)

        val result = useCase()

        assertEquals(DomainResult.Success(expectedProfile), result)
    }

    @Test
    fun `invoke propaga el DomainError cuando la autenticacion falla`() = runTest {
        val expectedError = DomainError.Unauthorized()
        coEvery { authRepository.login() } returns DomainResult.Failure(expectedError)

        val result = useCase()

        assertEquals(DomainResult.Failure(expectedError), result)
    }
}
